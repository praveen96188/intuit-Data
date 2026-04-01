import com.intuit.ems.psp.adapters.dataadapter.dto.AuthRole;
import com.intuit.ems.psp.adapters.dataadapter.dto.AuthUser;
import com.intuit.ems.psp.adapters.dataadapter.exception.MethodNotAllowed;
import com.intuit.ems.psp.adapters.dataadapter.service.AuthRoleResource;
import com.intuit.ems.psp.adapters.dataadapter.service.AuthUserResource;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.List;

/**
 * Created by Ankit on 10/30/2015.
 */
public class AuthRoleResourceTest {

    @Before
    public void startup() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.beforeEachTest();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();
    }

    @Test
    public void testAuthRoleMethodHappyPath() {
        AuthUserResourceTest authUserResourceTest = new AuthUserResourceTest();
        authUserResourceTest.createAuthUsers();
        AuthUserResource authUserResource = new AuthUserResource();
        AuthRoleResource authRoleResource = new AuthRoleResource();
        //Get list of auth users
        List<AuthUser> authUserList = authUserResource.getAuthUsers();
        //Assert that we have more than 1 auth user in the list
        Assert.assertTrue(authUserList.size() > 0);
        AuthUser authUser = authUserList.get(0);
        List<AuthRole> authRoleList = authRoleResource.getRoles();
        Assert.assertTrue(authRoleList.size() > 0);
        Assert.assertTrue(authRoleList.contains(authUser.getAuthRoles().get(0)));
    }

    @Test
    public void testNotAllowedMethods() {
        AuthRoleResource authRoleResource = new AuthRoleResource();
        try {
            authRoleResource.postToAuthRoles();
            Assert.assertTrue("POST should not be allowed", Boolean.FALSE);
        } catch (MethodNotAllowed ex) {
            Assert.assertEquals("Not Allowed", ex.getMessage());
        }
        try {
            authRoleResource.putToAuthRoles();
            Assert.assertTrue("PUT should not be allowed", Boolean.FALSE);
        } catch (MethodNotAllowed ex) {
            Assert.assertEquals("Not Allowed", ex.getMessage());
        }
        try {
            authRoleResource.deleteAuthRoles();
        } catch (MethodNotAllowed ex) {
            Assert.assertEquals("Not Allowed", ex.getMessage());
        }
    }

}
