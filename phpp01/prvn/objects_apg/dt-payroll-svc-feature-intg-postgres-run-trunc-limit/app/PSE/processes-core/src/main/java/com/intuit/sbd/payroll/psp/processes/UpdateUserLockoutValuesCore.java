package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * User: Greg Patterson
 * Date: 6/27/13
 * Time: 8:47 AM
 */
public class UpdateUserLockoutValuesCore extends Process implements IProcess {

    private AuthUser mUser;
    private boolean mLoginValid;

    public UpdateUserLockoutValuesCore(AuthUser pUser, boolean pLoginValid) {
        this.mUser = pUser;
        this.mLoginValid = pLoginValid;
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

        if (mLoginValid) {
            processValidLogin();
        } else {
            processInvalidLogin();
        }

        Application.save(mUser);

        return pr;
    }

    private void processValidLogin() {
        // always reset NOFLA
        mUser.setNumberOfFailedLoginAttempts(0);

        // clear lock value if the time has already passed
        if (mUser.getAccountLockedUntil() != null &&
                mUser.getAccountLockedUntil().before(PSPDate.getPSPTime())) {

            mUser.setAccountLockedUntil(null);
        }
    }

    private void processInvalidLogin() {

        // always increment NOFLA
        mUser.setNumberOfFailedLoginAttempts(mUser.getNumberOfFailedLoginAttempts() + 1);

        // set/update lockout time if NOFLA is above the threshold
        if (mUser.getNumberOfFailedLoginAttempts() >
                SystemParameter.findIntValue(SystemParameter.Code.MAX_NUMBER_OF_FAILED_LOGIN_ATTEMPTS)) {

            SpcfCalendar lockedUntil = PSPDate.getPSPTime();
            lockedUntil.addMinutes(SystemParameter.findIntValue(SystemParameter.Code.LOCK_ACCOUNT_DURATION));
            mUser.setAccountLockedUntil(lockedUntil);
        }
    }
}