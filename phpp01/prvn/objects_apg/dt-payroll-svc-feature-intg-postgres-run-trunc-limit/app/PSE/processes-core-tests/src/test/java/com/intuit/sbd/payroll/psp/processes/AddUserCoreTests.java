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
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 24, 2008
 * Time: 5:02:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddUserCoreTests {
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
    public void addUserSuccess(){
        AuthRole role = addRole();
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna", Arrays.asList(role.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        AuthUser user = (AuthUser)processResult.getResult();

        assertEquals("Corp Id", "rkrishna", user.getCorpId());
        assertEquals("Role Id", role.getRoleId(), user.getPrimaryRole().getRoleId());
        assertEquals("First Name", "Radha", user.getFirstName());
        assertEquals("Last Name", "Krishna", user.getLastName());

        //Assertion for UsersList
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<AuthUser> usersList = AuthUser.findUsers(role.getAuthDomain().getDomainId());
        PayrollServices.commitUnitOfWork();

        assertEquals("Users List", 1, usersList.size());
    }

    private AuthRole addRole(){
        //Add Role & Operations
        PayrollServices.beginUnitOfWork();
        UserRoleDTO roleDto = new UserRoleDTO();
        roleDto.setRoleId("ADMIN");
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
    public void addUser_NullCorpId(){
        AuthRole role = addRole();
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.addUser(null,Arrays.asList(role.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "410", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Corp Id Not Specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addUser_NullRoleId(){
        AuthRole role = addRole();
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna",null,"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "412", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Role Id Not Specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addUser_UserAlreadyExist(){
        AuthRole role = addRole();
        
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna",Arrays.asList(role.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        AuthUser user = (AuthUser)processResult.getResult();

        assertEquals("Corp Id", "rkrishna", user.getCorpId());
        assertEquals("Role Id", role.getRoleId(), user.getPrimaryRole().getRoleId());
        assertEquals("First Name", "Radha", user.getFirstName());
        assertEquals("Last Name", "Krishna", user.getLastName());

        //Add existing User
        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.userManager.addUser("rkrishna",Arrays.asList(role.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "411", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "User rkrishna already exists.",errorMessage.getMessage());        
    }

    @Test
    public void addUser_RoleDoesNotExist(){
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna",Arrays.asList("ABC"),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "413", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Role ABC does not exist.",errorMessage.getMessage());

    }
}
