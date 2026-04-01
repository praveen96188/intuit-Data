package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.ems.payroll.psp.gateway.brm.BRMAssistedUsageFileUploader;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementInfoDTO;
import com.intuit.ems.payroll.psp.gateways.ers.EntitlementUnitInfoDTO;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.AssistedUsage.SyncAssistedUsageData;
import com.intuit.sbd.payroll.psp.batchjobs.AssistedUsageReport.AssistedUsageToBRMDataSyncFileGenerator;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EntitlementProcessor;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTRNRQ;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BatchJobType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbg.psp.dd.limitcheck.DDRestClient;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.apache.commons.lang.StringUtils;
import org.junit.*;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 7/10/12
 * Time: 12:56 PM
 */
public class AssistedBundleBillingTests {
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

    public void cleanDirectory(String folderPath) {
        File folder = new File(folderPath);
        // Get all files
        File[] files = folder.listFiles();
        // Delete all files
        for (File file : files) {
            file.delete();
        }
    }

    public int getFileCountInDirectory(String directoryName) {
        File workDir = new File(directoryName);
        File[] filesToBeUploaded = workDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {

                return name.endsWith(BRMAssistedUsageFileUploader.FILENAME_EXT);
            }
        });
        return filesToBeUploaded.length;
    }
    @Test
    public void testAssistedBundleDiamondPayrollRun() {
        DataLoadServices.setPSPDate(2012, 1, 10);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);

        Application.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        EntitlementUnit entitlementUnit = company1.getActivePrimaryEntitlementUnit();
        Entitlement et = entitlementUnit.getEntitlement();
        EntitlementCode ec = EntitlementCode.findEntitlementCode("1400076");
        et.setEntitlementCode(ec);
        Application.save(et);
        Application.commitUnitOfWork();

        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        // add 2 payrolls for the same month
        DataLoadServices.setPSPDate(2012, 1, 15);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertEquals(1, ipayrollrs.getIPAYROLLTX().size());

        SpcfCalendar todaysDate =  PSPDate.getPSPTime();
        SpcfCalendar startDate = CalendarUtils.getFirstDayOfMonth(todaysDate);
        SpcfCalendar endDate = CalendarUtils.getLastDayOfMonth(startDate);

        int oldFileCount = getFileCountInDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);
        new SyncAssistedUsageData().sync();
        Application.beginUnitOfWork();
        AsstBundleCompUsage compUsage =  AsstBundleCompUsage.findAssistedBundleCompanyUsage(psid, SourceSystemCode.QBDT, et.getLicenseNumber(), et.getEntitlementOfferingCode());
        Set<SpcfUniqueId> bills = AssistedBundleBill.findOpenBillsDuring(startDate, endDate);
        Application.commitUnitOfWork();
        assertEquals("Assisted Company usage doesnot exist", compUsage != null, true);
        assertEquals("Assisted Bundle bills doesnot exist", bills.size() != 0, true);
        new AssistedUsageToBRMDataSyncFileGenerator().generate();
        int newFileCount = getFileCountInDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);

        assertEquals("Assisted Bundle bills doesnot exist", newFileCount == oldFileCount + 1, true);
    }

    @Test
    public void testAssistedBundleDiamondPayrollRunNextMonth() {
        DataLoadServices.setPSPDate(2012, 1, 10);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);

        Application.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        EntitlementUnit entitlementUnit = company1.getActivePrimaryEntitlementUnit();
        Entitlement et = entitlementUnit.getEntitlement();
        EntitlementCode ec = EntitlementCode.findEntitlementCode("1400076");
        et.setEntitlementCode(ec);
        Application.save(et);
        Application.commitUnitOfWork();

        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        // add 2 payrolls for the same month
        DataLoadServices.setPSPDate(2012, 1, 15);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertEquals(1, ipayrollrs.getIPAYROLLTX().size());

        DataLoadServices.setPSPDate(2012, 2, 10);
        new SyncAssistedUsageData().sync();
        SpcfCalendar todaysDate =  PSPDate.getPSPTime();
        SpcfCalendar startDate = CalendarUtils.getFirstDayOfMonth(todaysDate);
        SpcfCalendar endDate = CalendarUtils.getLastDayOfMonth(startDate);

        Application.beginUnitOfWork();
        Set<SpcfUniqueId> bills = AssistedBundleBill.findOpenBillsDuring(startDate, endDate);
        Application.commitUnitOfWork();
        assertEquals("Assisted bundle bill exists", bills.size() == 0, true);
    }

    @Test
    public void testAssistedBundleDiamondPayrollRunTwoCompany() {
        DataLoadServices.setPSPDate(2012, 1, 10);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);

        Application.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        EntitlementUnit entitlementUnit = company1.getActivePrimaryEntitlementUnit();
        Entitlement et = entitlementUnit.getEntitlement();
        EntitlementCode ec = EntitlementCode.findEntitlementCode("1400076");
        et.setEntitlementCode(ec);
        Application.save(et);
        Application.commitUnitOfWork();

        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        // add 2 payrolls for the same month
        DataLoadServices.setPSPDate(2012, 1, 15);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertEquals(1, ipayrollrs.getIPAYROLLTX().size());

        DataLoadServices.setPSPDate(2012, 1, 10);
        String newPsid = "223456789";
        Company newCompany = DataLoadServices.newCompany(SourceSystemCode.QBDT, newPsid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(newCompany);
        OFX newOfx = OFXRequestGenerator.generateBalanceFile(newPsid, false);
        QBDTTestHelper.submitQBDTRequest(newOfx);

        newCompany = DataLoadServices.refreshCompany(newCompany);

        Application.beginUnitOfWork();
        Company newCompany1 = Company.findCompany(newPsid, SourceSystemCode.QBDT);
        EntitlementUnit newEntitlementUnit = newCompany1.getActivePrimaryEntitlementUnit();
        Entitlement newEt = newEntitlementUnit.getEntitlement();
        EntitlementCode newEc = EntitlementCode.findEntitlementCode("1400076");
        newEt.setEntitlementCode(newEc);
        Application.save(newEt);
        Application.commitUnitOfWork();

        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        // add 2 payrolls for the same month
        DataLoadServices.setPSPDate(2012, 1, 15);
        List<IPAYROLLRUN> newPayrollRuns = new ArrayList<IPAYROLLRUN>();
        newPayrollRuns.add(OFXRequestGenerator.generatePayrollRun(newOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                newOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                false));

        OFX newPayrollOfx = new OFX();
        newPayrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(newPsid, DataLoadServices.PIN));
        IPAYROLLTRNRQ newipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                newPayrollRuns);
        newPayrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(newCompany, true, newipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX newResponse = QBDTTestHelper.submitQBDTRequest(newPayrollOfx);
        IPAYROLLRS newipayrollrs = newResponse.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertEquals(1, newipayrollrs.getIPAYROLLTX().size());

        new SyncAssistedUsageData().sync();
        SpcfCalendar todaysDate =  PSPDate.getPSPTime();
        SpcfCalendar startDate = CalendarUtils.getFirstDayOfMonth(todaysDate);
        SpcfCalendar endDate = CalendarUtils.getLastDayOfMonth(startDate);

        Application.beginUnitOfWork();
        Set<SpcfUniqueId> bills = AssistedBundleBill.findOpenBillsDuring(startDate, endDate);
        Application.commitUnitOfWork();
        assertEquals("Assisted bundle bill exists", bills.size() == 2, true);
    }
    @Test
    public void testAssistedBundleReportGenerateProcess() {
        DataLoadServices.setPSPDate(2012, 1, 10);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);

        Application.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        EntitlementUnit entitlementUnit = company1.getActivePrimaryEntitlementUnit();
        Entitlement et = entitlementUnit.getEntitlement();
        EntitlementCode ec = EntitlementCode.findEntitlementCode("1400076");
        et.setEntitlementCode(ec);
        Application.save(et);
        Application.commitUnitOfWork();

        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        // add 2 payrolls for the same month
        DataLoadServices.setPSPDate(2012, 1, 15);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertEquals(1, ipayrollrs.getIPAYROLLTX().size());

        SpcfCalendar todaysDate =  PSPDate.getPSPTime();
        SpcfCalendar startDate = CalendarUtils.getFirstDayOfMonth(todaysDate);
        SpcfCalendar endDate = CalendarUtils.getLastDayOfMonth(startDate);

        cleanDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);
        int oldFileCount = getFileCountInDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);
        new SyncAssistedUsageData().sync();
        Application.beginUnitOfWork();
        AsstBundleCompUsage compUsage =  AsstBundleCompUsage.findAssistedBundleCompanyUsage(psid, SourceSystemCode.QBDT, et.getLicenseNumber(), et.getEntitlementOfferingCode());
        Set<SpcfUniqueId> bills = AssistedBundleBill.findOpenBillsDuring(startDate, endDate);
        Application.commitUnitOfWork();
        assertEquals("Assisted Company usage doesnot exist", compUsage != null, true);
        assertEquals("Assisted Bundle bills doesnot exist", bills.size() != 0, true);
        new AssistedUsageToBRMDataSyncFileGenerator().generate();
        int newFileCount = getFileCountInDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);

        assertEquals("Assisted Bundle Report file not generated", newFileCount == oldFileCount + 1, true);
    }
    @Test
    public void testAssistedBundleReportGenerateProcessNoFile() {
        DataLoadServices.setPSPDate(2012, 1, 10);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);

        Application.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        EntitlementUnit entitlementUnit = company1.getActivePrimaryEntitlementUnit();
        Entitlement et = entitlementUnit.getEntitlement();
        EntitlementCode ec = EntitlementCode.findEntitlementCode("1400076");
        et.setEntitlementCode(ec);
        Application.save(et);
        Application.commitUnitOfWork();

        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        // add 2 payrolls for the same month
        DataLoadServices.setPSPDate(2012, 1, 15);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertEquals(1, ipayrollrs.getIPAYROLLTX().size());

        cleanDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);
        DataLoadServices.setPSPDate(2012, 2, 10);
        int oldFileCount = getFileCountInDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);
        new SyncAssistedUsageData().sync();
        SpcfCalendar todaysDate =  PSPDate.getPSPTime();
        SpcfCalendar startDate = CalendarUtils.getFirstDayOfMonth(todaysDate);
        SpcfCalendar endDate = CalendarUtils.getLastDayOfMonth(startDate);

        Application.beginUnitOfWork();
        Set<SpcfUniqueId> bills = AssistedBundleBill.findOpenBillsDuring(startDate, endDate);
        Application.commitUnitOfWork();
        assertEquals("Assisted bundle bill exists", bills.size() == 0, true);
        new AssistedUsageToBRMDataSyncFileGenerator().generate();
        int newFileCount = getFileCountInDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);
        assertEquals("Assisted Bundle bills  exist", newFileCount == oldFileCount, true);
    }
    @Test
    public void testAssistedBundleReportUploadProcess() {
        DataLoadServices.setPSPDate(2012, 1, 10);
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);

        Application.beginUnitOfWork();
        Company company1 = Company.findCompany(psid, SourceSystemCode.QBDT);
        EntitlementUnit entitlementUnit = company1.getActivePrimaryEntitlementUnit();
        Entitlement et = entitlementUnit.getEntitlement();
        EntitlementCode ec = EntitlementCode.findEntitlementCode("1400076");
        et.setEntitlementCode(ec);
        Application.save(et);
        Application.commitUnitOfWork();

        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        // add 2 payrolls for the same month
        DataLoadServices.setPSPDate(2012, 1, 15);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                new Date("01/15/2012"),
                false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertEquals(1, ipayrollrs.getIPAYROLLTX().size());

        SpcfCalendar todaysDate =  PSPDate.getPSPTime();
        SpcfCalendar startDate = CalendarUtils.getFirstDayOfMonth(todaysDate);
        SpcfCalendar endDate = CalendarUtils.getLastDayOfMonth(startDate);

        cleanDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);
        int oldFileCount = getFileCountInDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);
        int oldFileArchiveCount = getFileCountInDirectory(BRMAssistedUsageFileUploader.PSP_S3_ARCHIVE_DIR);
        int oldFileErrorCount = getFileCountInDirectory(BRMAssistedUsageFileUploader.PSP_S3_ERROR_DIR);
        new SyncAssistedUsageData().sync();
        Application.beginUnitOfWork();
        AsstBundleCompUsage compUsage =  AsstBundleCompUsage.findAssistedBundleCompanyUsage(psid, SourceSystemCode.QBDT, et.getLicenseNumber(), et.getEntitlementOfferingCode());
        Set<SpcfUniqueId> bills = AssistedBundleBill.findOpenBillsDuring(startDate, endDate);
        Application.commitUnitOfWork();
        assertEquals("Assisted Company usage doesnot exist", compUsage != null, true);
        assertEquals("Assisted Bundle bills doesnot exist", bills.size() != 0, true);
        new AssistedUsageToBRMDataSyncFileGenerator().generate();
        int newFileCount = getFileCountInDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);

        assertEquals("Assisted Bundle Report file not generated", newFileCount == oldFileCount + 1, true);
        try {
            new BRMAssistedUsageFileUploader().upload();
        } catch (Exception ex) {
        }

        /*int newFileCountAfterUpload = getFileCountInDirectory(BRMAssistedUsageFileUploader.LOCAL_WORK_DIR);
        int newArchiveFileCountAfterUpload = getFileCountInDirectory(BRMAssistedUsageFileUploader.PSP_S3_ARCHIVE_DIR);
        int newErrorFileCountAfterUpload = getFileCountInDirectory(BRMAssistedUsageFileUploader.PSP_S3_ERROR_DIR);
        assertEquals("Assisted Bundle Report file not generated in stage directory", newFileCountAfterUpload == oldFileCount, true);
        if (!Application.isAWSEnvironment()) {
            assertEquals("Assisted Bundle Report file not copied in error directory", oldFileErrorCount + 1 == newErrorFileCountAfterUpload, true);
        } else {
            assertEquals("Assisted Bundle Report file not copied in archive directory", oldFileArchiveCount + 1 == newArchiveFileCountAfterUpload, true);
        }*/
    }
    @Test
    public void testAssistedBundle3EINSupport() {
        String maxEUsAllowedForDiamondAssisted = ConfigurationManager.getSettingValue(ConfigurationModule.SAPAdapter, "diamond-assisted");

        assertEquals("Failed to read max config",StringUtils.isNumeric(maxEUsAllowedForDiamondAssisted), true);
        int mMaxNumberOfRecords=Integer.valueOf(maxEUsAllowedForDiamondAssisted);
        String licenseNumber = "lic1";
        String eoc = "eoc1";
        List<Company> companies = new ArrayList<Company>(10);
        EntitlementInfoDTO entitlementInfoDTO = new EntitlementInfoDTO();
        entitlementInfoDTO.setEntitlementState(EntitlementStateCode.Enabled);
        for (int i = 0; i<mMaxNumberOfRecords; i++) {
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Tax);
            companies.add(company);
            DataLoadServices.addAssistedBundleEntitlementUnit(company, licenseNumber, eoc);
            if (!entitlementInfoDTO.getEntitlementUnits().containsKey(company.getFedTaxId())) {
                EntitlementUnitInfoDTO entitlementUnitInfoDTO = new EntitlementUnitInfoDTO();
                entitlementUnitInfoDTO.setFedTaxId(company.getFedTaxId());
                entitlementUnitInfoDTO.setEntitlementUnitStatusCode(EntitlementUnitStatusCode.Activated);
                entitlementInfoDTO.getEntitlementUnits().put(company.getFedTaxId(), entitlementUnitInfoDTO);
            }
        }
        EntitlementProcessor entitlementProcessor =
                new EntitlementProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EntitlementProcessor, UUID.randomUUID().toString(), "");

        entitlementProcessor.execute();
        PayrollServices.beginUnitOfWork();
        for (Company company : companies) {
            company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
            assertEquals("Company doesnot exist", company != null, true);
        }
        PayrollServices.rollbackUnitOfWork();
    }
}


