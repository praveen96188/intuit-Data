package com.intuit.sbd.payroll.psp.adapters.qbdt.billing;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.intuit.ems.payroll.psp.gateway.brm.BRMFileUploader;
import com.intuit.idps.domain.item.Key;
import com.intuit.idps.service.IdpsException;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXAssert;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.billing.ProcessErrorCsv;
import com.intuit.sbd.payroll.psp.batchjobs.processors.BRMUsageErrorFileProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EMSBSToBRMDataSyncProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.PSPToEMSBSDataSyncProcessor;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.IDPSFileStreamManager;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.StreamUtil;
import com.intuit.sbd.payroll.psp.common.utils.encryption.idps.fileencryption.IDPSFileReader;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.EntitlementStateCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.io.FilenameUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: Tiger Shao
 * Date: 6/27/12
 * Time: 2:50 PM
 */
public class UsageBillingTests extends UsageBillingTestsBase {

    @Test
    @Ignore
    public void ofxGenerationTest() {

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, mCompanyPassword, "123456789", "987654321", PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        System.out.println(OFXManager.javaRequestToOFX(request));
        request = new UsageOFXDataloader().createOFX(mDDPSID, mCompanyPassword, "123456789", "987654321", "100");
        System.out.println(OFXManager.javaRequestToOFX(request));
    }

