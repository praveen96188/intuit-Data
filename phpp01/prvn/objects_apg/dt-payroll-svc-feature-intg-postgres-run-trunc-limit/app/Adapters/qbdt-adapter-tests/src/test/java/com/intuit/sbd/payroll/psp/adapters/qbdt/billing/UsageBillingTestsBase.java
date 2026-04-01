package com.intuit.sbd.payroll.psp.adapters.qbdt.billing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EMSBSToBRMDataSyncProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.PSPToEMSBSDataSyncProcessor;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 7/31/12
 * Time: 11:41 AM
 * To change this template use File | Settings | File Templates.
 */
public class UsageBillingTestsBase {
    public static final String mDDPSID = CompanyQB1DataLoader.COMPANY_PSID;
    public static final String mCloudPSID = "100023560";
    public static final String mCompanyPassword = "test1234";
    public static final String mLicenceIdDD = "590285459983251";
    public static final String mLicenceIdCloud = "590285459983250";
    public static final String mEoc = "389857";

    protected Company company;
    protected String ein;
    protected String subscriptionNumber;

    @BeforeClass
    public static void runBefore() {
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.truncateTables();

        PayrollServicesTest.beforeEachTest();
        DataLoadServices.resetAllPaymentTemplateSupportDates();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN, "0");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2011, 7, 1);
        setupDDCompany();
        setupCloudCompany();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    protected static void updateBDOMForCompany(Company pCompany, int pBDOM) {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(pCompany.getSourceCompanyId(), SourceSystemCode.QBDT);

        Entitlement entitlement = company.getActivePrimaryEntitlementUnit().getEntitlement();
        entitlement.setBillingDayOfMonth(pBDOM);
        entitlement.setSubscriptionStartDate(entitlement.getCreatedDate());
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();
    }


    public static Company setupDDCompany() {
        return setupDDCompany(mDDPSID, mLicenceIdDD, mEoc);
    }

    public static Company setupDDCompany(String psid, String licenseId, String eoc) {
        return setupDDCompany(mDDPSID, null, mLicenceIdDD, mEoc);
    }

    public static Company setupDDCompany(String psid, String pEin, String licenseId, String eoc) {
        DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, pEin, true, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        EntitlementUnit eu = company.getActivePrimaryEntitlementUnit();
        PayrollServices.rollbackUnitOfWork();
        DataLoadServices.deactivateEntitlementUnit(eu);
        DataLoadServices.addEntitlementUnit(company, licenseId, eoc, EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, null);

        updateBDOMForCompany(company, 15);

        // update DD limit
        Application.beginUnitOfWork();
        SpcfMoney compOverrideAmount = new SpcfMoney("500.00");
        SpcfMoney empOverrideAmount = new SpcfMoney("1000.00");
        PayrollServices.companyManager.updateDDLimits(SourceSystemCode.QBDT, mDDPSID, compOverrideAmount, empOverrideAmount);
        Application.commitUnitOfWork();

        return company;
    }

    public static Company setupCloudCompany() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, mCloudPSID, false, ServiceCode.Cloud);

        DataLoadServices.addEntitlementUnit(company, mLicenceIdCloud, mEoc, EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_LOWBASE, null);

        PayrollServices.beginUnitOfWork();
        ProcessResult<HashMap<String, String>> procResult =
                PayrollServices.subscriptionManager.createCompanyPIN(SourceSystemCode.QBDT, mCloudPSID, mCompanyPassword);
        PayrollServicesTest.assertSuccess("createPINResult", procResult);
        PayrollServices.commitUnitOfWork();

        updateBDOMForCompany(company, 15);

        return company;
    }

    public static int getUsageOnBill(String psid, int year, int month, int day) {
        try {
            Application.beginUnitOfWork();
            return getBill(psid, year, month, day).getUsageCount();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return 0;
        } finally {
            Application.rollbackUnitOfWork();
        }

    }

    public static List<Object[]> getBillDetailsForCompany(String psid, int year, int month, int day) {
         List<Object[]> results = null;
          try {
              Application.beginUnitOfWork();
              Bill bill = getBill(psid, year, month, day);
              results = Bill.findBillDetails(bill.getId());
          } catch (RuntimeException e) {
              e.printStackTrace();
          } finally {
              Application.rollbackUnitOfWork();
          }

          return results;
      }

     public static List<Object[]> getBillDetailsForLicense(String psid, int year, int month, int day, String pLicenseId) {
         List<Object[]> results = null;
          try {
              Application.beginUnitOfWork();
              results = Bill.findBillDetailsByLicenceNumber(SpcfCalendar.createInstance(year, month, day, SpcfTimeZone.getLocalTimeZone()), pLicenseId);
          } catch (RuntimeException e) {
              e.printStackTrace();
          } finally {
              Application.rollbackUnitOfWork();
          }
          return results;
      }


    public static Bill getBill(String psid, int year, int month, int day) {
        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(psid).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companyUsages.size());
        CompanyUsage companyUsage = companyUsages.getFirst();
        return Bill.findBill(companyUsage, SpcfCalendar.createInstance(year, month, day, SpcfTimeZone.getLocalTimeZone()));
    }

    public Bill findBill(CompanyUsage companyUsage, SpcfCalendar paycheckDate){
        SpcfCalendar lastDayOfCurrentMonth = CalendarUtils.getLastDayOfMonth(paycheckDate);
        SpcfCalendar billPeriodStart = CalendarUtils.dayOfMonthAfter(lastDayOfCurrentMonth, 1);
        SpcfCalendar billPeriodEnd = CalendarUtils.getLastDayOfMonth(billPeriodStart);
        Criterion<Bill> billCriterion = Bill.CompanyUsage().equalTo(companyUsage).And(Bill.BillDate().between(billPeriodStart, billPeriodEnd));
        Bill bill = assertOne(Application.find(Bill.class, billCriterion));
        return bill;
    }

    private static int jobId = 1;

    public void runPSPToEMSBSDataSyncProcessor() {

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, Integer.toString(++jobId), "");
        aPSPToEMSBSDataSyncProcessor.executeJob();
    }

    public void runEMSBSToBRMDataSyncProcessor() {
        runEMSBSToBRMDataSyncProcessor(null);
    }

    public void runEMSBSToBRMDataSyncProcessor(SpcfCalendar billPeriodStartDate) {

        if(Objects.nonNull(billPeriodStartDate)){
            SpcfCalendar lastDayOfMonth = CalendarUtils.getLastDayOfMonth(billPeriodStartDate);
            CalendarUtils.clearTime(lastDayOfMonth);
            DataLoadServices.setPSPDate(lastDayOfMonth);
        }

        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, Integer.toString(++jobId), "");
        aEMSBSToBRMDataSyncProcessor.executeJob();
    }

    public void assertBillClosed(Company company, Entitlement entitlement) {
        assertBill(company, entitlement, true);
    }

    public void assertBillClosed(Company company, Entitlement entitlement, SpcfCalendar billPeriodStartDate) {
        assertBill(company, entitlement, billPeriodStartDate, true);
    }

    public void assertBillOpen(Company company, Entitlement entitlement) {
        assertBill(company, entitlement, false);
    }

    public void assertBillOpen(Company company, Entitlement entitlement, SpcfCalendar billPeriodStartDate) {
        assertBill(company, entitlement, billPeriodStartDate, false);
    }

    public void assertBill(Company company, Entitlement entitlement, boolean closed) {
        Application.beginUnitOfWork();
        CompanyUsage usage = CompanyUsage.findCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
        Bill bill = assertOne(Application.find(Bill.class, Bill.CompanyUsage().equalTo(usage)));
        assertEquals(bill.getClosed(), closed);
        Application.rollbackUnitOfWork();
    }

    public void assertBill(Company company, Entitlement entitlement, SpcfCalendar billPeriodStartDate, boolean closed) {
        Application.beginUnitOfWork();
        CompanyUsage usage = CompanyUsage.findCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
        Bill bill = findBill(usage, billPeriodStartDate);
        assertEquals(bill.getClosed(), closed);
        Application.rollbackUnitOfWork();
    }

    public void assertBillCounts(Company company, Entitlement entitlement, int usageCount, int syncedCount) {
        Application.beginUnitOfWork();
        CompanyUsage usage = CompanyUsage.findCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
        Bill bill = assertOne(Application.find(Bill.class, Bill.CompanyUsage().equalTo(usage)));
        assertEquals(usageCount, bill.getUsageCount());
        assertEquals(syncedCount, bill.getSynchedCount());
        Application.rollbackUnitOfWork();
    }

    public void assertBillCounts(Company company, Entitlement entitlement, SpcfCalendar billPeriodStartDate, int usageCount, int syncedCount) {
        Application.beginUnitOfWork();
        CompanyUsage usage = CompanyUsage.findCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
        Bill bill = findBill(usage, billPeriodStartDate);
        assertEquals(usageCount, bill.getUsageCount());
        assertEquals(syncedCount, bill.getSynchedCount());
        Application.rollbackUnitOfWork();
    }

    public void assertNoBill(Company company, Entitlement entitlement) {
        Application.beginUnitOfWork();
        CompanyUsage usage = CompanyUsage.findCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlement.getLicenseNumber(), entitlement.getEntitlementOfferingCode());
        assertEquals(0, Application.find(Bill.class, Bill.CompanyUsage().equalTo(usage)).size());
        Application.rollbackUnitOfWork();

    }
}
