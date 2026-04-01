package com.intuit.sbd.payroll.psp.adapters.cdmadapter;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.finders.PaystubFinder;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.managers.PayrollCompanyManager;
import com.intuit.sbd.payroll.psp.adapters.cdmadapter.util.AssertHelper;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Entitlement;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.schema.ems.v3.*;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Author: Sriram Nutakki
 * Date created: 5/3/13
 */
public class PayrollSubscriptionTests {

    private static String psid = "99000123";

    @Before
    public void startUp() {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void shutdown() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testAddOnServicesAndEntitlements() {

        // Setup
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.WorkersComp);
        DataLoadServices.addCompanyPIN(company, DataLoadServices.PIN);
        EntitlementUnit entitlementUnit = DataLoadServices.addEntitlementUnit(company, "1234567890", "123456", EditionType.Basic, NumberOfEmployeesType.UNLIMITED, DataLoadServices.AssetItemNumber.DIY_YEARLY, PSPDate.getPSPTime(), "4263", "Visa", "03/16", "89511", "John Doe", "test@intuit.com", PSPDate.getPSPTime());
        Entitlement entitlement = entitlementUnit.getEntitlement();
        String subscriptionNumber = entitlement.getSubscriptionNumber();
        String ein = entitlementUnit.getFedTaxId();
        PayrollCompanyManager payrollCompanyManager = new PayrollCompanyManager();
        PayrollCompany payrollCompany = payrollCompanyManager.getPayrollCompany(ein, subscriptionNumber);
        Assert.assertNotNull(payrollCompany);
        PayrollSubscription ps = payrollCompany.getPayrollSubscription();
        Assert.assertNotNull(ps);

        // AddOnService - Validations
        Assert.assertNotNull(ps.getAddOnService());
        Assert.assertTrue(ps.getAddOnService().size() >= 2);
        AddOnService cloudService = null;
        AddOnService workerCompService = null;

        for (AddOnService service : ps.getAddOnService()) {
            if (service.getName().equals(ServiceCode.Cloud.name())) {
                cloudService = service;
            } else if (service.getName().equals(ServiceCode.WorkersComp.name())) {
                workerCompService = service;
            }
        }
        Assert.assertNotNull(cloudService);
        Assert.assertNotNull(workerCompService);
        Assert.assertNotNull(cloudService.getStatus());
        Assert.assertNotNull(workerCompService.getStatus());

        // Entitlement - Validations
        Assert.assertNotNull(ps.getEntitlement());
        Assert.assertTrue(ps.getEntitlement().size() == 1);
        com.intuit.schema.ems.v3.Entitlement entitlementOut = ps.getEntitlement().get(0);
        Assert.assertNotNull(entitlementOut);
        Assert.assertNotNull(entitlementOut.getAssetItemCode());
        Assert.assertNotNull(entitlementOut.getEditionType());
        Assert.assertEquals(EditionType.Basic.name(), entitlementOut.getEditionType());
        Assert.assertEquals(AssetItemCode.DIY.name(), entitlementOut.getAssetItemCode());
        Assert.assertEquals(true, entitlementOut.isActive());
        Assert.assertEquals(true, entitlementOut.isPrimary());
    }
}
