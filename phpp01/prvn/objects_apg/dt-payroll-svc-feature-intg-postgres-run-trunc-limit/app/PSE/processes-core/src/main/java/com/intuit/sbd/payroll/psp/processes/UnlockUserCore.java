package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

/**
 * User: Greg Patterson
 * Date: 6/27/13
 * Time: 8:47 AM
 */
public class UnlockUserCore extends Process implements IProcess {

    private AuthUser mUser;

    public UnlockUserCore(AuthUser pUser) {
        this.mUser = pUser;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        //Validate AuthUser
        if (mUser == null) {
            validationResult.getMessages().UserDoesNotExist(EntityName.AuthUser, null);
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult pr = new ProcessResult();

        mUser.setAccountLockedUntil(null);
        mUser.setNumberOfFailedLoginAttempts(0);
        Application.save(mUser);

        return pr;
    }
}