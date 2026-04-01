package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author sgupta36
 */
public class OwnershipTypeTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testFindOwnershipTypes() {
        String ownershipName = "Sole Proprietorship";
        OwnershipType ownershipType = OwnershipType.findOwnershipType(ownershipName);
        Assert.assertTrue(ownershipName.equals(ownershipType.getOwnership()));
        ownershipName = "Non-Profit Organization";
        ownershipType = OwnershipType.findOwnershipType(ownershipName);
        Assert.assertTrue(ownershipName.equals(ownershipType.getOwnership()));
    }
}
