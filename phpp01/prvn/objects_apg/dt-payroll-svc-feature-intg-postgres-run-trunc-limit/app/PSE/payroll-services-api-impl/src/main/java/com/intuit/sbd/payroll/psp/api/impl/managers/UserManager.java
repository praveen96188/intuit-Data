package com.intuit.sbd.payroll.psp.api.impl.managers;

import com.intuit.sbd.payroll.psp.api.dtos.UserRoleDTO;
import com.intuit.sbd.payroll.psp.api.managers.IUserManager;
import com.intuit.sbd.payroll.psp.domain.AuthRole;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.UserSetting;
import com.intuit.sbd.payroll.psp.processes.*;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 24, 2008
 * Time: 12:13:47 PM
 */


public class UserManager implements IUserManager {

    public ProcessResult<AuthRole> addRole(UserRoleDTO pRoleDto) {
        AddRoleCore processCore = new AddRoleCore(pRoleDto);
        ProcessResult<AuthRole> processResult = processCore.execute();

        processResult.setResult(processCore.getRole());

        return processResult;
    }

    public ProcessResult<AuthRole> updateRole(UserRoleDTO pRoleDto) {
        UpdateRoleCore processCore = new UpdateRoleCore(pRoleDto);
        ProcessResult<AuthRole> processResult = processCore.execute();

        processResult.setResult(processCore.getRole());

        return processResult;
    }

    public ProcessResult<AuthUser> addUser(String pCorpId, List<String> pRoleIds, String pFirstName, String pLastName) {
        AddUserCore processCore = new AddUserCore(pCorpId, pRoleIds, pFirstName, pLastName);

        ProcessResult<AuthUser> processResult = processCore.execute();

        processResult.setResult(processCore.getUser());
        return processResult;
    }

    public ProcessResult<AuthUser> updateUser(String pUserSeqId, String pCorpId, List<String> pRoleIds, String pFirstName, String pLastName) {
        UpdateUserCore processCore = new UpdateUserCore(pUserSeqId, pCorpId, pRoleIds, pFirstName, pLastName);

        ProcessResult<AuthUser> processResult = processCore.execute();

        processResult.setResult(processCore.getUser());
        return processResult;
    }

    public ProcessResult<AuthUser> deleteUser(String pCorpId) {
        DeleteUserCore processCore = new DeleteUserCore(pCorpId);

        return processCore.execute();
    }

    public ProcessResult<String> updateUserAuthorizationToken(String pCorpId) {
        UpdateUserAuthorizationTokenCore processCore = new UpdateUserAuthorizationTokenCore(pCorpId);

        return processCore.execute();
    }

    public ProcessResult<AuthUser> removeUserAuthorizationToken(String pCorpId) {
        RemoveUserAuthorizationTokenCore processCore = new RemoveUserAuthorizationTokenCore(pCorpId);

        return processCore.execute();
    }

    public ProcessResult<AuthUser> updateUserLastRemoteCallTimestamp(String pCorpId) {
        UpdateUserLastRemoteCallTimestampCore processCore = new UpdateUserLastRemoteCallTimestampCore(pCorpId);

        return processCore.execute();
    }

    public ProcessResult<UserSetting> updateUserSetting(String pCorpId, String pKey, String pValue) {
        UpdateUserSettingCore processCore = new UpdateUserSettingCore(pCorpId, pKey, pValue);

        return processCore.execute();
    }

    public ProcessResult<String> resetSettingsToDefault(String pCorpId) {
        ResetUserSettingsToDefaultCore processCore = new ResetUserSettingsToDefaultCore(pCorpId);

        return processCore.execute();
    }

    public ProcessResult updateUserLockoutValues(AuthUser pAuthUser, boolean pLoginValid) {
        UpdateUserLockoutValuesCore processCore = new UpdateUserLockoutValuesCore(pAuthUser, pLoginValid);

        return processCore.execute();
    }

    public ProcessResult<String> unlockUser(AuthUser pAuthUser) {
        UnlockUserCore processCore = new UnlockUserCore(pAuthUser);

        return processCore.execute();
    }
}