package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.PayrollItem;
import com.intuit.sbd.payroll.psp.adapters.qbdt.wrappers.Rate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.hibernate.FlushMode;
import org.junit.*;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 24, 2010
 * Time: 4:49:38 PM
 */
public class AssistedPayrollItemProcessingTests {
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
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testPayrollItemsAddedWithBalanceFile() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM());

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollItemsAddedWithBalanceFile_MaxLengths() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);

        // test max length of EXPACCT 
        // - should save but be truncated to 255 characters
        StringBuffer buffer = new StringBuffer(400);
        for (int i = 0; i < buffer.capacity(); i++) {
            buffer.append("1");
        }
        String twoFiftyFive = buffer.toString();

        IPITEM pitem = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().get(0);
        pitem.getITAXITEM().setIEXPACCT(twoFiftyFive);
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        // set size to expected 255 length truncation for verification testing
        pitem.getITAXITEM().setIEXPACCT(pitem.getITAXITEM().getIEXPACCT().substring(0, 255));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM());

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM());

        PayrollServices.rollbackUnitOfWork();
    }


    @Test
    public void testPayrollItemsTypeParsing() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(11, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM());

        assertPayrollItemCode(company, PayrollItemCode.Bonus);
        assertPayrollItemCode(company, PayrollItemCode.Commission);
        assertPayrollItemCode(company, PayrollItemCode.DirectDeposit);
        assertPayrollItemCode(company, PayrollItemCode.OtherAdditionPreTax);
        assertPayrollItemCode(company, PayrollItemCode.OtherAdditionPostTax);
        assertPayrollItemCode(company, PayrollItemCode.Hourly);
        assertPayrollItemCode(company, PayrollItemCode.OtherNonTaxableEmployerContribution);
        assertPayrollItemCode(company, PayrollItemCode.OtherTaxableEmployerContribution);
        assertPayrollItemCode(company, PayrollItemCode.OtherPostTaxDeduction);
        assertPayrollItemCode(company, PayrollItemCode.OtherPreTaxDeduction);
        assertPayrollItemCode(company, PayrollItemCode.Salary);

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollItemsUpdatedBeforeAdd() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        List<IPITEM> ipitems = new ArrayList<IPITEM>();
        for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            ipitems.add(ipitem);
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(0, payrollItems.size());

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        assertEquals(0, companyLaws.size());
        PayrollServices.rollbackUnitOfWork();

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 ipitems,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(payrollOfx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollItemsUpdates() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        List<IPITEM> ipitems = new ArrayList<IPITEM>();
        for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            ipitems.add(ipitem);
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD().addAll(ipitems);
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        PayrollServices.rollbackUnitOfWork();

        for (IPITEM ipitem : ipitems) {
            ipitem.setIPITEMNAME(ipitem.getIPITEMNAME() + "updated");
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 ipitems,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(payrollOfx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollItemsInvalidUpdates() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        List<IPITEM> ipitems = new ArrayList<IPITEM>();
        for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            ipitems.add(ipitem);
        }
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        PayrollServices.rollbackUnitOfWork();

        for (IPITEM ipitem : ipitems) {
            if(ipitem.getITAXITEM() != null) {
                ipitem.setITAXITEM(null);
                ipitem.setIADDITEM(OFXRequestGenerator.generateAddition(true,
                                                                        false,
                                                                        null,
                                                                        null,
                                                                        null,
                                                                        null,
                                                                        "blah",
                                                                        false,
                                                                        "blah",
                                                                        null,
                                                                        false,
                                                                        null,
                                                                        new ArrayList<IPITEM>()));
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 ipitems,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(payrollOfx, false);
    }

    @Test
    public void testPayrollItemsLawInvalidUpdates() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        List<IPITEM> ipitems = new ArrayList<IPITEM>();
        for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            ipitems.add(ipitem);
        }
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        PayrollServices.rollbackUnitOfWork();

        for (IPITEM ipitem : ipitems) {
            if(ipitem.getITAXITEM() == null) {
                ipitem.setITAXITEM(OFXRequestGenerator.generateTaxItem(false,
                                                                       false,
                                                                       null,
                                                                       new SpcfMoney("-106800.00"),
                                                                       "blah",
                                                                       "SS_EE",
                                                                       "blah",
                                                                       "",
                                                                       true,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       null,
                                                                       -6.2,
                                                                       null));
                ipitem.setIADDITEM(null);
                ipitem.setIBONUSITEM(null);
                ipitem.setICOMMITEM(null);
                ipitem.setICONTRIBITEM(null);
                ipitem.setIDDITEM(null);
                ipitem.setIDEDUCTITEM(null);
                ipitem.setIHRLYITEM(null);
                ipitem.setISALARYITEM(null);
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 ipitems,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(payrollOfx, false);
    }

    @Test
    public void testPayrollItemsDeletes() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        List<IPITEM> ipitems = new ArrayList<IPITEM>();
        for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            ipitems.add(ipitem);
        }
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        PayrollServices.rollbackUnitOfWork();

        List<String> payrollItemDeletes = new ArrayList<String>();
        for (IPITEM ipitem : ipitems) {
            payrollItemDeletes.add(ipitem.getIPITEMID());
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 payrollItemDeletes,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(payrollOfx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollItemsDeleteNonExistentItems() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().removeAll(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        List<IPITEM> ipitems = new ArrayList<IPITEM>();
        List<String> ipitemIds = new ArrayList<String>();
        for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            ipitems.add(ipitem);
            ipitemIds.add(ipitem.getIPITEMID());
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().removeAll(ipitems);
        assertEquals("payroll items", 0, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().size());
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMDELID().addAll(ipitemIds);
        assertEquals("payroll item deletes", 26, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMDELID().size());
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(0, payrollItems.size());

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(0, companyLaws.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollItemsAddTwice() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().removeAll(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());

        List<IPITEM> ipitems = new ArrayList<IPITEM>();
        for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            ipitems.add(ipitem);
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().addAll(ipitems);
        assertEquals("payroll item adds", 52, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().size());
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM());

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollItemsAddModSameRequest() throws Exception {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().removeAll(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());

        List<IPITEM> ipitems = new ArrayList<IPITEM>();
        for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            ipitems.add(ipitem);
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD().addAll(ipitems);
        assertEquals("payroll item adds", 26, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().size());
        assertEquals("payroll item mods", 26, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD().size());
        QBDTTestHelper.submitQBDTRequest(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(company)));

        assertEquals(15, payrollItems.size());
        assertPayrollItems(payrollItems, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM());

        DomainEntitySet<CompanyLaw> companyLaws = Application.find(CompanyLaw.class,
                new Query<CompanyLaw>()
                        .Where(CompanyLaw.CompanyAgency().Company().equalTo(company)));

        // 7 fed and 4 CA
        assertEquals(11, companyLaws.size());
        assertCompanyLaws(companyLaws, ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollItem_TokenUpdate() {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008,1,1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        assertEquals("beginning token", 1, company.getCurrentToken());
        long currentToken = company.getCurrentToken();
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        ++currentToken;
        DomainEntitySet<CompanyPayrollItem> companyPayrollItems = Application.find(CompanyPayrollItem.class);
        for (CompanyPayrollItem companyPayrollItem : companyPayrollItems) {
            assertEquals("payroll item token", currentToken, companyPayrollItem.getQbdtPayrollItemInfo().getToken());
        }
        PayrollServices.rollbackUnitOfWork();
        company = DataLoadServices.refreshCompany(company);
        assertEquals("company token", currentToken, company.getCurrentToken());

        for (IPITEM ipitem : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM()) {
            OFX payrollItemUpdate = new OFX();
            ipitem.setIPITEMNAME(ipitem.getIPITEMNAME() + 1);
            payrollItemUpdate.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
            IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                     null,
                                                                                     null,
                                                                                     null,
                                                                                     null,
                                                                                     null,
                                                                                     Arrays.asList(ipitem),
                                                                                     null,
                                                                                     null,
                                                                                     null,
                                                                                     null,
                                                                                     null);
            payrollItemUpdate.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
            QBDTTestHelper.submitQBDTRequest(payrollItemUpdate);
            PayrollServices.beginUnitOfWork();
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findItemForSourcePayrollItemId(company, ipitem.getIPITEMID());
            if(companyPayrollItem != null) {
                assertEquals("payroll item token", ++currentToken, companyPayrollItem.getQbdtPayrollItemInfo().getToken());
            } else {
                CompanyLaw companyLaw = CompanyLaw.findCompanyLawBySourceId(company, ipitem.getIPITEMID());
                assertEquals("payroll item token", ++currentToken, companyLaw.getQbdtPayrollItemInfo().getToken());
            }
            PayrollServices.rollbackUnitOfWork();
            company = DataLoadServices.refreshCompany(company);
            assertEquals("company token", currentToken, company.getCurrentToken());
            company = DataLoadServices.refreshCompany(company);
        }
    }

    @Test
    public void testPayrollItem_Sync() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);        
        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        List<IPITEM> balanceFileItems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM> syncResponseItems = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMMOD();
        assertEquals("Payroll item mods", balanceFileItems.size(), syncResponseItems.size());
        for (IPITEM balanceFileItem : balanceFileItems) {
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM syncResponseItem : syncResponseItems) {
                if(balanceFileItem.getIPITEMID().equals(syncResponseItem.getIPITEMID())) {
                    assertIPITEMEquals(balanceFileItem, syncResponseItem);
                    break;
                }
            }
        }
    }

    @Test
    public void testPayrollItem_Sync_RatePush() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        QBDTTestHelper.submitQBDTRequest(ofx);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateCompanyLawRate(company.getSourceSystemCd(), company.getSourceCompanyId(), Application.findById(Law.class, Law.FIT), SpcfCalendar.createInstance(2012, 1, 1, SpcfTimeZone.getLocalTimeZone()), 0.028, true));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertEquals(3, CompanyLaw.findCompanyLaw(company, Law.FIT).getQbdtPayrollItemInfo().getRatePushToken());
        PayrollServices.rollbackUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM syncItem = assertOne(syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMMOD());
        assertEquals("Y", syncItem.getITAXITEM().getIRATEPUSH());

        PayrollServices.beginUnitOfWork();
        Application.refresh(company);
        assertEquals(-1, CompanyLaw.findCompanyLaw(company, Law.FIT).getQbdtPayrollItemInfo().getRatePushToken());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayrollItem_SyncDeleted() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        QBDTTestHelper.submitQBDTRequest(ofx);

        IPITEM ipitem = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().get(0);

        OFX payrollItemDelete = new OFX();
        payrollItemDelete.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 Arrays.asList(ipitem.getIPITEMID()),
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        payrollItemDelete.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(payrollItemDelete);

        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);
        List<String> delIds = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMDELID();
        assertEquals("deleted item ids", 1, delIds.size());
        assertEquals("deleted item id", ipitem.getIPITEMID(), delIds.get(0));
    }

    @Test
    public void testPayrollItem_Sync_RateChanges() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().clear();

        // create SUI item
        IPITEM sui = OFXRequestGenerator.generatePayrollItemTax(false,
                                                                false,
                                                                "CA - Unemployment",
                                                                null,
                                                                OFXRequestGenerator.generateTaxItem(false,
                                                                                                    false,
                                                                                                    "999-9999-5",
                                                                                                    new SpcfMoney("7000.00"),
                                                                                                    OFXRequestGenerator.EXPENSE_ACCOUNT,
                                                                                                    null,
                                                                                                    OFXRequestGenerator.LIABILITY_ACCOUNT,
                                                                                                    "EDD",
                                                                                                    true,
                                                                                                    null,
                                                                                                    "CA",
                                                                                                    "SUI_ER",
                                                                                                    null,
                                                                                                    2.5,
                                                                                                    null));
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().add(sui);
        QBDTTestHelper.submitQBDTRequest(ofx);

        // sync item
        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        List<IPITEM> balanceFileItems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM> syncResponseItems = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMMOD();
        assertEquals("Payroll item mods", balanceFileItems.size(), syncResponseItems.size());
        for (IPITEM balanceFileItem : balanceFileItems) {
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM syncResponseItem : syncResponseItems) {
                if(balanceFileItem.getIPITEMID().equals(syncResponseItem.getIPITEMID())) {
                    assertIPITEMEquals(balanceFileItem, syncResponseItem);
                    break;
                }
            }
        }

        DataLoadServices.setPSPDate(2012, 4, 11);
        // change rates
        Map<String, Double> rateChanges = new HashMap<String, Double>();
        rateChanges.put("20130101", 9.1);
        rateChanges.put("20121001", 8.42555);
        rateChanges.put("20120701", 12.22553);
        rateChanges.put("20110101", 4.12);
        rateChanges.put("20120101", 2.5);

        sui.getITAXITEM().setIRATE("9.1%");
        sui.getITAXITEM().getIRATECHANGE().clear();
        for (String endDate : rateChanges.keySet()) {
            IRATECHANGE iratechange = new IRATECHANGE();
            iratechange.setIDTSUNSET(endDate);
            iratechange.setIRATE(rateChanges.get(endDate) + "%");
            sui.getITAXITEM().getIRATECHANGE().add(iratechange);
        }

        OFX payrollItemUpdate = new OFX();
        payrollItemUpdate.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 Arrays.asList(sui),
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        payrollItemUpdate.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(payrollItemUpdate);

        // sync item again
        company = DataLoadServices.refreshCompany(company);
        syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        List<IPITEM> payrollItems = payrollItemUpdate.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD();
        syncResponseItems = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMMOD();
        assertEquals("Payroll item mods", payrollItems.size(), syncResponseItems.size());
        for (IPITEM payrollItem : payrollItems) {
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM syncResponseItem : syncResponseItems) {
                if(payrollItem.getIPITEMID().equals(syncResponseItem.getIPITEMID())) {
                    assertIPITEMEquals(payrollItem, syncResponseItem);
                    break;
                }
            }
        }
        PayrollServicesTest.validateQbdtRequestInfo(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.QBDT,null, TransmissionType.PayrollSubmission,"Sent Maintenance"),PayrollServicesTest.getQbdtRequestInfo(0,0,0,0,0,0,0,0,1,0,0,0));
    }

    @Test
    public void testPayrollItem_DuplicateBalanceFile_Sync() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        List<IPITEM> balanceFileItems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM> syncResponseItems = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMMOD();
        assertEquals("Payroll item mods", balanceFileItems.size(), syncResponseItems.size());
        for (IPITEM balanceFileItem : balanceFileItems) {
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM syncResponseItem : syncResponseItems) {
                if(balanceFileItem.getIPITEMID().equals(syncResponseItem.getIPITEMID())) {
                    assertIPITEMEquals(balanceFileItem, syncResponseItem);
                    break;
                }
            }
        }

        long nextId = Long.parseLong(company.getNextPayrollItemId());
        for (IPITEM balanceFileItem : balanceFileItems) {
            balanceFileItem.setIPITEMID(nextId++ + "");
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getICOINFOMOD().setIDTFILEQTRSTART(null);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getICOINFOMOD().setITAXREADY(null);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);
        syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        balanceFileItems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        syncResponseItems = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMMOD();
        assertEquals("Payroll item mods", balanceFileItems.size(), syncResponseItems.size());
        for (IPITEM balanceFileItem : balanceFileItems) {
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM syncResponseItem : syncResponseItems) {
                if(balanceFileItem.getIPITEMID().equals(syncResponseItem.getIPITEMID())) {
                    assertIPITEMEquals(balanceFileItem, syncResponseItem);
                    break;
                }
            }
        }
    }

    private void assertIPITEMEquals(IPITEM requestItem, com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM responseItem) {
        assertEquals(requestItem.getIINACTIVE(), responseItem.getIINACTIVE());
        assertEquals(requestItem.getIISEMP(), responseItem.getIISEMP());
        assertEquals(requestItem.getIPITEMNAME(), responseItem.getIPITEMNAME());
        assertEquals(requestItem.getISPECIALTYPE(), responseItem.getISPECIALTYPE());

        if(requestItem.getIADDITEM() != null) {
            assertNotNull(responseItem.getIADDITEM());
            IADDITEM reqAdditem = requestItem.getIADDITEM();
            com.intuit.sbd.payroll.psp.common.ofx.response.IADDITEM respAdditem = responseItem.getIADDITEM();
            assertEquals(reqAdditem.getIADJGROSS(), respAdditem.getIADJGROSS());
            assertEquals(reqAdditem.getIBASEDONQTY(), respAdditem.getIBASEDONQTY());
            assertEquals(QBOFX.nullStringCheck(reqAdditem.getICOMPID()), respAdditem.getICOMPID());
            assertEquals(reqAdditem.getIDEFLIMIT(), respAdditem.getIDEFLIMIT());
            assertEquals(reqAdditem.getIDEFRATE(), respAdditem.getIDEFRATE());
            assertEquals(reqAdditem.getIEXPACCT(), respAdditem.getIEXPACCT());
            assertEquals(reqAdditem.getIEXPBYJOB(), respAdditem.getIEXPBYJOB());
            assertEquals(reqAdditem.getILIABACCT(), respAdditem.getILIABACCT());
            assertEquals(reqAdditem.getILIABAGENCY(), respAdditem.getILIABAGENCY());            
            assertEquals(reqAdditem.getITAXFORMLINE(), respAdditem.getITAXFORMLINE());
            assertEquals(reqAdditem.getITAXAFFECTED().size(), respAdditem.getITAXAFFECTED().size());
        }
        
        if(requestItem.getIBONUSITEM() != null) {
            assertNotNull(responseItem.getIBONUSITEM());
            IBONUSITEM reqBonusitem = requestItem.getIBONUSITEM();
            com.intuit.sbd.payroll.psp.common.ofx.response.IBONUSITEM respBonusitem = responseItem.getIBONUSITEM();
            assertEquals(reqBonusitem.getIEXPACCT(), respBonusitem.getIEXPACCT());
        }

        if(requestItem.getICOMMITEM() != null) {
            assertNotNull(responseItem.getICOMMITEM());
            ICOMMITEM reqCommitem = requestItem.getICOMMITEM();
            com.intuit.sbd.payroll.psp.common.ofx.response.ICOMMITEM respCommitem = responseItem.getICOMMITEM();
            assertEquals(reqCommitem.getIADJGROSS(), respCommitem.getIADJGROSS());
            assertEquals(reqCommitem.getIBASEDONQTY(), respCommitem.getIBASEDONQTY());
            assertEquals(QBOFX.nullStringCheck(reqCommitem.getICOMPID()), respCommitem.getICOMPID());
            assertEquals(QBOFX.convertNULLToEmptyString(reqCommitem.getIDEFLIMIT()), respCommitem.getIDEFLIMIT());
            assertEquals(reqCommitem.getIDEFRATE(), respCommitem.getIDEFRATE());
            assertEquals(reqCommitem.getIEARNINGSTABLE(), respCommitem.getIEARNINGSTABLE());
            assertEquals(reqCommitem.getIEXPACCT(), respCommitem.getIEXPACCT());
            assertEquals(reqCommitem.getIEXPBYJOB(), respCommitem.getIEXPBYJOB());
            assertEquals(QBOFX.convertNULLToEmptyString(reqCommitem.getILIABACCT()), respCommitem.getILIABACCT());
            assertEquals(QBOFX.convertNULLToEmptyString(reqCommitem.getILIABAGENCY()), respCommitem.getILIABAGENCY());
            assertEquals(QBOFX.Y_N(QBOFX.mapOFXStringToBoolean(reqCommitem.getIONSERVICE())), respCommitem.getIONSERVICE());
            assertEquals(QBOFX.convertNULLToEmptyString(reqCommitem.getITAXFORMLINE()), respCommitem.getITAXFORMLINE());
        }

        if(requestItem.getICONTRIBITEM() != null) {
            assertNotNull(responseItem.getICONTRIBITEM());
            ICONTRIBITEM reqContribitem = requestItem.getICONTRIBITEM();
            com.intuit.sbd.payroll.psp.common.ofx.response.ICONTRIBITEM respContribitem = responseItem.getICONTRIBITEM();
            assertEquals(reqContribitem.getIADJGROSS(), respContribitem.getIADJGROSS());
            assertEquals(reqContribitem.getIBASEDONQTY(), respContribitem.getIBASEDONQTY());
            assertEquals(QBOFX.nullStringCheck(reqContribitem.getICOMPID()), respContribitem.getICOMPID());
            assertEquals(QBOFX.convertNULLToEmptyString(reqContribitem.getIDEFLIMIT()), respContribitem.getIDEFLIMIT());
            assertEquals(reqContribitem.getIDEFRATE(), respContribitem.getIDEFRATE());
            assertEquals(reqContribitem.getIEXPACCT(), respContribitem.getIEXPACCT());
            assertEquals(reqContribitem.getIEXPBYJOB(), respContribitem.getIEXPBYJOB());
            assertEquals(reqContribitem.getILIABACCT(), respContribitem.getILIABACCT());
            assertEquals(QBOFX.convertNULLToEmptyString(reqContribitem.getILIABAGENCY()), respContribitem.getILIABAGENCY());
            assertEquals(reqContribitem.getIONSERVICE(), respContribitem.getIONSERVICE());
            assertEquals(QBOFX.convertNULLToEmptyString(reqContribitem.getITAXFORMLINE()), respContribitem.getITAXFORMLINE());
            assertEquals(reqContribitem.getITAXAFFECTED().size(), respContribitem.getITAXAFFECTED().size());
        }

        if(requestItem.getIDDITEM() != null) {
            assertNotNull(responseItem.getIDDITEM());
            IDDITEM reqDditem = requestItem.getIDDITEM();
            com.intuit.sbd.payroll.psp.common.ofx.response.IDDITEM respDditem = responseItem.getIDDITEM();
            assertEquals(QBOFX.nullStringCheck(reqDditem.getICOMPID()), respDditem.getICOMPID());
            assertEquals(reqDditem.getILIABACCT(), respDditem.getILIABACCT());
            assertEquals(reqDditem.getILIABAGENCY(), respDditem.getILIABAGENCY());
        }

        if(requestItem.getIDEDUCTITEM() != null) {
            assertNotNull(responseItem.getIDEDUCTITEM());
            IDEDUCTITEM reqDeductitem = requestItem.getIDEDUCTITEM();
            com.intuit.sbd.payroll.psp.common.ofx.response.IDEDUCTITEM respDeductitem = responseItem.getIDEDUCTITEM();
            assertEquals(reqDeductitem.getIADJGROSS(), respDeductitem.getIADJGROSS());
            assertEquals(reqDeductitem.getIBASEDONQTY(), respDeductitem.getIBASEDONQTY());
            assertEquals(QBOFX.nullStringCheck(reqDeductitem.getICOMPID()), respDeductitem.getICOMPID());
            assertEquals(reqDeductitem.getIDEFLIMIT(), respDeductitem.getIDEFLIMIT());
            assertEquals(reqDeductitem.getIDEFRATE(), respDeductitem.getIDEFRATE());
            assertEquals(reqDeductitem.getILIABACCT(), respDeductitem.getILIABACCT());
            assertEquals(reqDeductitem.getILIABAGENCY(), respDeductitem.getILIABAGENCY());
            assertEquals(reqDeductitem.getIONSERVICE(), respDeductitem.getIONSERVICE());
            assertEquals(reqDeductitem.getITAXFORMLINE(), respDeductitem.getITAXFORMLINE());
            assertEquals(reqDeductitem.getITAXAFFECTED().size(), respDeductitem.getITAXAFFECTED().size());
        }

        if(requestItem.getIHRLYITEM() != null) {
            assertNotNull(responseItem.getIHRLYITEM());
            IHRLYITEM reqHrlyitem = requestItem.getIHRLYITEM();
            com.intuit.sbd.payroll.psp.common.ofx.response.IHRLYITEM respHrlyitem = responseItem.getIHRLYITEM();
            assertEquals(reqHrlyitem.getIEXPACCT(), respHrlyitem.getIEXPACCT());
            assertEquals(reqHrlyitem.getIPAYTYPE(), respHrlyitem.getIPAYTYPE());
        }

        if(requestItem.getISALARYITEM() != null) {
            assertNotNull(responseItem.getISALARYITEM());
            ISALARYITEM reqSalaryitem = requestItem.getISALARYITEM();
            com.intuit.sbd.payroll.psp.common.ofx.response.ISALARYITEM respSalaryitem = responseItem.getISALARYITEM();
            assertEquals(reqSalaryitem.getIEXPACCT(), respSalaryitem.getIEXPACCT());
            assertEquals(reqSalaryitem.getIPAYTYPE(), respSalaryitem.getIPAYTYPE());
        }

        if(requestItem.getITAXITEM() != null) {
            assertNotNull(responseItem.getITAXITEM());
            ITAXITEM reqTaxitem = requestItem.getITAXITEM();
            com.intuit.sbd.payroll.psp.common.ofx.response.ITAXITEM respTaxitem = responseItem.getITAXITEM();
            assertEquals(reqTaxitem.getIADJGROSS(), respTaxitem.getIADJGROSS());
            assertEquals(reqTaxitem.getIBASEDONQTY(), respTaxitem.getIBASEDONQTY());
            assertEquals(QBOFX.nullStringCheck(reqTaxitem.getICOMPID()), respTaxitem.getICOMPID());
            assertEquals(QBOFX.convertNULLToEmptyString(reqTaxitem.getIDEFLIMIT()), respTaxitem.getIDEFLIMIT());
            assertEquals(reqTaxitem.getIEXPACCT(), respTaxitem.getIEXPACCT());
            assertEquals(reqTaxitem.getIFEDTAX(), respTaxitem.getIFEDTAX());
            assertEquals(reqTaxitem.getILIABACCT(), respTaxitem.getILIABACCT());
            assertEquals(reqTaxitem.getILIABAGENCY(), respTaxitem.getILIABAGENCY());
            assertEquals(reqTaxitem.getIONSERVICE(), respTaxitem.getIONSERVICE());
            assertEquals(reqTaxitem.getIOTHERTAX(), respTaxitem.getIOTHERTAX());
            assertEquals(QBOFX.nullStringCheck(reqTaxitem.getIRATE()), respTaxitem.getIRATE());
            assertEquals(QBOFX.convertNULLToEmptyString(reqTaxitem.getITAXFORMLINE()), respTaxitem.getITAXFORMLINE());

            if(reqTaxitem.getISTATETAXDESC() != null) {
                assertNotNull(respTaxitem.getISTATETAXDESC());
                assertEquals(reqTaxitem.getISTATETAXDESC().getISTATE(), respTaxitem.getISTATETAXDESC().getISTATE());
                assertEquals(reqTaxitem.getISTATETAXDESC().getISTATETAX(), respTaxitem.getISTATETAXDESC().getISTATETAX());
            }

            assertEquals(reqTaxitem.getIRATECHANGE().size(), respTaxitem.getIRATECHANGE().size());
            int matches = 0;
            for (IRATECHANGE reqRatechange : reqTaxitem.getIRATECHANGE()) {
                for (com.intuit.sbd.payroll.psp.common.ofx.response.IRATECHANGE respRatechange : respTaxitem.getIRATECHANGE()) {
                    if(reqRatechange.getIDTSUNSET().equals(respRatechange.getIDTSUNSET())) {
                        assertEquals(reqRatechange.getIRATE(), respRatechange.getIRATE());
                        matches++;
                        break;
                    }
                }
            }
            assertEquals(matches, reqTaxitem.getIRATECHANGE().size());
            assertNull(respTaxitem.getIRATEPUSH());
        }
    }

    private void assertPayrollItemCode(Company pCompany, PayrollItemCode pPayrollItemCode) {
        DomainEntitySet<CompanyPayrollItem> payrollItems = Application.find(CompanyPayrollItem.class,
                new Query<CompanyPayrollItem>()
                        .Where(CompanyPayrollItem.Company().equalTo(pCompany)
                        .And(CompanyPayrollItem.PayrollItem().PayrollItemCode().equalTo(pPayrollItemCode))));
        assertEquals(1, payrollItems.size());
    }

    private void assertCompanyLaws(DomainEntitySet<CompanyLaw> pCompanyLaws, Collection<IPITEM> pOFXPayrollItems) {
        for (IPITEM ipitem : pOFXPayrollItems) {
            PayrollItem ofxPayrollItem = new PayrollItem(ipitem);
            if(ofxPayrollItem.getItemType() == QBOFX.OFXPayrollItemType.Tax) {
                CompanyLaw companyLaw = pCompanyLaws.findEntity(CompanyLaw.SourceId().equalTo(ofxPayrollItem.getSourceId()));
                assertNotNull("Company law not found with id" + ofxPayrollItem.getSourceId(), companyLaw);

                assertQBDTPayrollItemInfo(ofxPayrollItem, companyLaw.getQbdtPayrollItemInfo());
                Map<Date,Rate> map=ofxPayrollItem.getRateChanges();
                for (CompanyLawRate companyLawRate : companyLaw.getCompanyLawRateCollection()) {
                    if (companyLawRate.calculateExpirationDate() == null) {
                        assertEquals("Company Law rate does not equal future rate.", new Double(companyLawRate.getRate()), ofxPayrollItem.getFutureRate().getRate());
                        assertEquals("Company Law rate type does not equal future rate.",companyLawRate.getRateType(), ofxPayrollItem.getFutureRate().getRateType());
                    }
                    else if (map.containsKey(new Date(companyLawRate.calculateExpirationDate().getTimeInMilliseconds()))) {
                        assertEquals("Company Law rate does not equal OFX Item rate.", new Double(companyLawRate.getRate()), map.get(new Date(companyLawRate.calculateExpirationDate().getTimeInMilliseconds())).getRate());
                        assertEquals("Company Law rate type does not equal OFX Item rate.", companyLawRate.getRateType(), map.get(new Date(companyLawRate.calculateExpirationDate().getTimeInMilliseconds())).getRateType());
                    }
                }
                assertEquals(ofxPayrollItem.getPayrollItemStatus(), companyLaw.getStatus());
                assertEquals(ofxPayrollItem.getSourceDescription(), companyLaw.getSourceDescription());
                assertEquals(ofxPayrollItem.getTaxFormLine(), companyLaw.getTaxFormLine());
            }
        }
    }

    private void assertPayrollItems(DomainEntitySet<CompanyPayrollItem> pCompanyPayrollItems, Collection<IPITEM> pOFXPayrollItems) {
        for (IPITEM ipitem : pOFXPayrollItems) {
            PayrollItem ofxPayrollItem = new PayrollItem(ipitem);
            if(ofxPayrollItem.getItemType() != QBOFX.OFXPayrollItemType.Tax) {
                CompanyPayrollItem companyPayrollItem = pCompanyPayrollItems.findEntity(CompanyPayrollItem.SourcePayrollItemId().equalTo(ofxPayrollItem.getSourceId()));
                assertNotNull("Company payroll item not found with id" + ofxPayrollItem.getSourceId(), companyPayrollItem);

                assertQBDTPayrollItemInfo(ofxPayrollItem, companyPayrollItem.getQbdtPayrollItemInfo());

                assertEquals(ofxPayrollItem.getTaxableToPayrollItemIds().size(), companyPayrollItem.getPayrollItemTaxableToCollection().size());
                assertEquals(ofxPayrollItem.getPayrollItemStatus(), companyPayrollItem.getStatus());
                assertEquals(ofxPayrollItem.getSourceDescription(), companyPayrollItem.getSourceDescription());
                assertEquals(ofxPayrollItem.getTaxFormLine(), companyPayrollItem.getTaxFormLine());
            }
        }
    }

    private void assertQBDTPayrollItemInfo(PayrollItem pOfxPayrollItem, QbdtPayrollItemInfo pQbdtPayrollItemInfo) {
        assertEquals(pOfxPayrollItem.getAdjustsGross(), pQbdtPayrollItemInfo.getAdjustsGross());
        assertEquals(pOfxPayrollItem.getBasedOnQuantity(), pQbdtPayrollItemInfo.getBasedOnQuantity());
        assertEquals(pOfxPayrollItem.getDefaultLimit(), pQbdtPayrollItemInfo.getDefaultLimit());
        assertEquals(pOfxPayrollItem.getDefaultRate(), pQbdtPayrollItemInfo.getDefaultRate());
        assertEquals(pOfxPayrollItem.getDefaultRateType(), pQbdtPayrollItemInfo.getDefaultRateType());
        assertEquals(pOfxPayrollItem.getEarningsTable(), pQbdtPayrollItemInfo.getEarningsTable());
        assertEquals(pOfxPayrollItem.getExpenseAccount(), pQbdtPayrollItemInfo.getExpenseAccount());
        assertEquals(pOfxPayrollItem.getExpenseByJob(), pQbdtPayrollItemInfo.getExpenseByJob());
        assertEquals(pOfxPayrollItem.getIsEmployeePaid(), pQbdtPayrollItemInfo.getIsEmployeePaid());
        assertEquals(pOfxPayrollItem.getLiabilityAccount(), pQbdtPayrollItemInfo.getLiabilityAccount());
        assertEquals(pOfxPayrollItem.getLiabilityAgency(), pQbdtPayrollItemInfo.getLiabilityAgency());
        assertEquals(pOfxPayrollItem.getOnService(), pQbdtPayrollItemInfo.getOnService());
        assertEquals(pOfxPayrollItem.getPayType(), pQbdtPayrollItemInfo.getPayType());
        assertEquals(pOfxPayrollItem.getSpecialType(), pQbdtPayrollItemInfo.getSpecialType());
        assertEquals(pOfxPayrollItem.getAgencyId(), pQbdtPayrollItemInfo.getAgencyId());
    }
    @Test
    public void testPayrollItem_Sync_RateChanges_PSRV004127_DupeRatesInPItem() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().clear();

        // create SUI item
        IPITEM sui = OFXRequestGenerator.generatePayrollItemTax(false,
                                                                false,
                                                                "CA - Unemployment",
                                                                null,
                                                                OFXRequestGenerator.generateTaxItem(false,
                                                                                                    false,
                                                                                                    "999-9999-5",
                                                                                                    new SpcfMoney("7000.00"),
                                                                                                    OFXRequestGenerator.EXPENSE_ACCOUNT,
                                                                                                    null,
                                                                                                    OFXRequestGenerator.LIABILITY_ACCOUNT,
                                                                                                    "EDD",
                                                                                                    true,
                                                                                                    null,
                                                                                                    "CA",
                                                                                                    "SUI_ER",
                                                                                                    null,
                                                                                                    2.5,
                                                                                                    null));
        //Adding 2 duplicate taxitems   .
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().add(sui);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().add(sui);
        System.out.println(OFXManager.javaRequestToOFX(ofx));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(ofx);
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        CompanyLaw law = CompanyLaw.findCompanyLaw(company,"87");
        DomainEntitySet<CompanyLawRate> lawrates = Application.find(CompanyLawRate.class, CompanyLawRate.CompanyLaw().equalTo(law)) ;
        PayrollServices.rollbackUnitOfWork();
        junit.framework.Assert.assertEquals("Law Rates Collection", 1, lawrates.size());
    }
    @Test
    public void testPayrollItem_Sync_RateChanges_PSRV004127_DupeRatesInPItemMod() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().clear();
        IPAYROLLRQ ipayrollrq = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ();
        // create SUI item
        IPITEM sui = OFXRequestGenerator.generatePayrollItemTax(false,
                                                                false,
                                                                "CA - Unemployment",
                                                                null,
                                                                OFXRequestGenerator.generateTaxItem(false,
                                                                                                    false,
                                                                                                    "999-9999-5",
                                                                                                    new SpcfMoney("7000.00"),
                                                                                                    OFXRequestGenerator.EXPENSE_ACCOUNT,
                                                                                                    null,
                                                                                                    OFXRequestGenerator.LIABILITY_ACCOUNT,
                                                                                                    "EDD",
                                                                                                    true,
                                                                                                    null,
                                                                                                    "CA",
                                                                                                    "SUI_ER",
                                                                                                    null,
                                                                                                    2.5,
                                                                                                    null));
        //Adding 2 duplicate taxitems   .
        ipayrollrq.getIPITEMMOD().add(sui);
        ipayrollrq.getIPITEMMOD().add(sui);
        System.out.println(OFXManager.javaRequestToOFX(ofx));
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(ofx);
        PayrollServices.beginUnitOfWork(FlushMode.MANUAL);
        CompanyLaw law = CompanyLaw.findCompanyLaw(company,"87");
        DomainEntitySet<CompanyLawRate> lawrates = Application.find(CompanyLawRate.class, CompanyLawRate.CompanyLaw().equalTo(law)) ;
        PayrollServices.rollbackUnitOfWork();
        junit.framework.Assert.assertEquals("Law Rates Collection", 1, lawrates.size());
    }

    @Test
    public void testPayrollItem_Sync_RateChanges_PSP2476_SunsetUnexpiredCurrentQuarter() {
        String psid = "123456789";

        DataLoadServices.setPSPDate(2012, 1, 1);

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateAllPayrollItemTypes(psid);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().clear();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().clear();

        // create SUI item
        IPITEM sui = OFXRequestGenerator.generatePayrollItemTax(false,
                                                                false,
                                                                "CA - Unemployment",
                                                                null,
                                                                OFXRequestGenerator.generateTaxItem(false,
                                                                                                    false,
                                                                                                    "999-9999-5",
                                                                                                    new SpcfMoney("7000.00"),
                                                                                                    OFXRequestGenerator.EXPENSE_ACCOUNT,
                                                                                                    null,
                                                                                                    OFXRequestGenerator.LIABILITY_ACCOUNT,
                                                                                                    "EDD",
                                                                                                    true,
                                                                                                    null,
                                                                                                    "CA",
                                                                                                    "SUI_ER",
                                                                                                    null,
                                                                                                    2.5,
                                                                                                    null));
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().add(sui);
        QBDTTestHelper.submitQBDTRequest(ofx);

        // sync item
        company = DataLoadServices.refreshCompany(company);
        OFX syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        List<IPITEM> balanceFileItems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM> syncResponseItems = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMMOD();
        assertEquals("Payroll item mods", balanceFileItems.size(), syncResponseItems.size());
        for (IPITEM balanceFileItem : balanceFileItems) {
            for (com.intuit.sbd.payroll.psp.common.ofx.response.IPITEM syncResponseItem : syncResponseItems) {
                if(balanceFileItem.getIPITEMID().equals(syncResponseItem.getIPITEMID())) {
                    assertIPITEMEquals(balanceFileItem, syncResponseItem);
                    break;
                }
            }
        }

        DataLoadServices.setPSPDate(2012, 4, 11);
        // change rates
        Map<String, Double> rateChanges = new HashMap<String, Double>();
        rateChanges.put("20110101", 4.12);
        rateChanges.put("20120101", 2.5);
        rateChanges.put("20120701", 9.1);

        sui.getITAXITEM().setIRATE("9.1%");
        sui.getITAXITEM().getIRATECHANGE().clear();
        for (String endDate : rateChanges.keySet()) {
            IRATECHANGE iratechange = new IRATECHANGE();
            iratechange.setIDTSUNSET(endDate);
            iratechange.setIRATE(rateChanges.get(endDate) + "%");
            sui.getITAXITEM().getIRATECHANGE().add(iratechange);
        }

        OFX payrollItemUpdate = new OFX();
        payrollItemUpdate.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 Arrays.asList(sui),
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null,
                                                                                 null);
        payrollItemUpdate.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        QBDTTestHelper.submitQBDTRequest(payrollItemUpdate);

        // sync item again
        company = DataLoadServices.refreshCompany(company);
        syncRequest = OFXRequestGenerator.generateSyncRequest(psid, company.getCurrentToken()-1);
        syncResponse = QBDTTestHelper.submitQBDTRequest(syncRequest);

        List<IPITEM> payrollItems = payrollItemUpdate.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEMMOD();
        syncResponseItems = syncResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPITEMMOD();
        assertEquals("Payroll item mods", payrollItems.size(), syncResponseItems.size());

        // Manually verify the IRATE and IRATECHANGE values.
        assertEquals("9.1%", syncResponseItems.get(0).getITAXITEM().getIRATE());
        List<com.intuit.sbd.payroll.psp.common.ofx.response.IRATECHANGE> sunsetRates = syncResponseItems.get(0).getITAXITEM().getIRATECHANGE();
        assertEquals("20120701", sunsetRates.get(0).getIDTSUNSET());
        assertEquals("9.1%", sunsetRates.get(0).getIRATE());
        assertEquals("20120101", sunsetRates.get(1).getIDTSUNSET());
        assertEquals("2.5%", sunsetRates.get(1).getIRATE());
        assertEquals("20110101", sunsetRates.get(2).getIDTSUNSET());
        assertEquals("4.12%", sunsetRates.get(2).getIRATE());
    }

}
