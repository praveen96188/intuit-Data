package com.intuit.sbd.payroll.psp.api.dtos;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: dhaddan
 * Date: May 12, 2010
 * Time: 2:30:12 PM
 */
public class AS400CompanyDTO {
    private CompanyDTO companyDTO;
    private Collection<String> pins = new ArrayList<String>();
    private String status;
    private String subStatus;
    private String bankVerificationStatus;
    private boolean isOnHold;
    private long lastTaxQuarter;

    public Collection<String> getPins() {
        return pins;
    }

    public void setPins(Collection<String> pins) {
        this.pins = pins;
    }

    public CompanyDTO getCompanyDTO() {
        return companyDTO;
    }

    public void setCompanyDTO(CompanyDTO companyDTO) {
        this.companyDTO = companyDTO;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status != null)
            status = status.trim();
        this.status = status;
    }

    public String getSubStatus() {
        return subStatus;
    }

    public void setSubStatus(String subStatus) {
        if (subStatus != null)
            subStatus = subStatus.trim();
        this.subStatus = subStatus;
    }

    public String getBankVerificationStatus() {
        return bankVerificationStatus;
    }

    public void setBankVerificationStatus(String bankVerificationStatus) {
        if (bankVerificationStatus != null)
            bankVerificationStatus = bankVerificationStatus.trim();
        this.bankVerificationStatus = bankVerificationStatus;
    }

    public boolean isOnHold() {
        return isOnHold;
    }

    public void setOnHold(boolean onHold) {
        isOnHold = onHold;
    }

    public long getLastTaxQuarter() {
        return lastTaxQuarter;
    }

    public void setLastTaxQuarter(long lastTaxQuarter) {
        this.lastTaxQuarter = lastTaxQuarter;
    }
}
