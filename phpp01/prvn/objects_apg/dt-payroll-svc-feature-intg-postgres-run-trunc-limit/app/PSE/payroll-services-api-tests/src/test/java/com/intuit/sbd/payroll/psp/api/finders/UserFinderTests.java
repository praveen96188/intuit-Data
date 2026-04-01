package com.intuit.sbd.payroll.psp.api.finders;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.AuthRole;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

/**
 * User: dweinberg
 * Date: Mar 26, 2009
 * Time: 12:32:57 PM
 */
public class UserFinderTests {
    
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testFindUsersByOperation() {
        PayrollServices.beginUnitOfWork();

        AuthRole role1 = AuthRole.findRole("FRGRep");
        AuthRole role2 = AuthRole.findRole("Admin");

        PayrollServices.userManager.addUser("1-1", Arrays.asList(role1.getRoleId()),"1","1");
        PayrollServices.userManager.addUser("1-2",Arrays.asList(role1.getRoleId()),"1","2");
        PayrollServices.userManager.addUser("1-3",Arrays.asList(role1.getRoleId()),"1","3");
        PayrollServices.userManager.addUser("1-4",Arrays.asList(role1.getRoleId()),"1","4");
        PayrollServices.userManager.addUser("2-1",Arrays.asList(role2.getRoleId()),"1","2");
        PayrollServices.userManager.addUser("2-2",Arrays.asList(role2.getRoleId()),"1","3");

        PayrollServices.commitUnitOfWork();


        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AuthUser> users =  AuthUser.findUsersByOperation("AccessApplication");
        Assert.assertEquals("All users found by AccessApplication",6,users.size());
        users =  AuthUser.findUsersByOperation("AuthRemoveUsers");
        Assert.assertEquals("Only operators found by AuthRemoveUsers",2,users.size());
        PayrollServices.commitUnitOfWork();
    }
}
