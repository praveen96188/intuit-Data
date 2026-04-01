package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.UserRoleDTO;
import com.intuit.sbd.payroll.psp.domain.AuthDomain;
import com.intuit.sbd.payroll.psp.domain.AuthOperation;
import com.intuit.sbd.payroll.psp.domain.AuthRole;
import com.intuit.sbd.payroll.psp.domain.OperationId;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 23, 2008
 * Time: 4:58:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class AddRoleCore extends Process implements IProcess {
    private UserRoleDTO mRoleDto;
    private AuthDomain mDomain;
    private AuthRole mRole;

    public AddRoleCore(UserRoleDTO pRoleDto) {
        this.mRoleDto = pRoleDto;
    }

    public AuthRole getRole() {
        return mRole;
    }

    public ProcessResult validate() {
        ProcessResult validationResult;

        validationResult = mRoleDto.validate();

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        mDomain = Application.findById(AuthDomain.class, mRoleDto.getDomainId());

        if (mDomain == null) {
            validationResult.getMessages().DomainDoesNotExist(EntityName.AuthUser, mRoleDto.getDomainId());
        }

        mRole = AuthRole.findRole(mRoleDto.getRoleId());

        if (mRole != null) {
            validationResult.getMessages().RoleAlreadyExists(EntityName.AuthUser, mRoleDto.getRoleId());
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult<AuthRole> processResult = new ProcessResult<AuthRole>();

        mRole = new AuthRole();

        mRole.setRoleId(mRoleDto.getRoleId());
        mRole.setName(mRoleDto.getName());
        mRole.setDescription(mRoleDto.getDescription());
        mRole.setAuthDomain(mDomain);
        mRole = Application.save(mRole);

        for (OperationId operationId : mRoleDto.getOperationIds()) {
            AuthOperation operation = Application.findById(AuthOperation.class, operationId);
            mRole.addAuthOperation(operation);
        }

        mRole = Application.save(mRole);

        processResult.setResult(mRole);

        return processResult;
    }
}
