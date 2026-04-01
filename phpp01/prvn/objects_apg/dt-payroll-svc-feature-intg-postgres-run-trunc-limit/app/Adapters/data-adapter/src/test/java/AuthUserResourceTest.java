import com.intuit.ems.psp.adapters.dataadapter.dto.AuthUser;
import com.intuit.ems.psp.adapters.dataadapter.exception.BadRequestException;
import com.intuit.ems.psp.adapters.dataadapter.exception.DataNotFoundException;
import com.intuit.ems.psp.adapters.dataadapter.exception.MethodNotAllowed;
import com.intuit.ems.psp.adapters.dataadapter.service.AuthUserResource;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.UserRoleDTO;
import com.intuit.sbd.payroll.psp.domain.AuthDomain;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Ankit on 10/30/2015.
 */
public class AuthUserResourceTest {

    @Before
    public void startup() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(CalendarUtils.convertToSpcfCalendar(Calendar.getInstance()));
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.beforeEachTest();
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.UnitTest));
        PayrollServicesTest.truncateTables();
        createAuthUsers();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();

    }

    @Test
    public void testAuthUserMethodHappyPath() throws BadRequestException {

        AuthUserResource authUserResource = new AuthUserResource();
        //Get list of auth users
        List<AuthUser> authUserList = authUserResource.getAuthUsers();
        //Assert that we have more than 1 auth user in the list
        Assert.assertTrue(authUserList.size() > 0);

        AuthUser authUser = authUserList.get(0);
        try {

            AuthUser returnedAuthUser = authUserResource.getAuthUserByCorpId(authUser.getCorpId());
            Assert.assertEquals(authUser.getAuthUserSeq(), returnedAuthUser.getAuthUserSeq());

            returnedAuthUser = authUserResource.getAuthUserBySequenceId(authUser.getAuthUserSeq());
            Assert.assertEquals(authUser.getAuthUserSeq(), returnedAuthUser.getAuthUserSeq());

            List<AuthUser> returnedAuthUserList = authUserResource.getAuthUsersByLastModified(authUser.getModifiedDate().getTime());
            Assert.assertTrue(returnedAuthUserList.size() > 0);
        } catch (DataNotFoundException e) {
            Assert.assertTrue("Auth user not found", Boolean.FALSE);
            e.printStackTrace();
        } catch (BadRequestException e) {
            Assert.assertTrue("Incorrect Sequence", Boolean.FALSE);
            e.printStackTrace();
        }
    }

    @Test
    public void testAuthUserNegative() throws BadRequestException {

        AuthUserResource authUserResource = new AuthUserResource();
        //Get list of auth users
        List<AuthUser> authUserList = authUserResource.getAuthUsers();
        //once equals and hashocde is implemented this way to look up will not be needed
        List<String> authUserSeqList = new ArrayList<String>();
        List<String> authUserCorpIdList = new ArrayList<String>();
        for (AuthUser authUser : authUserList) {
            authUserSeqList.add(authUser.getAuthUserSeq());
            authUserCorpIdList.add(authUser.getCorpId());
        }
        //Auth user not found
        SpcfUniqueId spcfUniqueId = SpcfUniqueIdImpl.generateRandomUniqueId();
        //fail safe, max 10000 retries
        int counter = 0;
        while (authUserSeqList.contains(spcfUniqueId.toString()) && counter < 10000) {
            spcfUniqueId = SpcfUniqueIdImpl.generateRandomUniqueId();
            counter++;
        }
        counter = 0;
        Random random = new Random(System.currentTimeMillis());
        Integer randomCorpId = random.nextInt(100000);
        while (authUserCorpIdList.contains(randomCorpId.toString()) && counter < 10000) {
            randomCorpId = random.nextInt(100000);
            counter++;
        }
        try {
            authUserResource.getAuthUserBySequenceId(spcfUniqueId.toString());
            Assert.assertFalse("Auth user found by non existent seq", Boolean.TRUE);
        } catch (DataNotFoundException ex) {
            Assert.assertEquals("AuthUser not found", ex.getMessage());
        } catch (BadRequestException ex) {
            Assert.assertEquals("AuthUser SequenceId incorrect", ex.getMessage());
        }
        try {
            authUserResource.getAuthUserByCorpId(randomCorpId.toString());
            Assert.assertFalse("Auth user found by non existent corp id", Boolean.TRUE);
        } catch (DataNotFoundException ex) {
            Assert.assertEquals("AuthUser not found", ex.getMessage());
        }
    }

    @Test
    public void testAuthUserDelete() throws BadRequestException {

        AuthUserResource authUserResource = new AuthUserResource();
        //Get list of auth users
        List<AuthUser> authUserList = authUserResource.getAuthUsers();
        AuthUser deletedAuthUser = authUserList.get(0);
        try {
            authUserResource.deleteAuthUserBySeqId(deletedAuthUser.getAuthUserSeq().toString());
            authUserList = authUserResource.getAuthUsers();
            Assert.assertFalse("AuthUser list should not contain deleted auth user", authUserList.contains(deletedAuthUser));
        } catch (BadRequestException e) {
            Assert.assertEquals("User not deleted", "AuthUser could not be deleted", e.getMessage());
        } catch (DataNotFoundException e) {
            Assert.assertTrue("AuthUser not found", Boolean.FALSE);
        }


        deletedAuthUser = authUserList.get(1);
        try {
            authUserResource.deleteAuthUserByCorpId(deletedAuthUser.getCorpId().toString());
            authUserList = authUserResource.getAuthUsers();
            Assert.assertFalse("AuthUser list should not contain deleted auth user", authUserList.contains(deletedAuthUser));
        } catch (DataNotFoundException e) {
            Assert.assertTrue("AuthUser not found", Boolean.FALSE);
        } catch (BadRequestException e) {
            Assert.assertEquals("User not deleted", "AuthUser could not be deleted", e.getMessage());
        }

    }

    @Test
    public void testAuthUserRoleDelete() throws BadRequestException {
        com.intuit.sbd.payroll.psp.domain.AuthRole role = addRole("FRG Rep1");
        com.intuit.sbd.payroll.psp.domain.AuthRole role1 = addRole("QBOE-IOP Rep1");
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("rkrishna1", Arrays.asList(role.getRoleId(), role1.getRoleId()), "Radha1", "Krishna1");
        PayrollServices.commitUnitOfWork();
        AuthUserResource authUserResource = new AuthUserResource();
        //Get list of auth users
        List<AuthUser> authUserList = authUserResource.getAuthUsers();
        AuthUser deletedAuthUser = authUserList.get(0);
        try {
            authUserResource.deleteAuthUserRoleByCorpId("rkrishna1", "QBOE-IOP Rep1");
        } catch (BadRequestException e) {
            Assert.assertEquals("User not deleted", "AuthUser could not be deleted", e.getMessage());
        } catch (DataNotFoundException e) {
            Assert.assertTrue("AuthUser not found", Boolean.FALSE);
        }

        try {
            authUserResource.deleteAuthUserRoleByCorpId("50000048570", "FRG Rep");
        } catch (BadRequestException e) {
            Assert.assertEquals("User not deleted", "AuthUser could not be deleted", e.getMessage());
        } catch (DataNotFoundException e) {
            Assert.assertTrue("AuthUser not found", Boolean.FALSE);
        }

    }

    @Test
    public void testAuthUserRoleDeleteNegative() throws BadRequestException {
        ;
        AuthUserResource authUserResource = new AuthUserResource();
        //Get list of auth users
        List<AuthUser> authUserList = authUserResource.getAuthUsers();
        AuthUser deletedAuthUser = authUserList.get(0);
        try {
            authUserResource.deleteAuthUserRoleByCorpId("rkrishna", "FRG Rep");
        } catch (BadRequestException e) {
            Assert.assertEquals("User not deleted", "AuthRole not found", e.getMessage());
        } catch (DataNotFoundException e) {
            Assert.assertTrue("AuthUser not found", Boolean.FALSE);
        }
        try {
            authUserResource.deleteAuthUserRoleByCorpId(deletedAuthUser.getCorpId(), "DirectDepositManager");
        } catch (BadRequestException e) {
            Assert.assertEquals("User not deleted", "AuthRole not found", e.getMessage());
        } catch (DataNotFoundException e) {
            Assert.assertTrue("AuthUser not found", Boolean.FALSE);
        }


    }

    @Test
    public void testAuthUserDeleteNegative() throws BadRequestException {
        AuthUserResource authUserResource = new AuthUserResource();
        //Get list of auth users
        List<AuthUser> authUserList = authUserResource.getAuthUsers();
        int initialUserListSize = authUserList.size();
        //Auth user not found
        SpcfUniqueId spcfUniqueId = SpcfUniqueIdImpl.generateRandomUniqueId();
        List<String> authUserSeqList = new ArrayList<String>();
        List<String> authUserCorpIdList = new ArrayList<String>();
        for (AuthUser authUser : authUserList) {
            authUserSeqList.add(authUser.getAuthUserSeq());
            authUserCorpIdList.add(authUser.getCorpId());
        }
        //fail safe, max 10000 retries
        int counter = 0;
        while (authUserSeqList.contains(spcfUniqueId.toString()) && counter < 10000) {
            spcfUniqueId = SpcfUniqueIdImpl.generateRandomUniqueId();
            counter++;
        }
        counter = 0;
        Random random = new Random(System.currentTimeMillis());
        Integer randomCorpId = random.nextInt(100000);
        while (authUserCorpIdList.contains(randomCorpId.toString()) && counter < 10000) {
            randomCorpId = random.nextInt(100000);
            counter++;
        }

        try {
            authUserResource.deleteAuthUserBySeqId(spcfUniqueId.toString());
            Assert.assertTrue("AuthUser not found", Boolean.FALSE);
        } catch (DataNotFoundException e) {
            Assert.assertEquals("AuthUser not found", e.getMessage());
        }

        try {
            authUserResource.deleteAuthUserByCorpId(randomCorpId.toString());
            Assert.assertTrue("AuthUser not found", Boolean.FALSE);
        } catch (DataNotFoundException e) {
            Assert.assertEquals("AuthUser not found", e.getMessage());
        }
        authUserList = authUserResource.getAuthUsers();
        Assert.assertTrue("AuthUser list size should not have changes", initialUserListSize == authUserList.size());
    }

    @Test
    public void testNotAllowedMethods() {
        AuthUserResource authUserResource = new AuthUserResource();
        try {
            authUserResource.postToAuthUsers();
            Assert.assertTrue("POST should not be allowed", Boolean.FALSE);
        } catch (MethodNotAllowed ex) {
            Assert.assertEquals("Not Allowed", ex.getMessage());
        }
        try {
            authUserResource.putToAuthUsers();
            Assert.assertTrue("PUT should not be allowed", Boolean.FALSE);
        } catch (MethodNotAllowed ex) {
            Assert.assertEquals("Not Allowed", ex.getMessage());
        }
    }

    public void createAuthUsers() {
        com.intuit.sbd.payroll.psp.domain.AuthRole role = addRole("ADMIN");
        //Add User
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("rkrishna", Arrays.asList(role.getRoleId()), "Radha", "Krishna");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.updateUserSetting("rkrishna", "display_inline_settings", "true");
        PayrollServices.commitUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.AuthRole role1 = addRole("QBOE-IOP Rep");
        //Add User
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("mkrishna", Arrays.asList(role1.getRoleId()), "Madhav", "Krishna");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.updateUserSetting("mkrishna", "display_inline_settings", "true");
        PayrollServices.commitUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.AuthRole role2 = addRole("FRG Rep");
        //Add User
        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.addUser("50000048570", Arrays.asList(role2.getRoleId(), role1.getRoleId()), "Kapil", "Muraka");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollServices.userManager.updateUserSetting("50000048570", "display_inline_settings", "true");
        PayrollServices.commitUnitOfWork();


    }

    private com.intuit.sbd.payroll.psp.domain.AuthRole addRole(String pRoleId) {
        //Add Role & Operations
        PayrollServices.beginUnitOfWork();
        UserRoleDTO roleDto = new UserRoleDTO();
        roleDto.setRoleId(pRoleId);
        roleDto.setName("This user has access to all features in the DD UI Rep App");

        DomainEntitySet<AuthDomain> domains = Application.findObjects(AuthDomain.class);
        roleDto.setDomainId(domains.get(0).getDomainId());

        List<OperationId> operations = new ArrayList<OperationId>();
        operations.add(OperationId.AccessApplication);

        roleDto.setOperationIds(operations);

        ProcessResult processResult = PayrollServices.userManager.addRole(roleDto);
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        PayrollServices.beginUnitOfWork();

        com.intuit.sbd.payroll.psp.domain.AuthRole role = com.intuit.sbd.payroll.psp.domain.AuthRole.findRole(roleDto.getRoleId());
        PayrollServices.commitUnitOfWork();

        return role;
    }
}
