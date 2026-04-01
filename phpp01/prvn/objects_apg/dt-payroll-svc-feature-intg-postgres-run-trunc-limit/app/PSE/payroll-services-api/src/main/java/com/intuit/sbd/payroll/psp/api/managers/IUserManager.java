package com.intuit.sbd.payroll.psp.api.managers;

import com.intuit.sbd.payroll.psp.api.dtos.UserRoleDTO;
import com.intuit.sbd.payroll.psp.domain.AuthRole;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.UserSetting;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 24, 2008
 * Time: 12:13:25 PM
 */
public interface IUserManager {
    ProcessResult<AuthRole> addRole(UserRoleDTO pRoleDto);
    ProcessResult<AuthRole> updateRole(UserRoleDTO pRoleDto);
    ProcessResult<AuthUser> addUser(String pCorpId, List<String> pRoleIds, String pFirstName,String pLastName);
    ProcessResult<AuthUser> updateUser(String pUserSeqId, String pCorpId, List<String> pRoleIds, String pFirstName,String pLastName);
    ProcessResult<AuthUser> deleteUser(String pCorpId);
    ProcessResult<String> updateUserAuthorizationToken(String pCorpId);
    ProcessResult<AuthUser> removeUserAuthorizationToken(String pCorpId);
    ProcessResult<AuthUser> updateUserLastRemoteCallTimestamp(String pCorpId);
    ProcessResult<UserSetting> updateUserSetting(String pCorpId, String pKey, String pValue);
    ProcessResult<String> resetSettingsToDefault(String pCorpId);
    ProcessResult updateUserLockoutValues(AuthUser pAuthUser, boolean pLoginValid);
    ProcessResult unlockUser(AuthUser pAuthUser);
}
