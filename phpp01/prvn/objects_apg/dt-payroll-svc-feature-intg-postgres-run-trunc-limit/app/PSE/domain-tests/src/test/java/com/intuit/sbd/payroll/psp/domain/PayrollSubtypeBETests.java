package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Jun 30, 2008
 * Time: 6:58:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class PayrollSubtypeBETests {

    @Before
    public void beforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void afterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void happyNoFutureDates() {
        // initial data script loads two sets of PayrollSubtypes... one is effective 2007-01-01 00:00:01 and the other at 2008-01-01 00:00:01
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20080201120000"); // after 1/1/08
        Offering found = Offering.findByQuickBooksSubtypeAndService(EntitlementCode.getQuickBooksSubtypeFromPayrollSubtype(PayrollSubtypeCode.NewBasicUnlimited), ServiceCode.DirectDeposit);
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        Assert.assertNotNull("found a PayrollSubtype", found);
    }

    @Test
    public void happyOneFutureDate() {
        // initial data script loads two sets of PayrollSubtypes... one is effective 2007-01-01 00:00:01 and the other at 2008-01-01 00:00:01
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070201120000"); // after 1/1/07 and before 1/1/08
        Offering found = Offering.findByQuickBooksSubtypeAndService(EntitlementCode.getQuickBooksSubtypeFromPayrollSubtype(PayrollSubtypeCode.NewBasicUnlimited), ServiceCode.DirectDeposit);
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        Assert.assertNotNull("found a PayrollSubtype", found);
    }

    @Test
    public void noneEffective() {
        // initial data script loads two sets of PayrollSubtypes... one is effective 2007-01-01 00:00:01 and the other at 2008-01-01 00:00:01
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20060201120000"); // before 1/1/07
        Offering found = Offering.findByQuickBooksSubtypeAndService(EntitlementCode.getQuickBooksSubtypeFromPayrollSubtype(PayrollSubtypeCode.NewBasicUnlimited), ServiceCode.DirectDeposit);
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        Assert.assertNull("no effective PayrollSubtype", found);
    }
}
