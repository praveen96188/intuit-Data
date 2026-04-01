package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.UserRoleDTO;
import com.intuit.sbd.payroll.psp.domain.AuthDomain;
import com.intuit.sbd.payroll.psp.domain.AuthRole;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 24, 2008
 * Time: 12:11:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddRoleCoreTests {
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
    public void addRoleSuccess(){
        PayrollServices.beginUnitOfWork();
        UserRoleDTO roleDto = new UserRoleDTO();
        roleDto.setRoleId("ADMIN");
        roleDto.setName("This user has access to all features in the DD UI Rep App");

        DomainEntitySet<AuthDomain> domains = Application.findObjects(AuthDomain.class);
        roleDto.setDomainId(domains.get(0).getDomainId());

        DomainEntitySet<AuthRole> roleList = AuthRole.findRoles(roleDto.getDomainId());
        int roleListSize = roleList.size();

        List<OperationId> operations = new ArrayList<OperationId>();
        operations.add(OperationId.AccessApplication);
        operations.add(OperationId.FundingModelUpdate);

        roleDto.setOperationIds(operations);

        ProcessResult processResult = PayrollServices.userManager.addRole(roleDto);
        PayrollServices.commitUnitOfWork();

        // Verify that no  validation errors have been returned
        assertEquals(0, processResult.getMessages().size());
        PayrollServices.beginUnitOfWork();

        AuthRole role = AuthRole.findRole(roleDto.getRoleId());
        assertRolesEqual(roleDto, role);

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Assertion for the RoleList for the given Domain Id
        DomainEntitySet<AuthRole> newRoleList = AuthRole.findRoles(roleDto.getDomainId());
        PayrollServices.commitUnitOfWork();
        assertEquals("Roles Size",roleListSize+1 , newRoleList.size());
    }

    @Test
    public void addRole_RoleIdNotSpecified(){
        PayrollServices.beginUnitOfWork();
        UserRoleDTO roleDto = new UserRoleDTO();
        roleDto.setRoleId(null);
        roleDto.setName("This user has access to all features in the DD UI Rep App");

        DomainEntitySet<AuthDomain> domains = Application.findObjects(AuthDomain.class);
        roleDto.setDomainId(domains.get(0).getDomainId());

        List<OperationId> operations = new ArrayList<OperationId>();
        operations.add(OperationId.AccessApplication);

        roleDto.setOperationIds(operations);

        ProcessResult processResult = PayrollServices.userManager.addRole(roleDto);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "412", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Role Id Not Specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addRole_DomainIdNotSpecified(){
        PayrollServices.beginUnitOfWork();
        UserRoleDTO roleDto = new UserRoleDTO();
        roleDto.setRoleId("ADMIN");
        roleDto.setName("This user has access to all features in the DD UI Rep App");
        roleDto.setDomainId(null);

        List<OperationId> operations = new ArrayList<OperationId>();
        operations.add(OperationId.AccessApplication);

        roleDto.setOperationIds(operations);

        ProcessResult processResult = PayrollServices.userManager.addRole(roleDto);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "5001", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "DomainId has invalid value",
                errorMessage.getMessage());
    }

    @Test
    public void addRole_OperationIdNotSpecified(){
        PayrollServices.beginUnitOfWork();
        UserRoleDTO roleDto = new UserRoleDTO();
        roleDto.setRoleId("ADMIN");
        roleDto.setName("This user has access to all features in the DD UI Rep App");
        DomainEntitySet<AuthDomain> domains = Application.findObjects(AuthDomain.class);
        roleDto.setDomainId(domains.get(0).getDomainId());
        roleDto.setOperationIds(null);

        ProcessResult processResult = PayrollServices.userManager.addRole(roleDto);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "415", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Operation Id Not Specified.",
                errorMessage.getMessage());
    }

    @Test
    public void addRole_DomainIdDoesNotExist(){
        PayrollServices.beginUnitOfWork();
        UserRoleDTO roleDto = new UserRoleDTO();
        roleDto.setRoleId("ADMIN");
        roleDto.setName("This user has access to all features in the DD UI Rep App");
        roleDto.setDomainId("2");
        List<OperationId> operations = new ArrayList<OperationId>();
        operations.add(OperationId.AccessApplication);
        roleDto.setOperationIds(operations);

        ProcessResult processResult = PayrollServices.userManager.addRole(roleDto);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "416", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Domain 2 does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void addRole_RoleIdAlreadyExist(){
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
        processResult = PayrollServices.userManager.addRole(roleDto);
        PayrollServices.commitUnitOfWork();

        Assert.assertEquals("Messages size", 1, processResult.getMessages().size());
        Message errorMessage = processResult.getMessages().get(0);
        Assert.assertEquals("Error message code", "417", errorMessage.getMessageCode());
        Assert.assertEquals("Error message", "Role ADMIN already exists.",
                errorMessage.getMessage());
    }

    private void assertRolesEqual(UserRoleDTO pRoleDto, AuthRole pRole){
        assertEquals("Role ID", pRoleDto.getRoleId(), pRole.getRoleId());
        assertEquals("Domain Id", pRoleDto.getDomainId(), pRole.getAuthDomain().getDomainId());
        assertEquals("Operations Size ", pRoleDto.getOperationIds().size(), pRole.getAuthOperationCollection().size());
    }
}
