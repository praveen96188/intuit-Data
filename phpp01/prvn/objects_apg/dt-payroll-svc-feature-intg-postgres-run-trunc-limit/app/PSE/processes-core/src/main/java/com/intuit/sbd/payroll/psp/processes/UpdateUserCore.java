package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.AuthDomain;
import com.intuit.sbd.payroll.psp.domain.AuthRole;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 23, 2008
 * Time: 12:23:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateUserCore extends Process implements IProcess {

    private List<String> mRoleIds;
    private String mCorpId;
    private String mFirstName;
    private String mLastName;
    private AuthUser mUser;
    private List<AuthRole> mRoles;
    private String mUserSeqId;

    public UpdateUserCore(String pUserSeqId, String mCorpId, List<String> mRoleIds, String mFirstName, String mLastName) {
        this.mRoleIds = mRoleIds;
        this.mCorpId = mCorpId;
        this.mFirstName = mFirstName;
        this.mLastName = mLastName;
        this.mUserSeqId = pUserSeqId;
    }

    public AuthUser getUser() {
        return mUser;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        //Validate Corp Id
        if (mUserSeqId == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.AuthUser, null, "User Sequence Id");
        }

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

        mUser = Application.findById(AuthUser.class, SpcfUniqueId.createInstance(mUserSeqId));

        if (mUser == null) {
            validationResult.getMessages().UserDoesNotExist(EntityName.AuthUser, mUserSeqId);
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

        mUser.setCorpId(mCorpId);
        mUser.setFirstName(mFirstName);
        mUser.setLastName(mLastName);

        if(mUser.getAuthRoleCollection().size()!=mRoles.size() || !mUser.getAuthRoleCollection().containsAll(mRoles)){
            mUser.setModifiedDate(
                    SpcfCalendar.createInstance(PSPDate.getPSPTime().getTimeInMilliseconds())
            );
        }
        mUser.getAuthRoleCollection().clear();
        mUser.getAuthRoleCollection().addAll(mRoles);


        //Save the user
        mUser = Application.save(mUser);

        processResult.setResult(mUser);

        return processResult;
    }
}