    @Test
    public void deletePaycheckTest() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        // delete the paycheck now
        DataLoadServices.setPSPDate(2011, 7, 22);
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, "100");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 0, getUsageOnBill(mDDPSID, 2011, 8, 15));

        PayrollServicesTest.validateQbdtRequestInfo(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.QBDT,null, TransmissionType.UsageSend,"Sent 1 Paychecks"),PayrollServicesTest.getQbdtRequestInfo(1,0,0,1,0,0,0,0,0,0,0,0));
    }

    @Test
    public void basicHappyPathDD() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 1, getUsageOnBill(mDDPSID, 2011, 8, 15));

        List<Object[]> billDetailsForCompany = getBillDetailsForCompany(mDDPSID, 2011, 8, 15);
        List<Object[]> billDetailsForLicense = getBillDetailsForLicense(mDDPSID, 2011, 8, 15, mLicenceIdDD);
        assertNotNull(billDetailsForCompany);
        assertNotNull(billDetailsForLicense);
        assertEquals("billDetailsForCompany", 1, billDetailsForCompany.size());
        assertEquals("billDetailsForLicense", 1, billDetailsForLicense.size());

        DataLoadServices.setPSPDate(2011, 7, 31);
        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, "2", "");
        aEMSBSToBRMDataSyncProcessor.executeJob();
    }

    private static int nextPaycheckId = 100;

    private String submitUsageDataFromOFX() throws Throwable {

        return submitUsageDataFromOFX("1", "Susan", "Butchman");
    }

    private String submitUsageDataFromOFX(String empId, String firstName, String lastName) throws Throwable {

        Application.beginUnitOfWork();
        Application.refresh(company);
        String newSubscriptionNumber = company.getActivePrimaryEntitlementUnit().getEntitlement().getSubscriptionNumber();
        Application.rollbackUnitOfWork();

        String paycheckId = Integer.toString(++nextPaycheckId);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, newSubscriptionNumber, PSPDate.getPSPTime(), empId, firstName, lastName, paycheckId, "N", false, false);
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        return paycheckId;
    }

    private void voidUsageData(String paycheckId) throws Throwable {

        Application.beginUnitOfWork();
        Application.refresh(company);
        Paycheck paycheck = Paycheck.findPaycheck(company, paycheckId);
        String newSubscriptionNumber = company.getActivePrimaryEntitlementUnit().getEntitlement().getSubscriptionNumber();
        Employee sourceEmployee = paycheck.getSourceEmployee();
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, newSubscriptionNumber, paycheck.getPayrollRun().getPaycheckDate(), sourceEmployee.getSourceEmployeeId(), sourceEmployee.getFirstName(), sourceEmployee.getLastName(), paycheckId, "Y");
        Application.rollbackUnitOfWork();

        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
    }

    @Test
    //verifies PSP-2688
    public void testChangingEntitlementsDoesNotCreateDuplicateBilling() throws Throwable {

        DataLoadServices.setPSPDate(2011, 7, 10);
        submitUsageDataFromOFX();
        runPSPToEMSBSDataSyncProcessor();

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        DataLoadServices.setPSPDate(2011, 7, 15);
        EntitlementUnit newEU = DataLoadServices.addEntitlementUnit(company, "1", "2", EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, null);
        updateBDOMForCompany(company, 15);

        DataLoadServices.setPSPDate(2011, 7, 16);
        submitUsageDataFromOFX();
        runPSPToEMSBSDataSyncProcessor();

        assertBillCounts(company, entitlementUnit.getEntitlement(), 0, 0);
        assertBillCounts(company, newEU.getEntitlement(), 1, 0);

        DataLoadServices.setPSPDate(2011, 7, 31);
        runEMSBSToBRMDataSyncProcessor();

        assertBillCounts(company, entitlementUnit.getEntitlement(), 0, 0);
        assertBillCounts(company, newEU.getEntitlement(), 1, 1);

        assertBillClosed(company, entitlementUnit.getEntitlement());
        assertBillClosed(company, newEU.getEntitlement());
    }

    @Test
    public void testChangingEntitlementsMergesEmployees() throws Throwable {
        //scenario: some employees paid in old entitlement and not in new entitlement; should be added
        DataLoadServices.setPSPDate(2011, 7, 10);
        submitUsageDataFromOFX();
        submitUsageDataFromOFX("2", "Alfred P", "Sloan");
        runPSPToEMSBSDataSyncProcessor();

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        DataLoadServices.setPSPDate(2011, 7, 15);
        EntitlementUnit newEU = DataLoadServices.addEntitlementUnit(company, "1", "2", EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, null);
        updateBDOMForCompany(company, 15);

        DataLoadServices.setPSPDate(2011, 7, 16);
        submitUsageDataFromOFX();
        runPSPToEMSBSDataSyncProcessor();

        assertBillCounts(company, entitlementUnit.getEntitlement(), 1, 0);
        assertBillCounts(company, newEU.getEntitlement(), 1, 0);

        DataLoadServices.setPSPDate(2011, 7, 31);
        runEMSBSToBRMDataSyncProcessor();

        assertBillCounts(company, entitlementUnit.getEntitlement(), 1, 0);
        assertBillCounts(company, newEU.getEntitlement(), 1, 2);

        assertBillClosed(company, entitlementUnit.getEntitlement());
        assertBillClosed(company, newEU.getEntitlement());
    }

    @Test
    public void testChangingEntitlementsAndEmployeesBillsForAllEmployees() throws Throwable {
        //scenario: all employees paid in old entitlement and not in new entitlement; should be added
        DataLoadServices.setPSPDate(2011, 7, 10);
        submitUsageDataFromOFX("3", "Archer Daniels", "Midland");
        runPSPToEMSBSDataSyncProcessor();

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        DataLoadServices.setPSPDate(2011, 7, 15);
        EntitlementUnit newEU = DataLoadServices.addEntitlementUnit(company, "1", "2", EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, null);
        updateBDOMForCompany(company, 15);

        DataLoadServices.setPSPDate(2011, 7, 16);
        submitUsageDataFromOFX();
        runPSPToEMSBSDataSyncProcessor();

        assertBillCounts(company, entitlementUnit.getEntitlement(), 1, 0);
        assertBillCounts(company, newEU.getEntitlement(), 1, 0);

        DataLoadServices.setPSPDate(2011, 7, 31);
        runEMSBSToBRMDataSyncProcessor();

        assertBillCounts(company, entitlementUnit.getEntitlement(), 1, 0);
        assertBillCounts(company, newEU.getEntitlement(), 1, 2);

        assertBillClosed(company, entitlementUnit.getEntitlement());
        assertBillClosed(company, newEU.getEntitlement());
    }

    @Test
    public void testChangingEntitlementsAndNotRunningPayrollStillBills() throws Throwable {

        DataLoadServices.setPSPDate(2011, 7, 10);
        submitUsageDataFromOFX();
        runPSPToEMSBSDataSyncProcessor();

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        DataLoadServices.setPSPDate(2011, 7, 15);
        EntitlementUnit newEU = DataLoadServices.addEntitlementUnit(company, "1", "2", EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, null);
        updateBDOMForCompany(company, 15);

        DataLoadServices.setPSPDate(2011, 7, 16);
        runPSPToEMSBSDataSyncProcessor();

        assertBillCounts(company, entitlementUnit.getEntitlement(), 1, 0);
        assertNoBill(company, newEU.getEntitlement()); //at this point, no bill will have been created for the new entitlement since there's no payroll

        DataLoadServices.setPSPDate(2011, 7, 31);
        runEMSBSToBRMDataSyncProcessor();

        //now the job will move the old bill to the new entitlement
        assertNoBill(company, entitlementUnit.getEntitlement());
        assertBillCounts(company, newEU.getEntitlement(), 1, 1);

        assertBillClosed(company, newEU.getEntitlement());
    }

    @Test
    public void testMigratingToNewEntitlementDoesNotUpdateHistorical() throws Throwable {

        DataLoadServices.setPSPDate(2011, 7, 10);
        submitUsageDataFromOFX();
        runPSPToEMSBSDataSyncProcessor();

        DataLoadServices.setPSPDate(2011, 7, 31);
        runEMSBSToBRMDataSyncProcessor();

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        assertBillCounts(company, entitlementUnit.getEntitlement(), 1, 1);

        DataLoadServices.setPSPDate(2011, 8, 10);
        submitUsageDataFromOFX();
        runPSPToEMSBSDataSyncProcessor();

        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        DataLoadServices.setPSPDate(2011, 8, 15);
        EntitlementUnit newEU = DataLoadServices.addEntitlementUnit(company, "1", "2", EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, null);
        updateBDOMForCompany(company, 15);

        DataLoadServices.setPSPDate(2011, 8, 16);
        runPSPToEMSBSDataSyncProcessor();

        Application.beginUnitOfWork();
        CompanyUsage usage = CompanyUsage.findCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlementUnit.getEntitlement().getLicenseNumber(), entitlementUnit.getEntitlement().getEntitlementOfferingCode());
        DomainEntitySet<Bill> bills = Application.find(Bill.class, Bill.CompanyUsage().equalTo(usage));
        assertEquals(2, bills.size());
        Bill julyBill = assertOne(bills.find(Bill.Closed().equalTo(true)));
        assertEquals(1, julyBill.getUsageCount());
        assertEquals(1, julyBill.getSynchedCount());
        Bill augustBill = assertOne(bills.find(Bill.Closed().equalTo(false)));
        assertEquals(1, augustBill.getUsageCount());
        assertEquals(0, augustBill.getSynchedCount());
        Application.rollbackUnitOfWork();

        assertNoBill(company, newEU.getEntitlement());

        DataLoadServices.setPSPDate(2011, 8, 31);
        runEMSBSToBRMDataSyncProcessor();
        //in this scenario, the bill on the old entitlement will be for the previous period and there will be no bill for the new period, hence it will just find the one
        assertBillCounts(company, entitlementUnit.getEntitlement(), 1, 1);
        assertBillCounts(company, newEU.getEntitlement(), 1, 1);

        assertBillClosed(company, entitlementUnit.getEntitlement());
        assertBillClosed(company, newEU.getEntitlement());
    }

    @Test
    @Ignore("This scenario might be nice behavior, but there is no easy way to implement it because of the way the paycheck tokens are set on the entitlement level.  Business will workaround manually.")
    public void testVoidingPaycheckOnOldEntitlementIsNotCounted() throws Throwable {

        DataLoadServices.setPSPDate(2011, 7, 10);
        submitUsageDataFromOFX();
        String paycheckToVoid = submitUsageDataFromOFX("2", "Alfred P", "Sloan");
        runPSPToEMSBSDataSyncProcessor();

        EntitlementUnit entitlementUnit = assertOne(company.getActiveEntitlementUnits());
        DataLoadServices.deactivateEntitlementUnit(entitlementUnit);

        DataLoadServices.setPSPDate(2011, 7, 15);
        EntitlementUnit newEU = DataLoadServices.addEntitlementUnit(company, "1", "2", EditionType.Enhanced, null, DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_YEARLY, null);
        updateBDOMForCompany(company, 15);

        DataLoadServices.setPSPDate(2011, 7, 16);
        submitUsageDataFromOFX();
        runPSPToEMSBSDataSyncProcessor();

        assertBillCounts(company, entitlementUnit.getEntitlement(), 2, 0);
        assertBillCounts(company, newEU.getEntitlement(), 1, 0);

        DataLoadServices.setPSPDate(2011, 7, 17);
        voidUsageData(paycheckToVoid);

        runPSPToEMSBSDataSyncProcessor();

        assertBillCounts(company, entitlementUnit.getEntitlement(), 1, 0);
        assertBillCounts(company, newEU.getEntitlement(), 1, 0);

        DataLoadServices.setPSPDate(2011, 7, 31);
        runEMSBSToBRMDataSyncProcessor();

        assertBillCounts(company, entitlementUnit.getEntitlement(), 1, 0);
        assertBillCounts(company, newEU.getEntitlement(), 1, 1);

        assertBillClosed(company, entitlementUnit.getEntitlement());
        assertBillClosed(company, newEU.getEntitlement());
    }

    @Test
    public void basicHappyPathDDWithErrEEs() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFXWithErrEEs(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 1, getUsageOnBill(mDDPSID, 2011, 8, 15));
    }

    @Test
    public void noFraudReviewForMultiUsagePaycheck() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        for (int d = 0; d < 8; d++) {
            DataLoadServices.setPSPDate(2011, 7, 20 + d);
            for (int i = 0; i < 10; i++) {
                OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", String.valueOf(100 + i + 10 * d), "N");
                QBDTTestHelper.processOFXRequestSuccess(request);
            }
        }

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        CompanyService companyService = company.getCompanyService(ServiceCode.Cloud);
        junit.framework.Assert.assertEquals("service status is not active", ServiceSubStatusCode.ActiveCurrent, companyService.getStatusCd());
        companyService = company.getCompanyService(ServiceCode.DirectDeposit);
        junit.framework.Assert.assertEquals("service status is not active", ServiceSubStatusCode.ActiveCurrent, companyService.getStatusCd());
        PayrollServices.rollbackUnitOfWork();
    }

    @Ignore /* Test in failing at Download step as BRM would not have generated error file yet which we are trying to download */
    @Test
    public void basicHappyPathCloud() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 1, getUsageOnBill(mCloudPSID, 2011, 8, 15));

        List<Object[]> billDetailsForCompany = getBillDetailsForCompany(mCloudPSID, 2011, 8, 15);
        List<Object[]> billDetailsForLicense = getBillDetailsForLicense(mCloudPSID, 2011, 8, 15, mLicenceIdCloud);
        assertNotNull(billDetailsForCompany);
        assertNotNull(billDetailsForLicense);
        assertEquals("billDetailsForCompany", 1, billDetailsForCompany.size());
        assertEquals("billDetailsForLicense", 1, billDetailsForLicense.size());

        SpcfCalendar calendar = SpcfCalendar.createInstance(2011, 7, 31, 07, 0, 0, 0);
        DataLoadServices.setPSPDate(calendar);

        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, "2", "");
        aEMSBSToBRMDataSyncProcessor.executeJob();

        String fileName = "PSP_SymphonyUsage_20110731000000";
        PayrollServices.beginUnitOfWork();
        SystemParameter systemParameter = SystemParameter.findSystemParameter(SystemParameter.Code.BRM_SYMPHONY_FILE_NAME);
        assertEquals("Symphony file name System Parameter", fileName, systemParameter.getSystemParameterValue());
        PayrollServices.rollbackUnitOfWork();

        String args = null;
        BRMUsageErrorFileProcessor brmUsageErrorFileProcessor = new BRMUsageErrorFileProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.BRMUsageErrorFileProcessor, "3", args);
        brmUsageErrorFileProcessor.executeJob();

        File file = new File(BRMFileUploader.LOCAL_ARCHIVE_DIR + fileName + "_Error" + BRMFileUploader.FILENAME_EXT);
        Assert.assertTrue(file.exists());

    }

    @Test
    public void PSP2827Test() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();
        updateBDOMForCompany(company, 31);

        DataLoadServices.setPSPDate(2012, 5, 20);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 1, getUsageOnBill(mCloudPSID, 2012, 6, 30));

        // close the bill
        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, "2", "");
        aEMSBSToBRMDataSyncProcessor.executeJob();

        //
        DataLoadServices.setPSPDate(2012, 5, 21);
        request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "2", "Susan", "Butchman", "101", "N");
        QBDTTestHelper.processOFXRequestSuccess(request);

        aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "2", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 0, getUsageOnBill(mCloudPSID, 2012, 7, 31));
    }

    @Test
    public void freeTrialTest() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        primaryEntitlementUnit.getEntitlement().setSubscriptionStartDate(new SpcfCalendarImpl(2011, 8, 20, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 0, getUsageOnBill(mCloudPSID, 2011, 8, 15));
    }

    @Test
    public void symphonyDDFeeTest() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2011, 8, 10));
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
        }
        PayrollServices.rollbackUnitOfWork();

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitPayroll(company, payrollRunDTO);
        System.out.println(OFXManager.javaResponseToOFX(response));
        for (com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX aTx : response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS().getIPAYROLLTX()) {
            for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE aTxLine : aTx.getITXLINE()) {
                Assert.assertFalse("Zero fee exists", aTxLine.getIAMT().equals("$0.00"));
            }
        }
    }

    @Test
    /**
     * Testing for  QBPP017508
     * Symphony::Silent send fails always from the time a paycheck is created 45 days in advance.      (FutureDatedPayrollTooFarInfutureError)
     */
    public void testFutureDatedPayrollTooFarInfutureError() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2012, 11, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        System.out.println(OFXManager.javaRequestToOFX(request));

        DataLoadServices.setPSPDate(2012, 8, 7);
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getCODE().equals("0"));

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 1, getUsageOnBill(mDDPSID, 2012, 12, 15));
    }

    @Test
    public void voidPaycheckAfterDDCancelTest() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "2", "Jack", "Butchman", "101", "N");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        // run usage
        (new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "")).executeJob();
        assertEquals("usage", 2, getUsageOnBill(mDDPSID, 2011, 8, 15));

        PayrollServices.beginUnitOfWork();
        PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.DirectDeposit, ServiceSubStatusCode.Cancelled);
        PayrollServices.commitUnitOfWork();

        // void the check now
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "Y");
        responseMsg = QBDTTestHelper.processRequest(OFXManager.javaRequestToOFX(request), QBOFX.MESSAGE_SEVERITY.INFO, QBOFX.MESSAGE_SEVERITY.ERROR);
         /*
        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
        */
    }

    @Test
    public void voidPaycheckTest() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N", false, false);
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "2", "Jack", "Butchman", "101", "N", false, false);
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        // run usage
        (new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "")).executeJob();
        assertEquals("usage", 2, getUsageOnBill(mDDPSID, 2011, 8, 15));

        // void the check now
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "Y", false, false);
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        // run usage
        (new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "2", "")).executeJob();
        assertEquals("usage", 1, getUsageOnBill(mDDPSID, 2011, 8, 15));
    }

    @Test
    public void voidPaycheckOnClosedBillTest() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        // run usage
        (new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "")).executeJob();
        assertEquals("usage", 1, getUsageOnBill(mDDPSID, 2011, 8, 15));

        // close bill and move the bill date
        Application.beginUnitOfWork();
        Bill aBill = getBill(mDDPSID, 2011, 8, 15);
        aBill.setClosed(true);
        aBill.setBillDate(SpcfCalendar.createInstance(2011, 8, 1, SpcfTimeZone.getLocalTimeZone()));
        Application.save(aBill);
        Application.commitUnitOfWork();

        // send paycheck usage for another employee
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "2", "Jack", "Butchman", "101", "N");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        // void the check now
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "Y");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        // run usage
        (new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "2", "")).executeJob();
        assertEquals("usage", 1, getUsageOnBill(mDDPSID, 2011, 8, 15));
    }

    @Test
    public void usageBeforeSymphonyStartDateTest() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 6, 20);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(mCloudPSID).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(0, companyUsages.size());
    }

    @Test
    public void test_recall_DD_with_live_paycheck() {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1");
        List<Employee> emps = DataLoadServices.addEEs(company, 2, true, true);

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");

        DataLoadServices.setPSPDate(2012, 1, 1);
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(2012, 1, 15), emps, lawAmounts);
        String paycheckId = null;
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
            if (paycheckId == null) {
                paycheckId = paycheckDTO.getPaycheckId();
            } else {
                paycheckDTO.getDdTransactions().clear();
                break;
            }
        }
        PayrollServices.rollbackUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitPayroll(company, payrollRunDTO, false);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 15, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        FinancialTransaction ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                                            .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                            .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee)));
        FinancialTransaction monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                    .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                    .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        FinancialTransaction employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        SpcfDecimal liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if (itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);

            } else if (itxline.getIMEMO() == null) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if (itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 1, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(1)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if (itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if (itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 2), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if (itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }

        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());

        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePaycheckIdList(Arrays.asList(paycheckId));
        transactionCancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        assertSuccess(PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelEEDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 15, SpcfTimeZone.getLocalTimeZone()))));
        ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        monthlyFee = payrollRun.getFinancialTransactionCollection()
                               .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                               .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        employeeFee = payrollRun.getFinancialTransactionCollection()
                                .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        response = QBDTTestHelper.submitSyncRequest(company, company.getCurrentToken() - 1, true);
        ipayrolltx = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD().get(0);
        liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if (itxline.getIPITEMID() != null) {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if (itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.NO_FEE_FOR_DIRECT_DEPOSIT, 0), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if (itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if (itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 2), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if (itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }

        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());
    }

    @Test
    public void basicHappyPathDDRetail() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        Entitlement entitlement = primaryEntitlementUnit.getEntitlement();
        entitlement.setSubscriptionStartDate(SpcfCalendar.createInstance(2012, 10, 1));
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N", false, false);
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        PayrollServices.beginUnitOfWork();
        // Paycheck Usage is set to trial since the sub start date is in the future
        EmployeeUsage employeeUsage = Application.find(EmployeeUsage.class).get(0);
        assertEquals("EmployeeUsage", 0, employeeUsage.getUsageCount());
        PaycheckUsage paycheckUsage = PaycheckUsage.findPaycheckUsage(employeeUsage, "100ppp");
        assertEquals("PaycheckUsage", ReasonForFreeChargeCode.Trial, paycheckUsage.getReasonForFreeCharge());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        entitlement = primaryEntitlementUnit.getEntitlement();
        entitlement.setRetail(true);
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();

        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "200", "N", false, false);
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "2", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        PayrollServices.beginUnitOfWork();
        // Paycheck Usage is set to None for retail
        employeeUsage = Application.find(EmployeeUsage.class).get(0);
        assertEquals("EmployeeUsage", 1, employeeUsage.getUsageCount());
        paycheckUsage = PaycheckUsage.findPaycheckUsage(employeeUsage, "200ppp");
        assertEquals("PaycheckUsage", ReasonForFreeChargeCode.None, paycheckUsage.getReasonForFreeCharge());
        PayrollServices.rollbackUnitOfWork();
        assertEquals("usage", 1, getUsageOnBill(mDDPSID, 2011, 8, 15));


        List<Object[]> billDetailsForCompany = getBillDetailsForCompany(mDDPSID, 2011, 8, 15);
        List<Object[]> billDetailsForLicense = getBillDetailsForLicense(mDDPSID, 2011, 8, 15, mLicenceIdDD);
        assertNotNull(billDetailsForCompany);
        assertNotNull(billDetailsForLicense);
        assertEquals("billDetailsForCompany", 2, billDetailsForCompany.size());
        assertEquals("billDetailsForLicense", 2, billDetailsForLicense.size());


    }


    public void basicErrorFileProcessing() throws Exception {

        setupDDCompany("123456789", "234567890123456", "67890");
        setupDDCompany("234567890", "345678901234567", "78901");

        String args = "PSP_SymphonyUsage_20XX_Error";
        BRMUsageErrorFileProcessor brmUsageErrorFileProcessor = new BRMUsageErrorFileProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.BRMUsageErrorFileProcessor, "3", args);
        brmUsageErrorFileProcessor.executeJob();

        File file = new File(BRMFileUploader.LOCAL_ARCHIVE_DIR + args + "_reupload" + BRMFileUploader.FILENAME_EXT);
        Assert.assertTrue(file.exists());

    }

    /**
     * After PSP-6192, The disabled entitlements will not be considered for usage billing.
     * This unit test was for the 60-day condition, which is not there anymore.
     * @throws Exception
     */
    @Test
    public void testUsageBillingForSuspendedCompany() throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 1, getUsageOnBill(mDDPSID, 2011, 8, 15));

        DataLoadServices.setPSPDate(2011, 7, 31);
        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, "2", "");
        aEMSBSToBRMDataSyncProcessor.executeJob();

        //Disabling the entitlement
        DataLoadServices.setPSPDate(2011, 8, 1);
        PayrollServices.beginUnitOfWork();
        String lic = primaryEntitlementUnit.getEntitlement().getLicenseNumber();
        String pEOC = primaryEntitlementUnit.getEntitlement().getEntitlementOfferingCode();
        SpcfCalendar subsEndDate = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
        Entitlement entitlement = Entitlement.findEntitlement(lic, pEOC);
        entitlement.setEntitlementState(EntitlementStateCode.Disabled);
        entitlement.setSubscriptionEndDate(subsEndDate);
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();

        //Check for Usage Billing creation on date that is less than 60 days from the SubscriptionEndDate

        DataLoadServices.setPSPDate(2011, 9, 2);

        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "102", "N");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "3", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        //Bill was expected to be generated, Since the 9/2 is less than 60 days from Suspended Date(SubscriptionEndDate) of the Entitlement
        assertNull(getBill(mDDPSID, 2011, 10, 15));

        DataLoadServices.setPSPDate(2012, 3, 2);
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "101", "N");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "4", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        //Bill was expected to be generated on 4/15 but doesnt get generated as the entitlement is disabled more than 60 days ago
        assertNull(getBill(mDDPSID, 2012, 4, 15));

    }

    @Test
    public void testPayrollRunAfterBRMSyncJob() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company companyOne = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String einOne = companyOne.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = companyOne.getActivePrimaryEntitlementUnit();
        String subscriptionOne = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, einOne, subscriptionOne, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);

        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        // run the PSPToEMSBSDataSyncProcessor job and so it will create the bills
        runPSPToEMSBSDataSyncProcessor();
        // run the EMSBSToBRMDataSyncProcessor job and so it will generate csv and it will close the bills
        runEMSBSToBRMDataSyncProcessor();

        // after running the EMSBSToBRMDataSyncProcessor job and bills will be closed
        Bill b = getBill(mDDPSID, 2011, 8, 15);
        assertNotNull(b);
        assertTrue(b.getClosed());

        // run the payroll after EMSBSToBRMDataSyncProcessor and on last day of month
        SpcfCalendar sdate = SpcfCalendar.createInstance(2011, 7, 31, 21, 0, 0 ,0, SpcfTimeZone.getLocalTimeZone());
        DataLoadServices.setPSPDate(sdate);
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, einOne, subscriptionOne, PSPDate.getPSPTime(), "2", "Susan2", "Butchman2", "101", "N");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);
        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        // run the PSPToEMSBSDataSyncProcessor job and so it will create the bills
        runPSPToEMSBSDataSyncProcessor();

        // for above payroll new bill will created and it will be open till next EMSBSToBRMDataSyncProcessor job run
        b = getBill(mDDPSID, 2011, 9, 15);
        assertNotNull(b);
        assertFalse(b.getClosed());

        DataLoadServices.setPSPDate(2011, 8, 31);
        // run the EMSBSToBRMDataSyncProcessor job and so it will generate csv and it will close the bills
        runEMSBSToBRMDataSyncProcessor();

        // now for above payroll new bill will be closed
        b = getBill(mDDPSID, 2011, 9, 15);
        assertNotNull(b);
        assertTrue(b.getClosed());
    }

    @Test
    /**
     * Usage timestamp should not be after Entitlement disablement date (PSP-6192)
     * This is handled by considering the payrollRuns for Active Entitlements only.
     */
    public void testCountUsageForActiveEntitlementsOnly() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 2);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        Assert.assertEquals("usage", 1, getUsageOnBill(mDDPSID , 2011, 8, 15));

        DataLoadServices.setPSPDate(2011, 7, 7);
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime() , "4", "Susan", "Futchman", "104", "N");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "4", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        Assert.assertEquals("usage", 2, getUsageOnBill(mDDPSID , 2011, 8, 15));

        DataLoadServices.setPSPDate(2011, 7, 17);
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "5", "Susan", "Kutchman", "105", "N");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "5", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        Assert.assertEquals("usage", 3, getUsageOnBill(mDDPSID , 2011, 8, 15));

        //Disabling the entitlement
        DataLoadServices.setPSPDate(2011, 7, 25);
        PayrollServices.beginUnitOfWork();
        String lic = primaryEntitlementUnit.getEntitlement().getLicenseNumber();
        String pEOC = primaryEntitlementUnit.getEntitlement().getEntitlementOfferingCode();
        SpcfCalendar subsEndDate = SpcfCalendar.createInstance(PSPDate.getPSPTime().getYear(), PSPDate.getPSPTime().getMonth(), PSPDate.getPSPTime().getDay());
        Entitlement entitlement = Entitlement.findEntitlement(lic, pEOC);
        entitlement.setEntitlementState(EntitlementStateCode.Disabled);
        entitlement.setSubscriptionEndDate(subsEndDate);
        Application.save(entitlement);
        PayrollServices.commitUnitOfWork();

        //Submitting a payroll after disabling the entitlement
        DataLoadServices.setPSPDate(2011, 7, 27);
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "2", "Susan", "Dutchman", "101", "N");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "2", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        //New usage Should not be counted
        Assert.assertEquals("usage", 3, getUsageOnBill(mDDPSID , 2011, 8, 15));

        DataLoadServices.setPSPDate(2011, 7, 28);
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "3", "Susan", "Kutchman", "102", "N");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "3", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        //New usage Should not be counted
        Assert.assertEquals("usage", 3, getUsageOnBill(mDDPSID , 2011, 8, 15));

        DataLoadServices.setPSPDate(2011, 7, 31);
        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, "2", "");
        aEMSBSToBRMDataSyncProcessor.executeJob();

        //Payroll submitted in the next month
        DataLoadServices.setPSPDate(2011, 8, 8);
        request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "6", "Susan", "Kutchman", "106", "N");
        responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "9", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        //New usage bill should not be generated since the entitlement is disabled
        Assert.assertEquals(null, getBill(mDDPSID, 2011, 9, 15));

        DataLoadServices.setPSPDate(2011, 8, 31);
        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor2 = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, "2", "");
        aEMSBSToBRMDataSyncProcessor2.executeJob();
    }

    @Test
    public void testTimestampOnTransactionDateOnUsage() throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 2);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        Assert.assertEquals("usage", 1, getUsageOnBill(mDDPSID , 2011, 8, 15));

        DataLoadServices.setPSPDate(2011, 8, 31);
        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor2 = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, "2", "");
        aEMSBSToBRMDataSyncProcessor2.executeJob();

    }

    private List<String[]> mEntitlementList = null;
    private String[] mLicenseNumList = null;
    List<String[]> mOutputList = new ArrayList<String[]>();
    private String mFileName = null;

    final String LOCAL_RECV_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_local_recv_dir");
    final String LOCAL_WORK_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_local_work_dir");
    final String PSP_S3_ERROR_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_local_err_dir");
    final String PSP_S3_ARCHIVE_DIR = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_brm_s3_local_arcv_dir");

    @Test
    public void testDownloadBRMUsageErrorFileDownload() throws Exception{


            File errorFolder = new File(PSP_S3_ERROR_DIR);
            File[] listOfFiles = errorFolder.listFiles();
            copyFile(listOfFiles,LOCAL_RECV_DIR);
            //check if files are downloaded to Local RECV folder
            File dir = new File(LOCAL_RECV_DIR);
            File[] files = dir.listFiles();

            Assert.assertTrue(files!=null);

    }

    @Test
    public void testBRMUsageErrorFileProcessing() throws Exception{

        File errorFolder = new File(LOCAL_RECV_DIR);
        File[] listOfFiles = errorFolder.listFiles();
        for(File file : listOfFiles){
            if(file.getName().startsWith("PSP") && file.getName().endsWith(".csv")){
                loadEntitlementsInFile(file);
                processErrorFile();
                //After processing copy files to work folder
                writeToFile(file.getName());
            }
        }
        File workFolder = new File(LOCAL_WORK_DIR);
        File[] files = workFolder.listFiles();
        Assert.assertTrue(files!=null);

    }


    @Test
    public void testBRMUsageErrorFileArchiving() throws Exception{

        File errorFolder = new File(LOCAL_WORK_DIR);
        File[] listOfFiles = errorFolder.listFiles();
        copyFile(listOfFiles,PSP_S3_ARCHIVE_DIR);
        Assert.assertTrue(new File(PSP_S3_ARCHIVE_DIR).listFiles().length>0);
    }

    private void copyFile(File[] listOfFiles,String destDir) throws FileNotFoundException{

            for (File fileName : listOfFiles) {
                if(fileName.getName().startsWith("PSP") && fileName.getName().endsWith(".csv")){
                    InputStream inputStream = new FileInputStream(fileName);
                    File file = new File(destDir + fileName.getName());
                    try (FileOutputStream outputStream = new FileOutputStream(file)) {
                        int read;
                        byte[] bytes = new byte[1024];
                        while ((read = inputStream.read(bytes)) != -1) {
                            outputStream.write(bytes, 0, read);
                        }
                    }catch (IOException ex){
                        System.out.println("IO Error occured while downloading error file from BRM s3 "+ex.getMessage());
                    }
                }
            }
    }

    private void processErrorFile() {
        SpcfCalendar calendar = CalendarUtils.getFirstDayOfPrevMonth(PSPDate.getPSPTime());
        CalendarUtils.clearTime(calendar);

        System.out.println(mLicenseNumList + " " + calendar);

        // Get the entitlements for the license numbers in the error file
        // whose status is enabled or if disabled whose subscription end date is after the previous month had started
        DomainEntitySet<Entitlement> entitlementDomainEntity = Application.find(Entitlement.class,
                Entitlement.LicenseNumber().in(mLicenseNumList)
                        .And((Entitlement.EntitlementState().equalTo(EntitlementStateCode.Enabled)
                                .Or(Entitlement.EntitlementState().equalTo(EntitlementStateCode.Disabled)
                                        .And(Entitlement.SubscriptionEndDate().greaterOrEqualThan(calendar)))))
        ).sort(Entitlement.LicenseNumber());

        int count = 0;
        for (Entitlement entitlement : entitlementDomainEntity) {
            while (count < mEntitlementList.size()) {
                String dbLicenseNum = entitlement.getLicenseNumber();
                String errLicenseNum = mEntitlementList.get(count)[0];

                if (errLicenseNum.compareTo(dbLicenseNum) > 0) {
                    break;
                }

                String eoc = mEntitlementList.get(count)[1];
                if (dbLicenseNum.equalsIgnoreCase(errLicenseNum)) {
                    eoc = entitlement.getEntitlementOfferingCode();
                }

                SpcfCalendar monthEnd = CalendarUtils.getLastDayOfMonth(calendar);

                if (EntitlementStateCode.Disabled.equals(entitlement.getEntitlementState()) && entitlement.getSubscriptionEndDate() != null) {
                    monthEnd = entitlement.getSubscriptionEndDate();
                }
                String[] record = new String[5];
                record[0] = mEntitlementList.get(count)[0];
                record[1] = eoc;
                record[2] = mEntitlementList.get(count)[2];
                record[3] = mEntitlementList.get(count)[3];
                record[4] = CalendarUtils.convertCalendarToXmlStringNoMilliSeconds(monthEnd);

                mOutputList.add(record);

                count++;
            }
        }

        while(count < mEntitlementList.size()){
            mOutputList.add(mEntitlementList.get(count));
            count++;
        }
    }

    public void loadEntitlementsInFile(File pFile) throws FileNotFoundException,IOException {
        CSVReader reader = null;
        try {
            if(!pFile.getName().isEmpty() && !pFile.getName().contains("swp") && !pFile.getName().startsWith(".")){
                if(StreamUtil.isFileIDPSEncrypted(pFile))
                {
                    try {

                        Key key = IDPSFileStreamManager.newKeyHandleLatest();
                        reader = new CSVReader(new IDPSFileReader( pFile, key));
                    }catch(IdpsException e)
                    {
                        throw new RuntimeException("Can not proceed. IDPS error of BRM Error file", e);
                    }
                }
                else{
                    reader = new CSVReader(new FileReader(pFile));
                }
                if(reader!=null){
                    mEntitlementList = reader.readAll();
                }else{
                    System.out.println("EntitlementList is null for file"+pFile.getName());
                }

            }

        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("failed to close BRM file "+e);
                }
            }
        }
        // Remove the header row
        if(mEntitlementList!=null && mEntitlementList.size()>0){
            String[] entitlementHeader = Arrays.copyOf(mEntitlementList.get(0),5);
            mOutputList.add(entitlementHeader);
            mEntitlementList.remove(0);
            Collections.sort(mEntitlementList, new Comparator<String[]>() {

                public int compare(String[] entitlement1, String[] entitlement2) {
                    return entitlement1[0].compareTo(entitlement2[0]);
                }
            });
            mLicenseNumList = new String[mEntitlementList.size()];
            for (int i = 0; i < mEntitlementList.size(); i++) {
                mLicenseNumList[i] = mEntitlementList.get(i)[0].trim();
            }
        }else{
            System.out.println("EntitlementList is null for file"+pFile.getName());
        }

    }

    private void writeToFile(String fileName) throws IOException {
        OutputStreamWriter fileWriter = null;
        boolean fileGenSuccess = true;
        try {

            fileWriter = new FileWriter(new File(LOCAL_WORK_DIR, FilenameUtils.getName(fileName)));
            CSVWriter csvWriter = new CSVWriter(fileWriter, ',', CSVWriter.NO_QUOTE_CHARACTER);
            csvWriter.writeAll(mOutputList);
            csvWriter.close();
            fileWriter.close();
        }  finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    fileGenSuccess = false;
                    System.out.println("failed to close BRM file " + e);
                }
            }
        }
        if (!fileGenSuccess) {
            File outputFile = new File(LOCAL_WORK_DIR, FilenameUtils.getName(fileName));
            outputFile.delete();
        }
    }
}
