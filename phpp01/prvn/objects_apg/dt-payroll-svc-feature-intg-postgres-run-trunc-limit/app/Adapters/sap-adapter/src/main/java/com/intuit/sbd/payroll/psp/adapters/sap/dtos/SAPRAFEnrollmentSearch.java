package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * User: dweinberg
 * Date: 1/11/13
 * Time: 9:46 AM
 */
public class SAPRAFEnrollmentSearch {

    private String status;
    private String PSID_or_EIN;
    private Date creationDateStart;
    private Date creationDateEnd;
    private Date lastUpdateDateStart;
    private Date lastUpdateDateEnd;

    public SAPRAFEnrollmentSearch() {
    }

    public SAPRAFEnrollmentSearch(String pStatus, String pPSID_or_EIN, Date pCreationDateStart, Date pCreationDateEnd, Date pLastUpdateDateStart, Date pLastUpdateDateEnd) {
        status = pStatus;
        PSID_or_EIN = pPSID_or_EIN;
        creationDateStart = pCreationDateStart;
        creationDateEnd = pCreationDateEnd;
        lastUpdateDateStart = pLastUpdateDateStart;
        lastUpdateDateEnd = pLastUpdateDateEnd;
    }

    public List<String> getPSIDEINList() {
        if (StringUtils.isEmpty(getPSID_or_EIN())) {
            return new ArrayList<String>();
        }
        return Arrays.asList(getPSID_or_EIN().split("\\s+"));
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String pStatus) {
        status = pStatus;
    }

    public String getPSID_or_EIN() {
        return PSID_or_EIN;
    }

    public void setPSID_or_EIN(String pPSID_or_EIN) {
        PSID_or_EIN = pPSID_or_EIN;
    }

    public Date getCreationDateStart() {
        return creationDateStart;
    }

    public void setCreationDateStart(Date pCreationDateStart) {
        creationDateStart = pCreationDateStart;
    }

    public Date getCreationDateEnd() {
        return creationDateEnd;
    }

    public void setCreationDateEnd(Date pCreationDateEnd) {
        creationDateEnd = pCreationDateEnd;
    }

    public Date getLastUpdateDateStart() {
        return lastUpdateDateStart;
    }

    public void setLastUpdateDateStart(Date pLastUpdateDateStart) {
        lastUpdateDateStart = pLastUpdateDateStart;
    }

    public Date getLastUpdateDateEnd() {
        return lastUpdateDateEnd;
    }

    public void setLastUpdateDateEnd(Date pLastUpdateDateEnd) {
        lastUpdateDateEnd = pLastUpdateDateEnd;
    }


}
