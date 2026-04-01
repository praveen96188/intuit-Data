package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.AuthUser;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * User: jwarmelink
 * Date: Jun 20, 2008
 * Time: 4:20:27 PM
 */
public class UpdateUserLastRemoteCallTimestampCore extends Process implements IProcess {

    private AuthUser mUser;
    private String mCorpId;

    public UpdateUserLastRemoteCallTimestampCore(String pCorpId) {
        this.mCorpId = pCorpId;
        this.mUser = null;
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
        if (mUser.getLastRemoteCallTimestamp() == null) {
            SpcfCalendar spcfTimestamp = SpcfCalendar.createInstance(PSPDate.getPSPTime().getTimeInMilliseconds());

            mUser.setLastRemoteCallTimestamp(spcfTimestamp);
            Application.save(mUser);
        } else {
            //don't update if less than 15 seconds ago according to database (multiple threads will execute this simultaneously)
            SpcfCalendar timestamp = SpcfCalendar.createInstance(PSPDate.getPSPTime().getTimeInMilliseconds());
            SpcfCalendar lastTimestampToUpdate =timestamp.copy();
            lastTimestampToUpdate.addSeconds(-15);

            Application.executeHQLUpdate("update com.intuit.sbd.payroll.psp.domain.AuthUser user " +
                                                 "set user.LastRemoteCallTimestamp = :timestamp " +
                                                 "where user = :user and user.LastRemoteCallTimestamp < :lastTimestampToUpdate"
                    , new String[] {"user", "timestamp", "lastTimestampToUpdate"}
                    , new Object[] {mUser, timestamp, lastTimestampToUpdate});


        }

        return new ProcessResult();
    }
}