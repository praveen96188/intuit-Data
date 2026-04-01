package com.intuit.sbd.payroll.psp.adapters.qbdt.billing;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.PSPToEMSBSDataSyncProcessor;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import org.junit.Test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 8/13/12
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class UsageBillingServiceSubStatusCancelledTerminatedTests extends UsageBillingTestsBase {

    @Test
    public void testServiceSubstatus_DDCancelledDD() throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();

        CompanyService companyService = company.getCompanyService(ServiceCode.DirectDeposit);
        companyService.setStatusCd(ServiceSubStatusCode.Cancelled);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String requestOfxStr = OFXManager.javaToOFX(request, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseMsg = QBDTTestHelper.processRequest(requestOfxStr, QBOFX.MESSAGE_SEVERITY.INFO, null);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getCODE().equals("0") );
        assertFalse(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
    }

    @Test
    public void testServiceSubstatus_DDTerminatedDD() throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();

        CompanyService companyService = company.getCompanyService(ServiceCode.DirectDeposit);
        companyService.setStatusCd(ServiceSubStatusCode.Terminated);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String requestOfxStr = OFXManager.javaToOFX(request, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseMsg = QBDTTestHelper.processRequest(requestOfxStr, QBOFX.MESSAGE_SEVERITY.INFO, null);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getCODE().equals("0") );
        assertFalse(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
    }

    @Test
    public void testServiceSubstatus_DDCancelledDDCloud() throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();

        CompanyService companyService = company.getCompanyService(ServiceCode.DirectDeposit);
        companyService.setStatusCd(ServiceSubStatusCode.Cancelled);
        Application.save(companyService);
        companyService = company.getCompanyService(ServiceCode.Cloud);
        companyService.setStatusCd(ServiceSubStatusCode.Cancelled);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String requestOfxStr = OFXManager.javaToOFX(request, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseMsg = QBDTTestHelper.processRequest(requestOfxStr, QBOFX.MESSAGE_SEVERITY.INFO, QBOFX.MESSAGE_SEVERITY.ERROR);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getCODE().equals("0") );
        assertTrue(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
    }

    @Test
    public void testServiceSubstatus_DDTerminatedDDCloud() throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();

        CompanyService companyService = company.getCompanyService(ServiceCode.DirectDeposit);
        companyService.setStatusCd(ServiceSubStatusCode.Terminated);
        Application.save(companyService);
        companyService = company.getCompanyService(ServiceCode.Cloud);
        companyService.setStatusCd(ServiceSubStatusCode.Terminated);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String requestOfxStr = OFXManager.javaToOFX(request, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseMsg = QBDTTestHelper.processRequest(requestOfxStr, QBOFX.MESSAGE_SEVERITY.INFO, QBOFX.MESSAGE_SEVERITY.ERROR);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getCODE().equals("0") );
        assertTrue(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
    }

    @Test
    public void testServiceSubstatus_CloudCancelledCloud() throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();

        CompanyService companyService = company.getCompanyService(ServiceCode.Cloud);
        companyService.setStatusCd(ServiceSubStatusCode.Cancelled);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String requestOfxStr = OFXManager.javaToOFX(request, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseMsg = QBDTTestHelper.processRequest(requestOfxStr, QBOFX.MESSAGE_SEVERITY.INFO, QBOFX.MESSAGE_SEVERITY.ERROR);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getCODE().equals("0") );
        assertTrue(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
    }

    @Test
    public void testServiceSubstatus_CloudTerminatedCloud() throws Exception {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mCloudPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();

        CompanyService companyService = company.getCompanyService(ServiceCode.Cloud);
        companyService.setStatusCd(ServiceSubStatusCode.Terminated);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mCloudPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String requestOfxStr = OFXManager.javaToOFX(request, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        String responseMsg = QBDTTestHelper.processRequest(requestOfxStr, QBOFX.MESSAGE_SEVERITY.INFO, QBOFX.MESSAGE_SEVERITY.ERROR);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getCODE().equals("0") );
        assertTrue(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.ERROR) == 0);
    }
}
