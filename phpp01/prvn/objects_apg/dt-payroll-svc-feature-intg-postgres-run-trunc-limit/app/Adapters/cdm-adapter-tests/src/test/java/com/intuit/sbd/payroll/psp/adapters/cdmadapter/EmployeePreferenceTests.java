package com.intuit.sbd.payroll.psp.adapters.cdmadapter;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.PstubEmployeePreference;
import com.intuit.sbd.payroll.psp.processes.util.VmpTestUtil;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created with IntelliJ IDEA.
 * User: srikanthm180
 * Date: 2/14/13
 * Time: 1:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmployeePreferenceTests {
    private static final String psid = "99000123";
    private static final String realmId = "123456";

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
    public void testCreateNewEmployeePreference() {
        String paystubAmount = "1000.00";
        Employee employee = VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid, realmId, paystubAmount);

        PstubEmployeePreference pspPref = new PstubEmployeePreference();
        PstubEmployeePreference eePref = new PstubEmployeePreference();
        eePref.setEmployee(employee);
        eePref.setAppName("VMP");
        eePref.setPreferenceName("Email");
        eePref.setPreferenceValue("Yes");
        pspPref.createEmployeePreference(eePref);

        pspPref = pspPref.getEmployeePreferencesByApp(employee, "VMP").get(0);

        Assert.assertEquals(eePref.getPreferenceValue(), pspPref.getPreferenceValue());
        Assert.assertEquals(eePref.getPreferenceName(), pspPref.getPreferenceName());

    }

    @Test
    public void testUpdateEmployeePreference() {
        String paystubAmount = "1000.00";
        Employee employee = VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid, realmId, paystubAmount);

        PstubEmployeePreference pspPref = new PstubEmployeePreference();
        PstubEmployeePreference eePref = new PstubEmployeePreference();
        eePref.setEmployee(employee);
        eePref.setAppName("VMP");
        eePref.setPreferenceName("Email");
        eePref.setPreferenceValue("Yes");
        pspPref.createEmployeePreference(eePref);

        eePref = pspPref.getEmployeePreferencesByApp(employee, "VMP").get(0);

        eePref.setPreferenceValue("No");
        pspPref.updateEmployeePreference(eePref);

       String actualValue = pspPref.getEmployeePreferenceByName(employee, "VMP", "Email");

        Assert.assertEquals("No", actualValue);

    }
    @Test
    public void testDeleteEmployeePreference() {
        String paystubAmount = "1000.00";
        Employee employee = VmpTestUtil.setupCompanyCreateEmployeeWithPaystubAndAssociateWithConsumerRealmId(psid, realmId, paystubAmount);

        PstubEmployeePreference pspPref = new PstubEmployeePreference();
        PstubEmployeePreference eePref = new PstubEmployeePreference();
        eePref.setEmployee(employee);
        eePref.setAppName("VMP");
        eePref.setPreferenceName("Email");
        eePref.setPreferenceValue("Yes");
        pspPref.createEmployeePreference(eePref);

        eePref = pspPref.getEmployeePreferencesByApp(employee, "VMP").get(0);

        eePref.setPreferenceValue("No");
        pspPref.deleteEmployeePreference(eePref);

        String actualValue = pspPref.getEmployeePreferenceByName(employee, "VMP", "Email");

        Assert.assertNull(actualValue);

    }

}
