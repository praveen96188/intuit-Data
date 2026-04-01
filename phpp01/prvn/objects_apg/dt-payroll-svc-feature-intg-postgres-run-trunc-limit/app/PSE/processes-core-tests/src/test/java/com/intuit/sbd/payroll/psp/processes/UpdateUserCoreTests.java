package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.UserRoleDTO;
import com.intuit.sbd.payroll.psp.domain.AuthDomain;
import com.intuit.sbd.payroll.psp.domain.AuthRole;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 25, 2008
 * Time: 10:49:49 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateUserCoreTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void updateUserSuccess(){
        AuthRole role = addRole("ADMIN");
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna", Arrays.asList(role.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        role = addRole("QATEST");
        
        //Update User
        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser)processResult.getResult();
        processResult = PayrollServices.userManager.updateUser(user.getId().toString() , "rkrishna1",Arrays.asList(role.getRoleId()),"Radha","KrishnaPrasad");
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        AuthUser updatedUser = (AuthUser)processResult.getResult();

        assertEquals("Corp Id", "rkrishna1", updatedUser.getCorpId());
        assertEquals("Role Id", role.getRoleId(), updatedUser.getPrimaryRole().getRoleId());
        assertEquals("First Name", "Radha", updatedUser.getFirstName());
        assertEquals("Last Name", "KrishnaPrasad", updatedUser.getLastName());
    }

    private AuthRole addRole(String pRoleId){
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

        AuthRole role = AuthRole.findRole(roleDto.getRoleId());
        PayrollServices.commitUnitOfWork();

        return role;
    }

    @Test
    public void addUser_NullUserGUID(){
        AuthRole role = addRole("ADMIN");
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.updateUser(null, "abc", Arrays.asList(role.getRoleId()), "Radha", "Krishna");
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        System.out.println("MSG" + errorMessage);
        Assert.assertEquals("Error message code", "5002", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Required 'User Sequence Id' input is missing or blank",
                            errorMessage.getMessage());
    }

    @Test
    public void addUser_NullCorpId(){
        AuthRole role = addRole("ADMIN");
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.updateUser("111", null, Arrays.asList(role.getRoleId()), "Radha", "Krishna");
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "410", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Corp Id Not Specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addUser_NullRoleId(){
        //Update existing User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.updateUser("111","rkrishna",null,"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "412", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Role Id Not Specified.",
                errorMessage.getMessage());
    }
    
    @Test
    public void addUser_UserDoesNotExist(){
        AuthRole role = addRole("ADMIN");

        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna",Arrays.asList(role.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        //Update existing User
        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser)processResult.getResult();
        processResult = PayrollServices.userManager.updateUser("5049f71a-7d11-4956-8a60-b21c1f9a8f0e","rkrishna1",Arrays.asList(role.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "414", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "User 5049f71a-7d11-4956-8a60-b21c1f9a8f0e does not exist.",errorMessage.getMessage());
    }
    
    @Test
    public void addUser_RoleDoesNotExist(){
        AuthRole role = addRole("ADMIN");
        
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna",Arrays.asList(role.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        //Update existing User
        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser)processResult.getResult();
        processResult = PayrollServices.userManager.updateUser(user.getId().toString(),"rkrishna1",Arrays.asList("ABC"),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "413", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Role ABC does not exist.",errorMessage.getMessage());
    }

    @Test
    public void updateUser_multipleRoles() {
        AuthRole role = addRole("ADMIN");
        AuthRole role2 = addRole("ADMIN2");
        AuthRole role3 = addRole("ADMIN3");
        //Add User
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.userManager.addUser("rkrishna", Arrays.asList(role.getRoleId(), role2.getRoleId()), "Radha", "Krishna"));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AuthUser user = AuthUser.findUser("rkrishna");

        assertTrue(user.getAuthRoleCollection().contains(role));
        assertTrue(user.getAuthRoleCollection().contains(role2));
        assertFalse(user.getAuthRoleCollection().contains(role3));

        //Update User

        assertSuccess(PayrollServices.userManager.updateUser(user.getId().toString() , "rkrishna1",Arrays.asList(role.getRoleId(), role3.getRoleId()),"Radha","KrishnaPrasad"));
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        PayrollServices.beginUnitOfWork();
        assertNull(AuthUser.findUser("rkrishna"));
        user = AuthUser.findUser("rkrishna1");
        assertNotNull(user);

        assertEquals("Corp Id", "rkrishna1", user.getCorpId());
        assertEquals("First Name", "Radha", user.getFirstName());
        assertEquals("Last Name", "KrishnaPrasad", user.getLastName());

        assertTrue(user.getAuthRoleCollection().contains(role));
        assertFalse(user.getAuthRoleCollection().contains(role2));
        assertTrue(user.getAuthRoleCollection().contains(role3));
    }
    @Test
    public void updateUser_lastModified() {
        AuthRole role = addRole("ADMIN");
        AuthRole role2 = addRole("ADMIN2");
        AuthRole role3 = addRole("ADMIN3");
        //Add User
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.userManager.addUser("rkrishna", Arrays.asList(role.getRoleId(), role2.getRoleId()), "Radha", "Krishna"));

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        AuthUser user = AuthUser.findUser("rkrishna");
        SpcfCalendar spcfCalendar = user.getModifiedDate();
        assertTrue(user.getAuthRoleCollection().contains(role));
        assertTrue(user.getAuthRoleCollection().contains(role2));
        assertFalse(user.getAuthRoleCollection().contains(role3));

        //Update User

        assertSuccess(PayrollServices.userManager.updateUser(user.getId().toString() , "rkrishna1",Arrays.asList(role.getRoleId(), role3.getRoleId()),"Radha","KrishnaPrasad"));
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        PayrollServices.beginUnitOfWork();
        assertNull(AuthUser.findUser("rkrishna"));
        user = AuthUser.findUser("rkrishna1");
        assertNotNull(user);
        SpcfCalendar spcfCalendarModified = user.getModifiedDate();
        assertEquals("Corp Id", "rkrishna1", user.getCorpId());
        assertEquals("First Name", "Radha", user.getFirstName());
        assertEquals("Last Name", "KrishnaPrasad", user.getLastName());
        assertNotSame(spcfCalendar,spcfCalendarModified);
        assertTrue(user.getAuthRoleCollection().contains(role));
        assertFalse(user.getAuthRoleCollection().contains(role2));
        assertTrue(user.getAuthRoleCollection().contains(role3));
    }
}
