package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 2, 2010
 * Time: 10:37:24 AM
 */
public class EntitlementTests {
     @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testDuplicateEntitlementCodes() {
        PayrollServices.beginUnitOfWork();

        DomainEntitySet<EntitlementCode> entitlementCodes = PayrollServices.entityFinder.find(EntitlementCode.class);

        StringBuilder duplicate = new StringBuilder();

        for (EntitlementCode entitlementCode : entitlementCodes) {
            try {
                EntitlementCode.findEntitlementCode(entitlementCode.getAssetItemNumber(), entitlementCode.getEditionType(), entitlementCode.getNumberOfEmployeesType());
            } catch (Exception e) {
                duplicate.append("Duplicate entitlement code found in populate_entitlement.sql ")
                        .append(entitlementCode.getAssetItemCd()).append(":").append(entitlementCode.getEditionType()).append(":").append(entitlementCode.getNumberOfEmployeesType())
                        .append("\n");
            }
        }

        if (duplicate.length() > 0) {
            fail(duplicate.toString());
        }

        PayrollServices.rollbackUnitOfWork();
    }
}
