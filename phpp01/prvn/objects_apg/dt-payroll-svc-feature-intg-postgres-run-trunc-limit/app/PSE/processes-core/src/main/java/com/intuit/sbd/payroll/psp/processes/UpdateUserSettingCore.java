package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.UserSetting;
import com.intuit.sbd.payroll.psp.domain.UserPreference;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;

/**
 * User: dweinberg
 * Date: Nov 24, 2009
 * Time: 3:51:41 PM
 */
public class UpdateUserSettingCore extends Process implements IProcess {

    private AuthUser user;

    private String corpId;
    private String key;
    private String value;

    public UpdateUserSettingCore(String corpId, String key, String value) {
        this.corpId = corpId;
        this.key = key;
        this.value = value;
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

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate preference exists
        UserPreference pref = PayrollServices.entityFinder.findById(UserPreference.class, key);
        if (pref == null) {
            validationResult.getMessages().InvalidValue(EntityName.AuthUser, corpId, "key");
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult<UserSetting> processResult = new ProcessResult<UserSetting>();

        DomainEntitySet<UserSetting> settings = user.getUserSettingCollection();
        for (UserSetting setting : settings) {
            if (setting.getUserPreference().getKey().equals(key)) {
                if (setting.getUserPreference().getDefaultValue().equals(value)) {
                    //we're setting back to the default value which means we can just delete it
                    Application.delete(setting);
                } else {
                    setting.setValue(value);                                        
                }
                processResult.setResult(setting);
                return processResult;
            }
        }

        //new
        UserSetting setting = new UserSetting();

        UserPreference pref = PayrollServices.entityFinder.findById(UserPreference.class, key);
        setting.setUserPreference(pref);
        setting.setValue(value);
        user.addUserSetting(setting);

        setting.setAuthUser(user);
        
        Application.save(setting);

        processResult.setResult(setting);
        return processResult;

    }
}
