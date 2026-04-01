package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jun 23, 2008
 * Time: 4:21:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class DeleteUserCore extends Process implements IProcess {
    private AuthUser mUser;
    private String mCorpId;

    private ResetUserSettingsToDefaultCore resetPreferencesProcess;

    public DeleteUserCore(String pCorpId) {
        this.mCorpId = pCorpId;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate Corp Id
        if (mCorpId == null) {
            validationResult.getMessages().CorpIdNotSpecified(EntityName.AuthUser, null);
            return validationResult;
        }

        //Validate User Exists
        mUser = AuthUser.findUser(mCorpId);

        if (mUser == null) {
            validationResult.getMessages().UserDoesNotExist(EntityName.AuthUser, mCorpId);
            return validationResult;
        }

        resetPreferencesProcess = new ResetUserSettingsToDefaultCore(mCorpId);
        validationResult.merge(resetPreferencesProcess.validate());


        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult<AuthUser> processResult = new ProcessResult<AuthUser>();

        //need to remove preferences since they have a FK
        processResult.merge(resetPreferencesProcess.process());

        Application.delete(mUser);

        return processResult;
    }
}
