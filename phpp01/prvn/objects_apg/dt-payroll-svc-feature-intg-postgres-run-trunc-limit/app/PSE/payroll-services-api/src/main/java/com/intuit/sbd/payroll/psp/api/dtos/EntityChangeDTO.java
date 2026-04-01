package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * User: rnorian
 * Date: Feb 7, 2011
 * Time: 2:07:08 PM
 */
public class EntityChangeDTO {
    private String userId;
    private String oldEIN;
    private String newEIN;
    private DateDTO effectiveDate;
    private boolean isSuccessor=false;
    private boolean isError = false;
    private boolean hasNewDataFile = false;

    public EntityChangeDTO() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String pUserId) {
        userId = pUserId;
    }

    public String getOldEIN() {
        return oldEIN;
    }

    public void setOldEIN(String pOldEIN) {
        oldEIN = pOldEIN;
    }

    public String getNewEIN() {
        return newEIN;
    }

    public void setNewEIN(String pNewEIN) {
        newEIN = pNewEIN;
    }

    public DateDTO getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(DateDTO pEffectiveDate) {
        effectiveDate = pEffectiveDate;
    }

    public boolean getIsSuccessor() {
        return isSuccessor;
    }

    public void setIsSuccessor(boolean pSuccessor) {
        isSuccessor = pSuccessor;
    }

    public boolean getHasNewDataFile() {
        return hasNewDataFile;
    }

    public void setHasNewDataFile(boolean pHasNewDataFile) {
        hasNewDataFile = pHasNewDataFile;
    }

    public boolean getIsError() {
        return isError;
    }

    public void setIsError(boolean pError) {
        isError = pError;
    }
}
