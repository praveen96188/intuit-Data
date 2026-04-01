package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: jwarmelink
 * Date: Jun 20, 2008
 * Time: 4:20:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateUserAuthorizationTokenCore extends Process implements IProcess {

    private AuthUser mUser;
    private String mCorpId;
    private String mNewAuthorizationToken;

    public UpdateUserAuthorizationTokenCore(String pCorpId) {
        this.mCorpId = pCorpId;
        this.mUser = null;
        this.mNewAuthorizationToken = null;
    }

    public AuthUser getUser() {
        return mUser;
    }

    public String getNewAuthorizationToken() {
        return mNewAuthorizationToken;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate Corp Id
        if (mCorpId == null) {
            validationResult.getMessages().CorpIdNotSpecified(EntityName.AuthUser, null);
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Validate User Exists
        mUser = AuthUser.findUser(mCorpId);

        if (mUser == null) {
            validationResult.getMessages().UserDoesNotExist(EntityName.AuthUser, mCorpId);
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult<String> processResult = new ProcessResult<String>();

        UUID authTokenUUID = UUID.randomUUID();
        String authToken = authTokenUUID.toString();

        mUser.setAuthorizationToken(authToken);

        //Save the user
        mUser = Application.save(mUser);

        processResult.setResult(authToken);

        return processResult;
    }
}