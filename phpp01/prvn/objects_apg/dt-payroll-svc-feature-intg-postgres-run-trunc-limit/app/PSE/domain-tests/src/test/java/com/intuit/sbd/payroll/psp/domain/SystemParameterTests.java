package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Mar 5, 2012
 * Time: 9:45:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class SystemParameterTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void validateDecryptParameter() {
        //
        // The plain-text password we'll use is:  !TestP@ssw0rd!
        // It's encrypted form is: ENC(YA8fy4F8XyVauv5P6FwyTzy51YWk7WftWOLYGb3FqVfJY1fwRHInTFpydoLFR4wf5C88Ojer8/SBvN+YVYxxLw==)
        //

        String oldValue = SystemParameter.findStringValue(SystemParameter.Code.PSPUI_LDAP_PASSWORD);

        try {
            try {
                Application.beginUnitOfWork();
                SystemParameter.update(SystemParameter.Code.PSPUI_LDAP_PASSWORD, "IDPS(pspui-ldap-password)");
                Application.commitUnitOfWork();
            } finally {
                Application.rollbackUnitOfWork();
            }

            SystemParameter param = SystemParameter.findSystemParameter(SystemParameter.Code.PSPUI_LDAP_PASSWORD);

            String rawValue = param.getSystemParameterValue();
            assertEquals("IDPS(pspui-ldap-password)", rawValue);

            String decryptedValue1 = param.getDecryptedSystemParameterValue();
            assertEquals("!TestP@ssw0rd!", decryptedValue1);

            String decryptedValue2 = SystemParameter.findValue(SystemParameter.Code.PSPUI_LDAP_PASSWORD);
            assertEquals("!TestP@ssw0rd!", decryptedValue2);
        } finally {
            try {
                Application.beginUnitOfWork();
                SystemParameter.update(SystemParameter.Code.PSPUI_LDAP_PASSWORD, oldValue);
                Application.commitUnitOfWork();
            } finally {
                Application.rollbackUnitOfWork();
            }
        }
    }
}
