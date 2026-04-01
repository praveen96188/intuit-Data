package com.intuit.sbd.payroll.psp.adapters.qbdt.billing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.billing.MockingBRM;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EMSBSToBRMDataSyncProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.PSPToEMSBSDataSyncProcessor;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.CompanyQB1DataLoader;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: Poonam Gupta
 * Date: 7/05/12
 * Time: 12:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsageBillingForBasicTests extends UsageBillingTestsBase {
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

        DataLoadServices.setPSPDate(2011, 8, 14);
        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, "2", "");
        aEMSBSToBRMDataSyncProcessor.executeJob();

        //assertEquals("charges for company " + mDDPSID, 1, MockingBRM.getInstance().getCharge(mDDPSID));
    }

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

        DataLoadServices.setPSPDate(2011, 8, 14);
        EMSBSToBRMDataSyncProcessor aEMSBSToBRMDataSyncProcessor = new EMSBSToBRMDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.EMSBSToBRMDataSyncProcessor, "2", "");
        aEMSBSToBRMDataSyncProcessor.executeJob();

        //assertEquals("charges for company " + mCloudPSID, 1, MockingBRM.getInstance().getCharge(mCloudPSID));
    }


    /* Invalid pin for DD company
    */

    @Test
    public void invalidEINDD() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, "", subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");

        String responseMsg = QBDTTestHelper.processOFXRequestSignOnError(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE().contains("Problem EIN does not match our record"));
    }

    /*
    Invalid pin for DD company
    */
    @Test
    public void invalidEINCloud() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, "", subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");

        String responseMsg = QBDTTestHelper.processOFXRequestSignOnError(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE().contains("Problem EIN does not match our record"));
    }
    /*Problem Subscription number does not match our record
    */

    @Test
    public void invalidSubscriptionNumberDD() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, "", PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");

        String responseMsg = QBDTTestHelper.processOFXRequestSignOnError(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE().contains("Problem Subscription number does not match our record"));
    }

    /*Problem Subscription number does not match our record
     */

    @Test
    public void invalidSubscriptionNumberCloud() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, "", PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");

        String responseMsg = QBDTTestHelper.processOFXRequestSignOnError(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getMESSAGE().contains("Problem Subscription number does not match our record"));
    }

    /*
     Validating that if EIN and Sub# are not passed then Pin is used to authenticate
    */
    @Test

    public void emptyEINAndSubCloud() throws Exception {

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, mCompanyPassword, UsageOFXDataloader.OFX_NULL_STRING, UsageOFXDataloader.OFX_NULL_STRING, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 1, getUsageOnBill(mCloudPSID, 2011, 8, 15));
    }

    @Test
    /**
     * Testing for  QBPP017527
     * Symphony::Backdated checks before activation date should bot be calculated by PSP
     */
    public void backDatedChecksCloud() throws Exception {
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
        DataLoadServices.setPSPDate(2011, 7, 20);
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(mCloudPSID).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(0, companyUsages.size());
    }

    @Test
    /**
     * Symphony::Billing for multiple employees
     */
    public void multiEmployeeTest() throws Exception {
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

        DataLoadServices.setPSPDate(2011, 7, 27);


        OFX request3 = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "2", "Joe", "Marsh", "102", "N");
        String responseMsg3 = QBDTTestHelper.processOFXRequestSuccess(request3);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response3 = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response3.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        DataLoadServices.setPSPDate(2011, 7, 30);
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 2, getUsageOnBill(mCloudPSID, 2011, 8, 15));
    }

    @Test
    /**
     * Symphony::Billdate calculation if PSPDate is < billDayOfMonth for company
     */
    public void testBillDateForBackDatedChecksBeforeBillDayOfMonth() throws Exception {
        PayrollServicesTest.truncateTables();

        PayrollServicesTest.beforeEachTest();
        DataLoadServices.resetAllPaymentTemplateSupportDates();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN, "0");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2014, 4, 1);
        setupDDCompany();
        setupCloudCompany();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
         primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2014, 4, 7);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        DataLoadServices.setPSPDate(2014, 5, 10);
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
        //DataLoadServices.setPSPDate(2014, 7, 20);
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(mCloudPSID).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companyUsages.size());
        SpcfCalendar expectedBillDate=SpcfCalendar.createInstance(2014, 6, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar billdate=companyUsages.getFirst().getBillCollection().getFirst().getBillDate()  ;
        Assert.assertEquals("Year",expectedBillDate.getYear(),billdate.getYear());
        Assert.assertEquals("Month",expectedBillDate.getMonth(),billdate.getMonth());
        Assert.assertEquals("Day",expectedBillDate.getDay(),billdate.getDay());
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    /**
     * Symphony::Billdate calculation if PSPDate is < billDayOfMonth for company
     */
    public void testBillDateForBackDatedChecksAfterBillDayOfMonth() throws Exception {
        PayrollServicesTest.truncateTables();

        PayrollServicesTest.beforeEachTest();
        DataLoadServices.resetAllPaymentTemplateSupportDates();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN, "0");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2014, 4, 1);
        setupDDCompany();
        setupCloudCompany();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2014, 4, 7);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        DataLoadServices.setPSPDate(2014, 5, 16);
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
        //DataLoadServices.setPSPDate(2014, 7, 20);
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(mCloudPSID).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companyUsages.size());
        SpcfCalendar expectedBillDate=SpcfCalendar.createInstance(2014, 6, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar billdate=companyUsages.getFirst().getBillCollection().getFirst().getBillDate()  ;
        Assert.assertEquals("Year",expectedBillDate.getYear(),billdate.getYear());
        Assert.assertEquals("Month",expectedBillDate.getMonth(),billdate.getMonth());
        Assert.assertEquals("Day",expectedBillDate.getDay(),billdate.getDay());
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    /**
     * Symphony::Billdate calculation if PSPDate is = billDayOfMonth for company
     */
    public void testBillDateForBackDatedChecksEqualsBillDayOfMonth() throws Exception {
        PayrollServicesTest.truncateTables();

        PayrollServicesTest.beforeEachTest();
        DataLoadServices.resetAllPaymentTemplateSupportDates();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN, "0");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2014, 4, 1);
        setupDDCompany();
        setupCloudCompany();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2014, 4, 7);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        DataLoadServices.setPSPDate(2014, 5, 15);//15 is BillingDayOfMOnth  so testing for equal condition
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
        //DataLoadServices.setPSPDate(2014, 7, 20);
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(mCloudPSID).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companyUsages.size());
        SpcfCalendar expectedBillDate=SpcfCalendar.createInstance(2014, 6, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar billdate=companyUsages.getFirst().getBillCollection().getFirst().getBillDate()  ;
        Assert.assertEquals("Year",expectedBillDate.getYear(),billdate.getYear());
        Assert.assertEquals("Month",expectedBillDate.getMonth(),billdate.getMonth());
        Assert.assertEquals("Day",expectedBillDate.getDay(),billdate.getDay());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    /**
     * Symphony::Billdate calculation if PSPDate is = billDayOfMonth for company
     */
    public void testBillDateForBackDatedChecksBeforeBillDayOfMonth_MultipleRun() throws Exception {
        PayrollServicesTest.truncateTables();

        PayrollServicesTest.beforeEachTest();
        DataLoadServices.resetAllPaymentTemplateSupportDates();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN, "0");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2014, 4, 1);
        setupDDCompany();
        setupCloudCompany();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2014, 4, 7);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        DataLoadServices.setPSPDate(2014, 5, 10);//15 is BillingDayOfMOnth  so testing for equal condition
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        DataLoadServices.setPSPDate(2014, 4, 16);
         request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "2", "Susan2", "Butchman2", "101", "N");
        DataLoadServices.setPSPDate(2014, 5, 17);//15 is BillingDayOfMOnth  so testing for equal condition
         responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);
         response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);


        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(mCloudPSID).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companyUsages.size());
        SpcfCalendar expectedBillDate=SpcfCalendar.createInstance(2014, 6, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar billdate=companyUsages.getFirst().getBillCollection().getFirst().getBillDate()  ;
        Assert.assertEquals("Year",expectedBillDate.getYear(),billdate.getYear());
        Assert.assertEquals("Month",expectedBillDate.getMonth(),billdate.getMonth());
        Assert.assertEquals("Day",expectedBillDate.getDay(),billdate.getDay());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    /**
     * Symphony::Billdate calculation if PSPDate is > billDayOfMonth for company
     */
    public void testBillDateForNormalChecksAfterBillDayOfMonth() throws Exception {
        PayrollServicesTest.truncateTables();

        PayrollServicesTest.beforeEachTest();
        DataLoadServices.resetAllPaymentTemplateSupportDates();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN, "0");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2014, 4, 1);
        setupDDCompany();
        setupCloudCompany();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2014, 5, 16);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
        //DataLoadServices.setPSPDate(2014, 7, 20);
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(mCloudPSID).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companyUsages.size());
        SpcfCalendar expectedBillDate=SpcfCalendar.createInstance(2014, 6, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar billdate=companyUsages.getFirst().getBillCollection().getFirst().getBillDate()  ;
        Assert.assertEquals("Year",expectedBillDate.getYear(),billdate.getYear());
        Assert.assertEquals("Month",expectedBillDate.getMonth(),billdate.getMonth());
        Assert.assertEquals("Day",expectedBillDate.getDay(),billdate.getDay());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBillDateForNormalChecksBeforeBillDayOfMonth() throws Exception {
        PayrollServicesTest.truncateTables();

        PayrollServicesTest.beforeEachTest();
        DataLoadServices.resetAllPaymentTemplateSupportDates();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN, "0");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2014, 4, 1);
        setupDDCompany();
        setupCloudCompany();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2014, 5, 7);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
        //DataLoadServices.setPSPDate(2014, 7, 20);
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(mCloudPSID).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companyUsages.size());
        SpcfCalendar expectedBillDate=SpcfCalendar.createInstance(2014, 6, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar billdate=companyUsages.getFirst().getBillCollection().getFirst().getBillDate()  ;
        Assert.assertEquals("Year",expectedBillDate.getYear(),billdate.getYear());
        Assert.assertEquals("Month",expectedBillDate.getMonth(),billdate.getMonth());
        Assert.assertEquals("Day",expectedBillDate.getDay(),billdate.getDay());
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testBillDateForNormalChecksEqualsBillDayOfMonth() throws Exception {
        PayrollServicesTest.truncateTables();

        PayrollServicesTest.beforeEachTest();
        DataLoadServices.resetAllPaymentTemplateSupportDates();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN, "0");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2014, 4, 1);
        setupDDCompany();
        setupCloudCompany();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2014, 5, 15);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
        //DataLoadServices.setPSPDate(2014, 7, 20);
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(mCloudPSID).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companyUsages.size());
        SpcfCalendar expectedBillDate=SpcfCalendar.createInstance(2014, 6, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar billdate=companyUsages.getFirst().getBillCollection().getFirst().getBillDate()  ;
        Assert.assertEquals("Year",expectedBillDate.getYear(),billdate.getYear());
        Assert.assertEquals("Month",expectedBillDate.getMonth(),billdate.getMonth());
        Assert.assertEquals("Day",expectedBillDate.getDay(),billdate.getDay());
        PayrollServices.rollbackUnitOfWork();
    }
    @Test
    public void testBillDateForNormalChecksFutureBillDayOfMonth() throws Exception {
        PayrollServicesTest.truncateTables();

        PayrollServicesTest.beforeEachTest();
        DataLoadServices.resetAllPaymentTemplateSupportDates();

        PayrollServices.beginUnitOfWork();
        PayrollServices.systemParameterManager.updateSystemParameterValue(SystemParameter.Code.PSP_TO_EMSBS_SYNC_TOKEN, "0");
        PayrollServices.commitUnitOfWork();

        DataLoadServices.reinitialize();
        DataLoadServices.setPSPDate(2014, 4, 1);
        setupDDCompany();
        setupCloudCompany();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPSPDate(2014, 10, 10);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        DataLoadServices.setPSPDate(2014, 9, 7);
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);
        //DataLoadServices.setPSPDate(2014, 7, 20);
        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyUsage> companyUsages = Application.find(CompanyUsage.class, CompanyUsage.SourceCompanyId().equalTo(mCloudPSID).And(CompanyUsage.SourceSystemCd().equalTo(SourceSystemCode.QBDT)));
        assertEquals(1, companyUsages.size());
        SpcfCalendar expectedBillDate=SpcfCalendar.createInstance(2014, 11, 15, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar billdate=companyUsages.getFirst().getBillCollection().getFirst().getBillDate()  ;
        Assert.assertEquals("Year",expectedBillDate.getYear(),billdate.getYear());
        Assert.assertEquals("Month",expectedBillDate.getMonth(),billdate.getMonth());
        Assert.assertEquals("Day",expectedBillDate.getDay(),billdate.getDay());
        PayrollServices.rollbackUnitOfWork();
    }
}