package com.intuit.sbd.payroll.psp.processes;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.UserRoleDTO;
import com.intuit.sbd.payroll.psp.domain.AuthDomain;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.domain.AuthRole;
import com.intuit.sbd.payroll.psp.domain.AuthOperation;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.DomainEntitySet;

import java.util.List;
import java.util.ArrayList;

import junit.framework.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 25, 2008
 * Time: 11:33:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateRoleCoreTests {
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
    public void updateRoleSuccess() {
        UserRoleDTO roleDto = addRole();

        //Update Role
        PayrollServices.beginUnitOfWork();
        List<OperationId> operations = new ArrayList<OperationId>();
        operations.add(OperationId.BankReturnUpdate);

        roleDto.setOperationIds(operations);
        ProcessResult processResult = PayrollServices.userManager.updateRole(roleDto);
        AuthRole updatedRole = AuthRole.findRole(roleDto.getRoleId());
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());

        assertEquals("Role ID", roleDto.getRoleId(), updatedRole.getRoleId());
        assertEquals("Domain Id", roleDto.getDomainId(), updatedRole.getAuthDomain().getDomainId());
        assertEquals("Operations Size ", 1, updatedRole.getAuthOperationCollection().size());
        for (AuthOperation operation : updatedRole.getAuthOperationCollection()) {
            assertEquals("Associated Operation Id", OperationId.BankReturnUpdate, operation.getOperationId());
        }
    }

    private UserRoleDTO addRole(){
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
        AuthRole role = AuthRole.findRole(roleDto.getRoleId());
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());
        assertEquals("Role ID", roleDto.getRoleId(), role.getRoleId());
        assertEquals("Domain Id", roleDto.getDomainId(), role.getAuthDomain().getDomainId());
        assertEquals("Operations Size ", 1, role.getAuthOperationCollection().size());

        return roleDto;
    }

    @Test
    public void updateRole_RoleIdNotSpecified(){
        //Add Role
        UserRoleDTO roleDto = addRole();

        //Update Role
        PayrollServices.beginUnitOfWork();
        roleDto.setRoleId(null);

        ProcessResult processResult = PayrollServices.userManager.updateRole(roleDto);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "412", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Role Id Not Specified.",
                errorMessage.getMessage());        
    }

    @Test
    public void updateRole_DomainIdNotSpecified(){
        //Add Role
        UserRoleDTO roleDto = addRole();

        //Update Role
        PayrollServices.beginUnitOfWork();
        roleDto.setDomainId(null);

        ProcessResult processResult = PayrollServices.userManager.updateRole(roleDto);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "DomainId has invalid value",
                errorMessage.getMessage());
    }

    @Test
    public void updateRole_OperationIdNotSpecified(){
        //Add Role
        UserRoleDTO roleDto = addRole();

        //Update Role
        PayrollServices.beginUnitOfWork();
        roleDto.setOperationIds(null);

        ProcessResult processResult = PayrollServices.userManager.updateRole(roleDto);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "415", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Operation Id Not Specified.",
                errorMessage.getMessage());
    }

    @Test
    public void updateRole_DomainIdDoesNotExist(){
        //Add Role
        UserRoleDTO roleDto = addRole();

        //Update Role
        PayrollServices.beginUnitOfWork();
        roleDto.setDomainId("2");

        ProcessResult processResult = PayrollServices.userManager.updateRole(roleDto);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "416", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Domain 2 does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void updateRole_RoleDoesNotExist(){
        //Add Role
        UserRoleDTO roleDto = addRole();

        //Update Role
        PayrollServices.beginUnitOfWork();
        roleDto.setRoleId("ADMIN1");
        ProcessResult processResult = PayrollServices.userManager.updateRole(roleDto);
        PayrollServices.commitUnitOfWork();
        
        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "413", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Role ADMIN1 does not exist.",
                errorMessage.getMessage());
    }
    
    
}
