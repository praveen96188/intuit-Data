package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Collection;
import java.util.Date;

/**
 * User: cyoder
 * Date: Aug 15, 2008
 * Time: 8:20:36 PM
 */
public class SAPCompanyServiceStatusHistoryItem {
    private Date changeDate;
    private String changedBy;
    private String serviceCd;

    private String oldServiceStatus;
    private String newServiceStatus;

    private Collection<String> newSubStatuses;
    private Collection<String> oldSubStatuses;

    public Date getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public String getServiceCd() {
        return serviceCd;
    }

    public void setServiceCd(String pServiceCd) {
        serviceCd = pServiceCd;
    }

    public String getOldServiceStatus() {
        return oldServiceStatus;
    }

    public void setOldServiceStatus(String oldServiceStatus) {
        this.oldServiceStatus = oldServiceStatus;
    }

    public String getNewServiceStatus() {
        return newServiceStatus;
    }

    public void setNewServiceStatus(String newServiceStatus) {
        this.newServiceStatus = newServiceStatus;
    }

    public Collection<String> getNewSubStatuses() {
        return newSubStatuses;
    }

    public void setNewSubStatuses(Collection<String> newSubStatuses) {
        this.newSubStatuses = newSubStatuses;
    }

    public Collection<String> getOldSubStatuses() {
        return oldSubStatuses;
    }

    public void setOldSubStatuses(Collection<String> oldSubStatuses) {
        this.oldSubStatuses = oldSubStatuses;
    }
}
