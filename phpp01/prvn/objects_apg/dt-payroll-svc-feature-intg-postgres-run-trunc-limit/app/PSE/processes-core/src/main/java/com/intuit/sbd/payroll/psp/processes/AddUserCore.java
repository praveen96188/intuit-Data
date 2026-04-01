package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.AuthDomain;
import com.intuit.sbd.payroll.psp.domain.AuthRole;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 20, 2008
 * Time: 4:20:27 PM
 */
public class AddUserCore extends Process implements IProcess {

    private List<String> mRoleIds;
    private String mCorpId;
    private String mFirstName;
    private String mLastName;
    private AuthUser mUser;
    private List<AuthRole> mRoles;

    public AddUserCore(String pCorpId, List<String> pRoleIds, String pFirstName, String pLastName) {
        this.mLastName = pLastName;
        this.mRoleIds = pRoleIds;
        this.mCorpId = pCorpId;
        this.mFirstName = pFirstName;
    }

    public AuthUser getUser() {
        return mUser;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate Corp Id
        if (mCorpId == null) {
            validationResult.getMessages().CorpIdNotSpecified(EntityName.AuthUser, null);
        }

        //Validate Role Id
        if (mRoleIds == null || mRoleIds.size() == 0) {
            validationResult.getMessages().RoleIdNotSpecified(EntityName.AuthUser, null);
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }        

        //Validate User Exists
        mUser = AuthUser.findUser(mCorpId);

        if (mUser != null) {
            validationResult.getMessages().UserAlreadyExists(EntityName.AuthUser, mCorpId);
        }

        AuthDomain domain =null;
        mRoles = new ArrayList<AuthRole>();
        for (String roleId : mRoleIds) {
            AuthRole role = AuthRole.findRole(roleId);
            if (role == null) {
                validationResult.getMessages().RoleDoesNotExist(EntityName.AuthUser, roleId);
                return validationResult;
            }
            if (domain == null) {
                domain = role.getAuthDomain();
            } else {
                if (!domain.equals(role.getAuthDomain())) {
                    validationResult.getMessages().GenericError(EntityName.AuthUser, roleId, "Domains do not match");
                }
            }
            mRoles.add(role);
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult<AuthUser> processResult = new ProcessResult<AuthUser>();

        mUser = new AuthUser();

        mUser.setCorpId(mCorpId);
        mUser.setFirstName(mFirstName);
        mUser.setLastName(mLastName);
        mUser.getAuthRoleCollection().addAll(mRoles);

        //Save the user
        mUser = Application.save(mUser);

        processResult.setResult(mUser);

        return processResult;
    }
}
