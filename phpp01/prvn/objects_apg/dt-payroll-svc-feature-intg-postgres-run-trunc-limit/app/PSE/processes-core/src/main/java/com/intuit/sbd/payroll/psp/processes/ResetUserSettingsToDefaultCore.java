package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.UserSetting;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.Application;

/**
 * User: dweinberg
 * Date: Dec 1, 2009
 * Time: 4:13:08 PM
 */
public class ResetUserSettingsToDefaultCore extends Process implements IProcess {

    private AuthUser user;

    private String corpId;

    public ResetUserSettingsToDefaultCore(String corpId) {
        this.corpId = corpId;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate Corp Id
        if (corpId == null) {
            validationResult.getMessages().CorpIdNotSpecified(EntityName.AuthUser, null);
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate User Exists
        user = AuthUser.findUser(corpId);

        if (user == null) {
            validationResult.getMessages().UserDoesNotExist(EntityName.AuthUser, corpId);
        }        

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult<String> pr = new ProcessResult<String>();
        for (UserSetting userSetting : user.getUserSettingCollection()) {
            Application.delete(userSetting);
        }
        
        return pr;
    }
}
