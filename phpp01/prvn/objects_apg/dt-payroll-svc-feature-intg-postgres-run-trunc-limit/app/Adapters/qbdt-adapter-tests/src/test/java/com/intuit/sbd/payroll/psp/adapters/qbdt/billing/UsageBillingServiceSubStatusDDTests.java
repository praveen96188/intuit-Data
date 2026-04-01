package com.intuit.sbd.payroll.psp.adapters.qbdt.billing;

/**
 * Created by IntelliJ IDEA.
 * User: vidhyak689
 * Date: 7/31/12
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.EMSBSToBRMDataSyncProcessor;
import com.intuit.sbd.payroll.psp.batchjobs.processors.PSPToEMSBSDataSyncProcessor;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(Parameterized.class)
public class UsageBillingServiceSubStatusDDTests extends UsageBillingTestsBase{

    private ServiceSubStatusCode subStatusCode;
    private String subStatusName;

	 public UsageBillingServiceSubStatusDDTests(ServiceSubStatusCode ssc, String name) {
	    this.subStatusCode = ssc;
        this.subStatusName =   name;
	 }

	 @Parameters
     public static Collection<Object[]> data() {
         Object[][]  substatusList= new Object[][]{
                 {ServiceSubStatusCode.AchRejectOther,"AchRejectOther"},
                 {ServiceSubStatusCode.AchRejectR1R9,"AchRejectR1R9"},
                 {ServiceSubStatusCode.ActiveCurrent,"ActiveCurrent"},
                 {ServiceSubStatusCode.DirectDepositLimit,"DirectDepositLimit"},
                 {ServiceSubStatusCode.FraudReview,"FraudReview"},
                 {ServiceSubStatusCode.IntuitCollections,"IntuitCollections"},
                 {ServiceSubStatusCode.PendingBankVerification,"PendingBankVerification"},
                 {ServiceSubStatusCode.PendingFirstPayroll,"PendingFirstPayroll"},
                 {ServiceSubStatusCode.PendingPinCreation,"PendingPinCreation"},
                 {ServiceSubStatusCode.SuspendedDirectDeposit,"SuspendedDirectDeposit"},
                 {ServiceSubStatusCode.PendingPrefundingWire,"PendingPrefundingWire"},
                 {ServiceSubStatusCode.AS400Hold,"AS400Hold"},
                 {ServiceSubStatusCode.AS400DirectDepositLimitHold,"AS400DirectDepositLimitHold"} ,
         };
         return Arrays.asList(substatusList);
     }

    /**
     * Tests for successful "Payroll Submission" when a company having a service-sub-status  is submitting Payroll Data
     * but without any DDinfo.
     * @throws Exception
     */
    @Test
    public void testServiceSubstatus() throws Exception {
        System.out.println("Parameterized substatus is : " + this.subStatusName);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(mDDPSID, SourceSystemCode.QBDT);
        String ein = company.getFedTaxId();
        EntitlementUnit primaryEntitlementUnit = company.getActivePrimaryEntitlementUnit();
        assertNotNull(primaryEntitlementUnit);
        String subscriptionNumber = primaryEntitlementUnit.getEntitlement().getSubscriptionNumber();

        CompanyService companyService = company.getCompanyService(ServiceCode.Cloud);
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        Application.save(companyService);
        companyService = company.getCompanyService(ServiceCode.DirectDeposit);
        companyService.setStatusCd(this.subStatusCode);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2011, 7, 20);
        OFX request = new UsageOFXDataloader().createOFX(mDDPSID, UsageOFXDataloader.OFX_NULL_STRING, ein, subscriptionNumber, PSPDate.getPSPTime(), "1", "Susan", "Butchman", "100", "N");
        String responseMsg = QBDTTestHelper.processOFXRequestSuccess(request);

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = OFXManager.ofxResponseToJava(responseMsg, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        System.out.println("response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY()" + response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getCODE());
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getCODE().equals("0") );
        assertTrue(response.getSIGNONMSGSRSV1().getSONRS().getSTATUS().getSEVERITY().compareTo(QBOFX.MESSAGE_SEVERITY.INFO) == 0);

        PSPToEMSBSDataSyncProcessor aPSPToEMSBSDataSyncProcessor = new PSPToEMSBSDataSyncProcessor(BatchJobProcessor.RunMode.NotUsingFlux, BatchJobType.PSPToEMSBSDataSyncProcessor, "1", "");
        aPSPToEMSBSDataSyncProcessor.executeJob();

        assertEquals("usage", 1, getUsageOnBill(mDDPSID, 2011, 8, 15));
    }
}
