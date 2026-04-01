package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsBankVerificationStatus;

import javax.xml.bind.annotation.*;
import java.util.Calendar;

/**
 * @author Jeff Jones
 */

@XmlSeeAlso({
    EwsBankAccountResponse.class
})

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "verificationStatus",
        "retries",
        "lastRetryDateTime"
})
public class EwsBaseBankAccountResponse implements Cloneable {

    @XmlElement(name = "VerificationStatus", required = false)
    protected EwsBankVerificationStatus verificationStatus;

    @XmlElement(name = "Retries", required = false)
    protected boolean retries;

    @XmlElement(name = "LastRetryDateTime", required = false)
    protected Calendar lastRetryDateTime;

    public EwsBaseBankAccountResponse clone() throws CloneNotSupportedException {
        return (EwsBaseBankAccountResponse) super.clone();
    }

    public EwsBankVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(EwsBankVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public boolean isRetries() {
        return retries;
    }

    public void setRetries(boolean retries) {
        this.retries = retries;
    }

    public Calendar getLastRetryDateTime() {
        return lastRetryDateTime;
    }

    public void setLastRetryDateTime(Calendar lastRetryDateTime) {
        this.lastRetryDateTime = lastRetryDateTime;
    }

    public void validate() throws Exception {
    }

}
