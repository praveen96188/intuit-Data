package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntity;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.billing.UsageOFXDataloader;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.MockSocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.SocketManagerFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollFrequencyDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.ReturnFileParser;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.batchjobs.processors.NightlyBatchJobsProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.util.AchReturnAccountingFile;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS;
import com.intuit.sbd.payroll.psp.common.utils.MailSenderHolder;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.*;
import org.junit.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 4/5/13
 * Time: 2:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class JAZZTest {

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
    public void paystubHappyPath() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_1.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 2", 2, paystubs.size());
        assertNotNull("Paystub company null",paystubs.get(0).getCompany());
        assertEquals(company.getSourceCompanyId(),paystubs.get(0).getCompany().getSourceCompanyId());
        assertNotNull("Paystub company null",paystubs.get(1).getCompany());
        assertEquals(company.getSourceCompanyId(),paystubs.get(1).getCompany().getSourceCompanyId());


        DomainEntitySet<PstubEmployeeInfo> pstubEmployees = Application.find(PstubEmployeeInfo.class);
        assertEquals("total number of pstubEmployees should be 1", 1, pstubEmployees.size());

        DomainEntitySet<PstubEmployerInfo> pstubEmployers = Application.find(PstubEmployerInfo.class);
        assertEquals("total number of pstubEmployers should be 1", 1, pstubEmployers.size());

        DomainEntitySet<PstubAddress> pstubAddresses = Application.find(PstubAddress.class);
        assertEquals("total number of pstubAddresses should be 2", 2, pstubAddresses.size());

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testPaycheckLimit() throws Exception {
        String oldValue = SystemParameter.findStringValue(SystemParameter.Code.MAX_NUM_PAYCHKS_PER_OFX);

        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.MAX_NUM_PAYCHKS_PER_OFX, "1");
        PayrollServices.commitUnitOfWork();

        try {
            String psid = "100093352";
            DataLoadServices.setPSPDate(2013, 2, 7);
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            if (company == null) {
                company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
                DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            }

            String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_1.xml"));
            System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false));
        } finally {
            PayrollServices.beginUnitOfWork();
            SystemParameter.update(SystemParameter.Code.MAX_NUM_PAYCHKS_PER_OFX, oldValue);
            PayrollServices.commitUnitOfWork();
        }
    }

    @Test
    public void testEERenumbering() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_1.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        // both paychecks point to emp1 now
        Application.beginUnitOfWork();
        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class, Paycheck.Company().equalTo(company));
        assertEquals("total number of paychecks should be 2", 2, paychecks.size());

        for (Paycheck paycheck : paychecks) {
            assertEquals("both paychecks are associated with emp 1", "1", paycheck.getSourceEmployee().getSourceEmployeeId());
        }
        Application.rollbackUnitOfWork();

        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_6.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        // one paycheck points to emp2 now
        Application.beginUnitOfWork();
        paychecks = Application.find(Paycheck.class, Paycheck.Company().equalTo(company));
        assertEquals("total number of paychecks should be 2", 2, paychecks.size());

        for (Paycheck paycheck : paychecks) {
            if (paycheck.getSourcePaycheckId().equals("1")) {
                assertEquals("paycheck 1 is associated with emp 2", "2", paycheck.getSourceEmployee().getSourceEmployeeId());
            } else if (paycheck.getSourcePaycheckId().equals("2")) {
                assertEquals("paycheck 2 is associated with emp 1", "1", paycheck.getSourceEmployee().getSourceEmployeeId());
            }
        }
        Application.rollbackUnitOfWork();
    }

    @Test
    public void paystubModTest() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            //DataLoadServices.addCompanyBankAccount(company);
        }

        // create paustub 1 and 2
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_1.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        // a mod on paystub 1 & 2
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_2.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 2", 2, paystubs.size());
        assertNotNull("Paystub company null",paystubs.get(0).getCompany());
        assertEquals(company.getSourceCompanyId(),paystubs.get(0).getCompany().getSourceCompanyId());
        assertNotNull("Paystub company null",paystubs.get(1).getCompany());
        assertEquals(company.getSourceCompanyId(),paystubs.get(1).getCompany().getSourceCompanyId());

        DomainEntitySet<PstubEmployeeInfo> pstubEmployees = Application.find(PstubEmployeeInfo.class);
        assertEquals("total number of pstubEmployees should be 1", 1, pstubEmployees.size());

        DomainEntitySet<PstubEmployerInfo> pstubEmployers = Application.find(PstubEmployerInfo.class);
        assertEquals("total number of pstubEmployers should be 1", 1, pstubEmployers.size());

        DomainEntitySet<PstubAddress> pstubAddresses = Application.find(PstubAddress.class);
        assertEquals("total number of pstubAddresses should be 2", 2, pstubAddresses.size());

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testCancelCloudV2() {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            //DataLoadServices.addCompanyBankAccount(company);
        }

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.ViewMyPaycheck, ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertFalse(company.getService(ServiceCode.CloudV2).isActive());
        assertFalse(company.getService(ServiceCode.ViewMyPaycheck).isActive());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testDDPayrollSubmissionAfterCancelDD() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2008, 7, 20);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.addCompanyBankAccount(company);
        }

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        // create DD paycheck
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_5.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        //
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class);
        assertEquals("Money movement should not be created", 0, moneyMovementTransactions.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHistoricalDDWithCtrlE() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2008, 7, 26);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        // submit payroll without list ids
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_14.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class);
        assertEquals("Paychecks should be created", 2, paychecks.size());
        PayrollServices.rollbackUnitOfWork();

        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/sync_req_1.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        // Some of the future dd paychecks don't have list ids. Watermark date should be the latest paycheck date
        DomainEntitySet<QuickbooksInfo> qbInfo = Application.find(QuickbooksInfo.class);

        SpcfCalendar cal = SpcfCalendar.createInstance(2008, 7, 30, SpcfTimeZone.getLocalTimeZone());
        assertEquals("Watermark date should be latest paycheck date",
                     cal, qbInfo.get(0).getWatermarkDate().toLocal());

        // submit paychecks with tomorrow's date, 07/29/2008
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_9.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false));

        PayrollServices.beginUnitOfWork();
        paychecks = Application.find(Paycheck.class);
        assertEquals("Paychecks should not be created", 2, paychecks.size());
        PayrollServices.rollbackUnitOfWork();

        // submit ofx with new paychecks with paycheck date of 09/01/2008
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_11.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        PayrollServices.beginUnitOfWork();
        paychecks = Application.find(Paycheck.class);
        assertEquals("Paychecks should be created", 5, paychecks.size());
        PayrollServices.rollbackUnitOfWork();

        //submit ofx with no DD paychecks with old date and this should create new paychecks
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_12.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        PayrollServices.beginUnitOfWork();
        paychecks = Application.find(Paycheck.class);
        assertEquals("Paychecks should be created", 8, paychecks.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHistoricalDDWithCtrlEWithListIds() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2008, 7, 30);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_10.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class);
        assertEquals("Paychecks should be created", 2, paychecks.size());
        PayrollServices.rollbackUnitOfWork();

        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/sync_req_1.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        // All the future dd paychecks have list ids. Watermark date is today
        DomainEntitySet<QuickbooksInfo> qbInfo = Application.find(QuickbooksInfo.class);
        SpcfCalendar cal = SpcfCalendar.createInstance(2008, 7, 30, SpcfTimeZone.getLocalTimeZone());
        assertEquals("Watermark date should be today's date",
                     cal, qbInfo.get(0).getWatermarkDate().toLocal());

        // submit OFX  request with old paycheck date
        // This should not create any new paychecks
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_9.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false));

        PayrollServices.beginUnitOfWork();
        paychecks = Application.find(Paycheck.class);
        assertEquals("Paychecks should not be created", 2, paychecks.size());
        PayrollServices.rollbackUnitOfWork();

        // submit ofx with new paychecks
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_11.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        PayrollServices.beginUnitOfWork();
        paychecks = Application.find(Paycheck.class);
        assertEquals("Paychecks should be created", 5, paychecks.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHistoricalDDWithCtrlEForMango() throws Exception{

        String  psid = "100093352";

        DataLoadServices.setPSPDate(2008,07,31);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        if(company == null){
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_26.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class);
        PayrollServices.rollbackUnitOfWork();


        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/sync_req_2.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        DomainEntitySet<QuickbooksInfo> qbInfo = Application.find(QuickbooksInfo.class);
        SpcfCalendar cal = SpcfCalendar.createInstance(2008, 07, 31, SpcfTimeZone.getLocalTimeZone());
        assertEquals("Watermark date should be today's date", cal, qbInfo.get(0).getWatermarkDate().toLocal());

    }

    @Test
    public void testHistoricalDDWithCtrlEWoDD() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 8, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_13.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Paycheck> paychecks = Application.find(Paycheck.class);
        assertEquals("Paychecks should be created", 2, paychecks.size());
        PayrollServices.rollbackUnitOfWork();

        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/sync_req_1.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        DomainEntitySet<QuickbooksInfo> qbInfo = Application.find(QuickbooksInfo.class);
        assertNull("Watermark date should be null", qbInfo.get(0).getWatermarkDate());

        // submit OFX  request with old paycheck date
        // This should create new paychecks, since the first batch had only non-DD paychecks
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_9.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        PayrollServices.beginUnitOfWork();
        paychecks = Application.find(Paycheck.class);
        assertEquals("Paychecks should be created", 5, paychecks.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testHistoricalDD() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2008, 8, 2);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.addCompanyBankAccount(company);
        }

        // create DD paycheck
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_7.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        //
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class);
        assertEquals("Money movement should not be created", 4, moneyMovementTransactions.size());
        PayrollServices.commitUnitOfWork();

        // paycheck update
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_8.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        // check again
        PayrollServices.beginUnitOfWork();
        moneyMovementTransactions = Application.find(MoneyMovementTransaction.class);
        assertEquals("Money movement should not be created", 4, moneyMovementTransactions.size());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCompUpdateAfterCancelDD() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2008, 7, 20);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.addCompanyBankAccount(company);
        }

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        //Update Company Info
        PayrollServices.beginUnitOfWork();
        CompanyDTO company2 = (new DataLoader()).getTestIntuitCompany2();
        company2.setSourceSystemCd(company.getSourceSystemCd());
        company2.setCompanyId(company.getSourceCompanyId());
        company2.setPhone("123-873-2999");
        company2.setPayrollFrequencyCd(PayrollFrequencyDTO.Monthly);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateCompany(company.getSourceSystemCd(), company.getSourceCompanyId(), company2);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testCloudV2SubStatus() {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        Application.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertEquals(ServiceSubStatusCode.ActiveCurrent, company.getService(ServiceCode.CloudV2).getStatusCd());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testReactivateCloudV2() {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            //DataLoadServices.addCompanyBankAccount(company);
        }

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.ViewMyPaycheck, ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        Application.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertFalse(company.getService(ServiceCode.CloudV2).isActive());
        assertFalse(company.getService(ServiceCode.ViewMyPaycheck).isActive());

        PayrollServices.companyManager.reactivateService(company.getSourceSystemCd(),
                                                         company.getSourceCompanyId(),
                                                         ServiceCode.ViewMyPaycheck);
        Application.commitUnitOfWork();


        Application.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertTrue(company.getService(ServiceCode.CloudV2).isActive());
        assertTrue(company.getService(ServiceCode.ViewMyPaycheck).isActive());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testWCWarnings() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.WorkersComp);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_3.xml"));
        String response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        assertTrue("first name warning is incorrect", response.contains("For employee Salary23, the first name is missing or too long."));
        assertTrue("work state warning is incorrect", response.contains("For employee Salary23, the work state is missing or too long."));
    }

    @Test
    public void testEMP() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 3, 27);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_4.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        Application.beginUnitOfWork();
        DomainEntitySet<Employee> employees = Application.find(Employee.class);
        boolean foundEE = false;
        for (Employee e : employees) {
            if ("emp".equals(e.getFirstName())) {
                foundEE = true;
                break;
            }
        }
        assertTrue("employee 3 is added with names", foundEE);

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testQBDTVer() throws Exception {
        PayrollServices.beginUnitOfWork();
        SystemParameter.update(SystemParameter.Code.LISTID_SUPPORTED_QBDT_VER, "22R15,23R9,24R3");
        PayrollServices.commitUnitOfWork();

        OFXAPPVERObject ver = new OFXAPPVERObject("17.01.R.10");
        assertFalse(ver.listIdLoopBackSupported());

        ver = new OFXAPPVERObject("22.01.R.10");
        assertFalse(ver.listIdLoopBackSupported());

        ver = new OFXAPPVERObject("22.01.R.15");
        assertTrue(ver.listIdLoopBackSupported());

        ver = new OFXAPPVERObject("23.01.R.07");
        assertFalse(ver.listIdLoopBackSupported());

        ver = new OFXAPPVERObject("23.01.R.09");
        assertTrue(ver.listIdLoopBackSupported());

        ver = new OFXAPPVERObject("24.01.R.01");
        assertFalse(ver.listIdLoopBackSupported());

        ver = new OFXAPPVERObject("24.01.R.03");
        assertTrue(ver.listIdLoopBackSupported());

        ver = new OFXAPPVERObject("25.01.R.03");
        assertTrue(ver.listIdLoopBackSupported());
    }

    @Test
    public void testPaycheck5003() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 10, 20);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.addCompanyBankAccount(company);

        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_15.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        // Simulate the OFX pre symphony without qb paycheck info
        // These were throwing 5003 error earlier.
        Application.beginUnitOfWork();
        QbdtPaycheckInfo pcInfo = Application.find(QbdtPaycheckInfo.class).get(0);
        Application.delete(pcInfo);
        Application.commitUnitOfWork();

        // paycheck update
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_16.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 1", 1, paystubs.size());

        Application.rollbackUnitOfWork();


    }


    @Test
    public void testPaycheck5008() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 10, 20);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.addCompanyBankAccount(company);

        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_17.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        // paycheck update
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_18.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false);


    }


    @Test
    @Ignore
    public void paystubLoadTest() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        OFX request = new UsageOFXDataloader().createOFX(psid, DataLoadServices.PIN, "", "", PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N", true, 1);
        FileWriter fw = new FileWriter("D:\\work\\VMP\\loadtest.xml", false);
        fw.write(OFXManager.javaRequestToOFX(request));
        fw.close();

        for (int i = 500; i <= 5000; i += 500) {
            request = new UsageOFXDataloader().createOFX(psid, DataLoadServices.PIN, "", "", PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N", true, i);
            fw = new FileWriter("D:\\work\\VMP\\loadtest" + i + ".xml", false);
            fw.write(OFXManager.javaRequestToOFX(request));
            fw.close();
        }


        /*
        QBDTTestHelper.processOFXRequestSuccess(request);

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 20", 20, paystubs.size());
        Application.rollbackUnitOfWork();
        */
    }

    @Test
    public void noPerTransactionFee() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck, ServiceCode.DirectDeposit);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.addCompanyBankAccount(company);
        }

        //DataLoadServices.deactivateEntitlementUnit(company.getActivePrimaryEntitlementUnit());
        DataLoadServices.addEntitlementUnit(company, "1234567", "12345", EditionType.EnhancedAccountant, NumberOfEmployeesType.UNLIMITED, DataLoadServices.AssetItemNumber.DIY_YEARLY, SpcfCalendar.createInstance());
        DataLoadServices.updateOffering(company, OfferingCode.DIYDDSTD, "DIYDD-STD");

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_1.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 2", 2, paystubs.size());

        DomainEntitySet<PstubEmployeeInfo> pstubEmployees = Application.find(PstubEmployeeInfo.class);
        assertEquals("total number of pstubEmployees should be 1", 1, pstubEmployees.size());

        DomainEntitySet<PstubEmployerInfo> pstubEmployers = Application.find(PstubEmployerInfo.class);
        assertEquals("total number of pstubEmployers should be 1", 1, pstubEmployers.size());

        DomainEntitySet<PstubAddress> pstubAddresses = Application.find(PstubAddress.class);
        assertEquals("total number of pstubAddresses should be 2", 2, pstubAddresses.size());

        DomainEntitySet<FinancialTransaction> financialTransactions = Application.find(FinancialTransaction.class);
        assertEquals("total number of financial transactions should be 0", 0, financialTransactions.size());

        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class);
        assertEquals("total number of billing details should be 0", 0, billingDetails.size());

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testPaystubCheckDate() throws Exception {
        String psid = "100093352";

        DataLoadServices.setPSPDate(2013, 2, 7);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_1.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true); //System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 2", 2, paystubs.size());

        Date paycheckDateToLocal = new Date(paystubs.get(0).getPaycheckDate().toLocal().getTimeInMilliseconds());
        System.out.println(paystubs.get(0).getPaycheckDate() + " | " + paycheckDateToLocal);

        Calendar cal = Calendar.getInstance();
        cal.setTime(paycheckDateToLocal);
        assertEquals("paycheck date to local", 5, cal.get(Calendar.DAY_OF_MONTH));

        Application.rollbackUnitOfWork();

        // send a paycheck mod - to trigger the UpdatePaystubCore workflow
        // paystub_diy_21.xml is a copy of paystub_diy_1.xml, with PAYCHK replaced by PAYCHKMOD and MODTIMESTAMP changed
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_21.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true); //System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubMods = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 2", 2, paystubMods.size());

        Date paycheckModDateToLocal = new Date(paystubMods.get(0).getPaycheckDate().toLocal().getTimeInMilliseconds());
        System.out.println("MOD " + paystubMods.get(0).getPaycheckDate() + " | " + paycheckModDateToLocal);

        Calendar calMod = Calendar.getInstance();
        calMod.setTime(paycheckModDateToLocal);
        assertEquals("paycheck date to local after MOD", 5, calMod.get(Calendar.DAY_OF_MONTH));

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testAssistedPaystub_EEStateTaxFilingStatusLength() throws Exception {
        String psid = "100093352";

        DataLoadServices.setPSPDate(2013, 2, 7);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.activateTaxService(company);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_assisted_1.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true); //System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 2", 2, paystubs.size());

        Date paycheckDateToLocal = new Date(paystubs.get(0).getPaycheckDate().toLocal().getTimeInMilliseconds());
        System.out.println(paystubs.get(0).getPaycheckDate() + " | " + paycheckDateToLocal);

        Calendar cal = Calendar.getInstance();
        cal.setTime(paycheckDateToLocal);
        assertEquals("paycheck date to local", 5, cal.get(Calendar.DAY_OF_MONTH));

        Application.rollbackUnitOfWork();

        // send a paycheck mod - to trigger the UpdatePaystubCore workflow
        // paystub_diy_eestatusfilingstatuslength.xml is a copy of paystub_assisted_1.xml, with PAYCHK replaced by PAYCHKMOD and MODTIMESTAMP changed
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_eestatusfilingstatuslength.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubMods = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 2", 2, paystubMods.size());

        Date paycheckModDateToLocal = new Date(paystubMods.get(0).getPaycheckDate().toLocal().getTimeInMilliseconds());
        System.out.println("MOD " + paystubMods.get(0).getPaycheckDate() + " | " + paycheckModDateToLocal);

        Calendar calMod = Calendar.getInstance();
        calMod.setTime(paycheckModDateToLocal);
        assertEquals("paycheck date to local after MOD", 5, calMod.get(Calendar.DAY_OF_MONTH));

        Application.rollbackUnitOfWork();
    }

    @Test
    public void testAssistedPaystub_EEStateTaxFilingStatusLength_63() throws Exception {
        String psid = "100093352";

        DataLoadServices.setPSPDate(2013, 2, 7);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.activateTaxService(company);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_assisted_2.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 2", 2, paystubs.size());
        assertEquals("Employee state filing status has 63 chars", 63, paystubs.getFirst().getPstubEmployeeInfo().getStateTaxFilingStatus().length());


        Application.rollbackUnitOfWork();


    }

    @Test
    public void testAssistedPaystub_EEStateTaxFilingStatusLength_64() throws Exception {
        String psid = "100093352";

        DataLoadServices.setPSPDate(2013, 2, 7);

        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.activateTaxService(company);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_assisted_3.xml"));

        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false);

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 0", 0, paystubs.size());


        Application.rollbackUnitOfWork();


    }


    @Test
    public void paystubEmployee5003() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_19.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        Application.beginUnitOfWork();
        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 3", 3, paystubs.size());

        DomainEntitySet<PstubEmployeeInfo> pstubEmployees = Application.find(PstubEmployeeInfo.class);
        assertEquals("total number of pstubEmployees should be 3", 3, pstubEmployees.size());

        DomainEntitySet<PstubEmployerInfo> pstubEmployers = Application.find(PstubEmployerInfo.class);
        assertEquals("total number of pstubEmployers should be 1", 1, pstubEmployers.size());

        Employee employee = Application.find(Employee.class,
                                             Employee.QbdtEmployeeInfo().ListId().equalTo("2147483651-1382505019")).get(0);
        assertTrue(employee.getSourceEmployeeId().equalsIgnoreCase("1"));

        Application.rollbackUnitOfWork();
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_20.xml"));

        Application.beginUnitOfWork();
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        employee = Application.find(Employee.class,
                                    Employee.QbdtEmployeeInfo().ListId().equalTo("2147483651-1382505019")).get(0);
        assertTrue(employee.getSourceEmployeeId().equalsIgnoreCase("2"));
        Application.rollbackUnitOfWork();
    }

    //Ignoring this test because paystub processor changes for this code have been reverted
    @Ignore
    @Test
    public void paystubEmployeeIdsChanged() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_19.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        Application.beginUnitOfWork();

        DomainEntitySet<Employee> employees = Application.find(Employee.class);

        junit.framework.Assert.assertEquals(" Number of employees should be 3", 3, employees.size());
        Employee employee = employees.findEntity(Employee.QbdtEmployeeInfo().ListId().equalTo("2147483651-1382505019"));
        assertTrue(employee.getSourceEmployeeId().equalsIgnoreCase("1"));
        employee = employees.findEntity(
                Employee.QbdtEmployeeInfo().ListId().equalTo("2147483651-1382505020"));
        assertTrue(employee.getSourceEmployeeId().equalsIgnoreCase("2"));


        Application.rollbackUnitOfWork();
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_23.xml"));

        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));

        Application.beginUnitOfWork();
        employees = Application.find(Employee.class);

        junit.framework.Assert.assertEquals(" Number of employees should be 4", 4, employees.size());
        employee = employees.findEntity(Employee.QbdtEmployeeInfo().ListId().equalTo("2147483651-1382505020"));

        assertTrue(employee.getSourceEmployeeId().equalsIgnoreCase("4"));
        employee = employees.findEntity(Employee.QbdtEmployeeInfo().ListId().equalTo("2147483651-1382505033"));
        assertTrue(employee.getSourceEmployeeId().equalsIgnoreCase("2"));
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testWCCancelledPaycheckDelete() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        DataLoadServices.addWorkersCompService(company);
        DataLoadServices.cancelService(company, ServiceCode.WorkersComp);

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_24.xml"));
        String response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        OFX ofx = OFXManager.ofxRequestToJava(AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_24.xml")), OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().removeAll(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYCHKDELID());
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().removeAll(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP());
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN("3");
        List<IPAYCHK> ipitems = new ArrayList<IPAYCHK>();
        List<String> ipitemIds = new ArrayList<String>();
        for (IPAYCHK ipaychk : ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK()) {
            ipitems.add(ipaychk);
            ipitemIds.add(ipaychk.getIPAYCHKID());
        }
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK().removeAll(ipitems);
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYCHKDELID().addAll(ipitemIds);


        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponse = QBDTTestHelper.submitQBDTRequest(ofx);
        IPAYROLLRS ipayrollrs = ofxResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
    }

    @Test
    public void paystubOfxHasMissingNameInPaidTimeOff() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_25.xml"));
        String response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false);
        assertTrue("Error saving paid time off. OFX has non-zero hours with no name sent.", response.contains("Problem We encountered a problem with your payroll transaction request."));
    }

    @Ignore("This works but still marking ignore as It will take more time as running jobs on each day.Use test test6949_build()")
    @Test
    public void test6949() throws Exception {

        String psid = "632000034";

        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,8, 14,12,57,0,0, SpcfTimeZone.getLocalTimeZone()));
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2013, 1, 1));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,10, 10,11,51,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,10, 10,11,52,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.enrollEFTPS(company);
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balance_file.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,10, 10,12,2,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,10, 10,12,3,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request1.xml"));
        String response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,10, 10,23,59,59,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2013, 10, 11);
        DataLoadServices.runJobs(18);
        DataLoadServices.setPSPDate(2013, 10, 29);
        DataLoadServices.runJobsBetween(null, SpcfCalendar.createInstance(2013, 10, 29, 10, 41, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013, 10, 29, 10, 42, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request2.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,10, 29,10,46,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,10, 29,10,47,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request3.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,10, 29,23,59,59,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2013, 10, 30);
        DataLoadServices.runJobs(2);
        DataLoadServices.setPSPDate(2013, 11, 1);
        DataLoadServices.runJobs(12);
        DataLoadServices.setPSPDate(2013, 11, 13);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,11, 13,10,19,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,11, 13,10,20,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX =AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request4.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,11, 13,23,59,59,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2013, 11, 14);
        DataLoadServices.runJobs(10);
        DataLoadServices.setPSPDate(2013, 11, 25);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,11, 25,11,53,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,11, 25,11,54,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request5.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,11, 25,23,59,59,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2013, 11, 26);
        DataLoadServices.runJobs(5);
        DataLoadServices.setPSPDate(2013, 12, 1);
        DataLoadServices.runJobs(10);

        DataLoadServices.setPSPDate(2013, 12, 11);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,12, 11,11,20,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,12, 11,11,21,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request6.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,12, 11,23,59,59,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2013, 12, 12);
        DataLoadServices.runJobs(15);
        DataLoadServices.setPSPDate(2013, 12, 27);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,12, 27,11,19,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,12, 27,11,20,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request7.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,12, 27,11,33,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,12, 27,11,34,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request8.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,12, 27,11,36,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2013,12, 27,11,37,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request9.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2013,12, 27,23,59,59,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2013, 12, 28) ;
        DataLoadServices.runJobs(4);
        DataLoadServices.setPSPDate(2014, 1, 1);
        DataLoadServices.runJobs(12);
        DataLoadServices.setPSPDate(2014, 1, 13);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2014,1, 13,12,25,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2014,1, 13,12,26,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request10.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2014,1, 13,23,59,59,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2014, 1, 14);
        DataLoadServices.runJobs(15);
        DataLoadServices.setPSPDate(2014, 1, 29);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2014,1, 29,12,53,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2014,1, 29,12,54,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request11.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2014,1, 29,12,58,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2014,1, 29,12,59,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request12.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2014,1, 29,23,59,59,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2014, 1, 30);
        DataLoadServices.runJobs(2);
        DataLoadServices.setPSPDate(2014, 2, 1);
        DataLoadServices.runJobs(2);
        DataLoadServices.setPSPDate(2014, 2, 3);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2014,2, 3,9,34,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2014,2, 3,9,35,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request13.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2014,2, 3,10,0,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2014,2, 3,10,01,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request14.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2014,2,3,10,37,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2014,2, 3,10,38,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request15.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2014,2, 3,10,49,0,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(SpcfCalendar.createInstance(2014,2, 3,10,50,0,0, SpcfTimeZone.getLocalTimeZone()));
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request16.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        DataLoadServices.runJobsBetween(null,SpcfCalendar.createInstance(2014,2,3,23,59,59,0, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.setPSPDate(2014, 2, 4);
        DataLoadServices.runJobs(4);
        SpcfDecimal atrAmount= LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(company,LedgerAccountCode.AgencyTaxRefund);
        junit.framework.Assert.assertEquals("ATR amount", SpcfMoney.ZERO, atrAmount);
        SpcfDecimal erpAmount= LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(company,LedgerAccountCode.ERPayable);
        junit.framework.Assert.assertEquals("ERP amount", SpcfMoney.ZERO, erpAmount);
    }

    //This is to test the ATR/ATO issues whenever voiding previuos offloaded payroll after creating same payroll in  next quarter , PSP-6949
    //For thoruogh testing use test6949() added above.( in this test we manully moving txns)
    @Test
    public void test6949_build() throws Exception {

        String psid = "632000034";
        DataLoadServices.setPSPDate(2013, 10, 10);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2013, 1, 1));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);


        DataLoadServices.setPSPDate(2013, 10, 10);
        DataLoadServices.enrollEFTPS(company);
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balance_file.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request1.xml"));
        String response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        markMMTsAsExecuted(company);

        DataLoadServices.setPSPDate(2013, 10, 17);
        sendPaymentsAndSimulateSuccess();

        DataLoadServices.setPSPDate(2013, 10, 29);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request2.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request3.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        markMMTsAsExecuted(company);

        DataLoadServices.setPSPDate(2013, 11, 5);
        sendPaymentsAndSimulateSuccess();

        DataLoadServices.setPSPDate(2013, 11, 13);
        OFX =AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request4.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        markMMTsAsExecuted(company);

        DataLoadServices.setPSPDate(2013, 11, 19);
        sendPaymentsAndSimulateSuccess();

        DataLoadServices.setPSPDate(2013, 11, 25);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request5.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        markMMTsAsExecuted(company);

        DataLoadServices.setPSPDate(2013, 12, 3);
        sendPaymentsAndSimulateSuccess();

        DataLoadServices.setPSPDate(2013, 12, 11);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request6.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        markMMTsAsExecuted(company);

        DataLoadServices.setPSPDate(2013, 12, 17);
        sendPaymentsAndSimulateSuccess();

        DataLoadServices.setPSPDate(2013, 12, 27);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request7.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request8.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request9.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        markMMTsAsExecuted(company);

        DataLoadServices.setPSPDate(2014, 1, 3);
        sendPaymentsAndSimulateSuccess();

        DataLoadServices.setPSPDate(2014, 1, 13);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request10.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        markMMTsAsExecuted(company);

        DataLoadServices.setPSPDate(2014, 1, 22);
        sendPaymentsAndSimulateSuccess();

        DataLoadServices.setPSPDate(2014, 1, 29);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request11.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request12.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        DataLoadServices.setPSPDate(2014, 2, 03);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request13.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request14.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);



        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request15.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);


        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request16.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        completeERTaxDebit(psid);
        sendPaymentsAndSimulateSuccess();
        DataLoadServices.setPSPDate(2014, 2, 4);
        SpcfDecimal atrAmount= LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(company,LedgerAccountCode.AgencyTaxRefund);
        junit.framework.Assert.assertEquals("ATR amount", SpcfMoney.ZERO, atrAmount);
        SpcfDecimal erpAmount= LedgerAccount.getLedgerAccountBalanceIncludingPayrollInMemory(company,LedgerAccountCode.ERPayable);
        junit.framework.Assert.assertEquals("ERP amount", SpcfMoney.ZERO, erpAmount);
    }

    @Test
    public void balf9000Error() throws Exception {

        String psid = "632000034";
        DataLoadServices.setPSPDate(2013, 10, 10);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2013, 1, 1));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.addEEs(company, 3, false, true) ;
        }
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);


        DataLoadServices.setPSPDate(2013, 10, 10);
        DataLoadServices.enrollEFTPS(company);
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balance_file_9000.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
    }

    @Test
    public void testCAPayment() throws Exception {

        String psid = "606107658";
        DataLoadServices.setPSPDate(2013, 10, 10);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);

        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2013, 1, 1));
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit, ServiceCode.WorkersComp, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        DataLoadServices.enrollEFTPS(company);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110922133500");
        PayrollServices.commitUnitOfWork();
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request4_CA.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110922150000");
        PayrollServices.commitUnitOfWork();
        sendPaymentsAndSimulateSuccess();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110922171500");
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffload();
        runJobThesedays(2011, 9, 23, 30);
        runJobThesedays(2011, 10, 26, 29);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150304050700");
        PayrollServices.commitUnitOfWork();
        BatchJobManager.runJob(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.Commands.achtrans.name(), "20150304");
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150304133000");
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150218120700");
        PayrollServices.commitUnitOfWork();
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request5_CA.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        runJobThesedays(2015, 02, 18, 25);


        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150304140700");
        PayrollServices.commitUnitOfWork();
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/balance_file1.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150304150000");
        PayrollServices.commitUnitOfWork();
        sendPaymentsAndSimulateSuccess();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150304171500");
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffload();

        runJobThesedays(2015, 3, 5, 17);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150318050700");
        PayrollServices.commitUnitOfWork();
        BatchJobManager.runJob(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.Commands.achtrans.name(), "20150304");
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150318133000");
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150318135100");
        PayrollServices.commitUnitOfWork();
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request1_CA.xml"));
        String response = null;
        response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150318140700");
        PayrollServices.commitUnitOfWork();
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request6_CA.xml"));
        response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false);

        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request7_CA.xml"));
        response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150318140800");
        PayrollServices.commitUnitOfWork();
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request8_CA.xml"));
        response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150318140900");
        PayrollServices.commitUnitOfWork();
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request9_CA.xml"));
        response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150318141000");
        PayrollServices.commitUnitOfWork();
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request2_CA.xml"));
        response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150318142500");
        PayrollServices.commitUnitOfWork();
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request3_CA.xml"));
        PayrollServices.beginUnitOfWork();
        ProcessResult result = PayrollServices.companyManager.addOnHoldReason(SourceSystemCode.QBDT, psid,
                                                                              ServiceSubStatusCode.AchRejectR1R9);
        PayrollServices.commitUnitOfWork();
        response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, false);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX ofxResponseObj = OFXManager.ofxResponseToJava(response, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        Assert.assertEquals("Error Code is not same ", "101", ofxResponseObj.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getCODE());
        DataLoadServices.removeCompanyOnHoldReasons(company);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/Request10_CA.xml"));
        response = QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150318150000");
        PayrollServices.commitUnitOfWork();
        sendPaymentsAndSimulateSuccess();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20150318171500");
        PayrollServices.commitUnitOfWork();
        DataLoadServices.runOffload();
        runJobThesedays(2015, 3, 19, 25);


    }
    static void runJobThesedays(int year,int month,int start,int end){
        String startYearMonthString = year+""+ (month > 9 ? month+"": "0"+month);
        String     startDayString= "";
        while(start <= end){
            startDayString =  startYearMonthString+(start > 9 ? start+"": "0"+start);
            // startDayString =  "201503"+ startDayString;
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(startDayString+"050700");
            PayrollServices.commitUnitOfWork();
            BatchJobManager.runJob(BatchJobType.NightlyBatchJobs, NightlyBatchJobsProcessor.Commands.achtrans.name(),startDayString);
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(startDayString+"133000");
            PayrollServices.commitUnitOfWork();
            DataLoadServices.runOffloadTaxPayments(PSPDate.getPSPTime());
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(startDayString+"150000");
            PayrollServices.commitUnitOfWork();
            sendPaymentsAndSimulateSuccess();
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(startDayString+"171500");
            PayrollServices.commitUnitOfWork();
            DataLoadServices.runOffload();
            start++;
        }

    }

    private void completeERTaxDebit(String psid) {

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> erTaxDebits =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, psid,
                                                               TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Executed);
        for (FinancialTransaction financialTransaction : erTaxDebits) {
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Completed);
        }
        PayrollServices.commitUnitOfWork();
    }

    private static void sendPaymentsAndSimulateSuccess() {
        BatchJobManager.runJob(BatchJobType.EftpsPayment);

        PayrollServices.beginUnitOfWork();
        //Simulate that sent payments are acknowledged
        Expression<MoneyMovementTransaction> query = new Query<MoneyMovementTransaction>()
                .Where(MoneyMovementTransaction.TaxPaymentStatus().in(TaxPaymentStatus.SentToAgency)
                                               .And(MoneyMovementTransaction.Status().equalTo(PaymentStatus.Executed))
                                               .And((MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.EFTPS)).Or(MoneyMovementTransaction.MoneyMovementPaymentMethod().equalTo(PaymentMethod.ACHCredit))));

        DomainEntitySet<MoneyMovementTransaction> mmts = Application.find(MoneyMovementTransaction.class, query);

        for (MoneyMovementTransaction mmt : mmts) {
            mmt.setTaxPaymentStatus(TaxPaymentStatus.AcknowledgedByAgency);
            Application.save(mmt);
        }
        PayrollServices.commitUnitOfWork();
    }



    private static void markMMTsAsExecuted(Company pCompany) {
        Application.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, new Query<MoneyMovementTransaction>().Where(
                MoneyMovementTransaction.Status().equalTo(PaymentStatus.Created)
        ));

        for(MoneyMovementTransaction mmt: moneyMovementTransactions){
            mmt.setStatus(PaymentStatus.Executed);
        }
        Application.commitUnitOfWork();

    }

    @Ignore
    @Test
    public void returnFileParserExceptionTest() throws IOException {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        String psid = "105868803";
        DataLoadServices.setPSPDate(2014, 8, 20);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.activateDDService(company);
            DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        }
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_tax_dd_1.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));
        DataLoadServices.setPSPDate(2014, 8, 27);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_tax_dd_2.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));
        company = DataLoadServices.refreshCompany(company);
        long count = company.getPayrollCount();
        junit.framework.Assert.assertEquals("Payroll count is not matching", 6, count);
        // offload impounds
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20101027000000");
        Application.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        // return the EmployerTaxDebit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110210050000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxDebit);
        TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);

        Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName(null);
        transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
        transactionReturnBatch = Application.save(transactionReturnBatch);


        for (FinancialTransaction financialTx : finTxs) {
            TransactionReturn transactionReturn = new TransactionReturn();
            //invalid return code
            transactionReturn.setBankReturnCd("C08");
            transactionReturn.setBankReturnDescription("Insufficient funds");
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setBankReturnTraceNumber(12345678);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
            transactionReturn.setCompany(financialTx.getCompany());
            Application.save(transactionReturn);
        }

        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        transactionReturnBatch = Application.save(transactionReturnBatch);

        SpcfUniqueId batchId = transactionReturnBatch.getId();
        Application.commitUnitOfWork();


        MailSenderHolder.setFailMailSenderHolderFlag(Boolean.TRUE);
        System.setProperty("psp.test.email", "true");

        // Process TransactionReturns associated with the TransactionReturnBatch
        ReturnFileParser returnsProcessor = new ReturnFileParser();
        try {
            returnsProcessor.processTransactionReturns(batchId);
            Assert.assertTrue("RuntimeException was not thrown", Boolean.FALSE);
        } catch (RuntimeException ex) {
            Assert.assertEquals("Exception Message is not same", "Transaction Return processing encountered a RunTimeException while sending the details of the failed records..  Please correct and rerun the returns job.", ex.getMessage());

        }

        MailSenderHolder.setFailMailSenderHolderFlag(Boolean.FALSE);
        System.setProperty("psp.test.email", "false");
    }
    @Test
    public void testAssistedPayrollCountInBannerAndAccountFile() throws IOException {
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));
        String psid = "105868803";
        DataLoadServices.setPSPDate(2014, 8, 20);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.DirectDeposit,ServiceCode.Tax);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
            DataLoadServices.activateDDService(company);
            DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        }
        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_tax_dd_1.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));
        DataLoadServices.setPSPDate(2014, 8, 27);
        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_tax_dd_2.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));
        company =DataLoadServices.refreshCompany(company);
        long count= company.getPayrollCount();
        junit.framework.Assert.assertEquals("Payroll count is not matching", 6, count);
        // offload impounds
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20101027000000");
        Application.commitUnitOfWork();
        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);


        // return the EmployerTaxDebit
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110210050000");
        Application.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        TransactionType txnType = Application.findById(TransactionType.class, TransactionTypeCode.EmployerTaxDebit);
        TransactionState txnState = Application.findById(TransactionState.class, TransactionStateCode.Executed);

        Criterion<FinancialTransaction> where = FinancialTransaction.Company().equalTo(company)
                                                                    .And(FinancialTransaction.TransactionType().equalTo(txnType))
                                                                    .And(FinancialTransaction.CurrentTransactionState().equalTo(txnState));
        DomainEntitySet<FinancialTransaction> finTxs = Application.find(FinancialTransaction.class, where);

        TransactionReturnBatch transactionReturnBatch = new TransactionReturnBatch();
        transactionReturnBatch.setACHReturnFileName(null);
        transactionReturnBatch.setReturnDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusEffectiveDate(PSPDate.getPSPTime());
        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Received);
        transactionReturnBatch = Application.save(transactionReturnBatch);

        for (FinancialTransaction financialTx : finTxs) {
            TransactionReturn transactionReturn = new TransactionReturn();

            transactionReturn.setBankReturnCd("R01");
            transactionReturn.setBankReturnDescription("Insufficient funds");
            transactionReturn.setReturnBatch(transactionReturnBatch);
            transactionReturn.setBankReturnTraceNumber(12345678);
            transactionReturn.setReturnStatusCd(TransactionReturnStatusCode.Created);
            transactionReturn.setReturnStatusEffectiveDate(PSPDate.getPSPTime());
            transactionReturn.setMoneyMovementTransaction(financialTx.getMoneyMovementTransaction());
            transactionReturn.setCompany(financialTx.getCompany());
            Application.save(transactionReturn);
        }

        transactionReturnBatch.setStatusCd(TransactionReturnBatchStatusCode.Persisted);
        transactionReturnBatch = Application.save(transactionReturnBatch);

        SpcfUniqueId batchId = transactionReturnBatch.getId();
        Application.commitUnitOfWork();

        // Process TransactionReturns associated with the TransactionReturnBatch
        ReturnFileParser returnsProcessor = new ReturnFileParser();
        returnsProcessor.processTransactionReturns(batchId);
        /*  First test the debit returns file   */
        PayrollServices.beginUnitOfWork();
        List<File> rejectFiles = AchReturnAccountingFile.createFile(batchId);
        PayrollServices.commitUnitOfWork();
        junit.framework.Assert.assertEquals("Incorrect number of return files", 1, rejectFiles.size());
        // compare the new reject returns file with the expected result
        assertTrue("ACH returns accounting files do not match", hasPayrollCountInFileMatching(rejectFiles.get(0),count));
        DataLoadServices.reinitialize();;
    }

    public static boolean hasPayrollCountInFileMatching(File pLhsFile,long count) {
        return compareCSVFileFieldValues(pLhsFile,count,57);
    }
    /**
     *
     * @param pCSVFile
     * @param count
     * @param fieldIndexToCompare
     * @return
     * fieldIndexToCompare will start with 0
     */
    public static boolean compareCSVFileFieldValues(File pCSVFile,long count,int fieldIndexToCompare) {
        try {
            BufferedReader reader;
            boolean countMatch =false;
            StringWriter lhsContent = new StringWriter();
            reader = new BufferedReader(new FileReader(pCSVFile));
            try {
                int i=0;
                while (reader.ready()) {
                    if( i++  == 0){   //ignore first line as it is header
                        reader.readLine();
                        continue;
                    }
                    lhsContent.write(reader.readLine());
                }
            } finally {
                reader.close();
            }

            String fileContents[] = lhsContent.toString().split(",");
            String fieldValue = fileContents[fieldIndexToCompare];
            String expectedValue= "\""+String.valueOf(count)+"\"";
            if (expectedValue.equals(fieldValue)) {
                countMatch=true;
            }

            return countMatch;
        } catch (Throwable t) {
            throw new RuntimeException(String.format("Error comparing file (: %s, count: %s)", pCSVFile.getPath(), String.valueOf(count)), t);
        }
    }
    @Test
    public void paystubStateTaxId() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        Application.beginUnitOfWork();

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_statetaxid.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));


        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 1", 1, paystubs.size());

        DomainEntitySet<PstubStateTaxInfo> pstubStateTaxes = Application.find(PstubStateTaxInfo.class);
        assertEquals("total number of statetax should be 1", 1, pstubStateTaxes.size());

        Application.rollbackUnitOfWork();


        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_20.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        Application.beginUnitOfWork();
        paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 3", 3, paystubs.size());

        pstubStateTaxes = Application.find(PstubStateTaxInfo.class);
        assertEquals("total number of statetax should be 1", 1, pstubStateTaxes.size());

        Application.rollbackUnitOfWork();
    }

    @Test
    public void paystubStateTaxIdOFXWithoutHash() throws Exception {
        String psid = "100093352";
        DataLoadServices.setPSPDate(2013, 2, 7);
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        if (company == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, psid, false, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        }
        Application.beginUnitOfWork();

        String OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_statetaxid_without_hash.xml"));
        System.out.println(QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true));


        DomainEntitySet<Paystub> paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 1", 1, paystubs.size());

        DomainEntitySet<PstubStateTaxInfo> pstubStateTaxes = Application.find(PstubStateTaxInfo.class);
        assertEquals("total number of statetax should be 1", 1, pstubStateTaxes.size());

        Application.rollbackUnitOfWork();


        OFX = AssistedUnprocessedRequestTests.readFile(Application.findFileOnClassPath("resources/ofx/paystub_diy_20.xml"));
        QBDTTestHelper.submitQBDTRequestStringResponse(OFX, true);
        Application.beginUnitOfWork();
        paystubs = Application.find(Paystub.class);
        assertEquals("total number of paystubs should be 3", 3, paystubs.size());

        pstubStateTaxes = Application.find(PstubStateTaxInfo.class);
        assertEquals("total number of statetax should be 1", 1, pstubStateTaxes.size());

        Application.rollbackUnitOfWork();
    }

}
