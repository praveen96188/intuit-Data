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
 * Date: Jun 25, 2008
 * Time: 11:18:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteUserCoreTests {
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
    public void deleteUserSuccess(){
        AuthRole role = addRole("ADMIN");
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.addUser("rkrishna", Arrays.asList(role.getRoleId()),"Radha","Krishna");
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        PayrollServices.beginUnitOfWork();
        ProcessResult processResult2 = PayrollServices.userManager.updateUserSetting("rkrishna", "display_inline_settings", "true");
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult2.getMessages().size());

        //Delete User
        PayrollServices.beginUnitOfWork();
        AuthUser user = (AuthUser)processResult.getResult();
        processResult = PayrollServices.userManager.deleteUser(user.getCorpId());
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        PayrollServices.beginUnitOfWork();
        AuthUser deletedUser = AuthUser.findUser(user.getCorpId());
        PayrollServices.commitUnitOfWork();
        assertEquals("Deleted User", null, deletedUser);
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
    public void addUser_NullCorpId(){
        //Add User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.deleteUser(null);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "410", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Corp Id Not Specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addUser_UserDoesNotExist(){

        //Update existing User
        PayrollServices.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.userManager.deleteUser("rkrishna");
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "414", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "User rkrishna does not exist.",errorMessage.getMessage());
    }

    

}
