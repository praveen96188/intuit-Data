package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;

import javax.xml.bind.annotation.*;
import java.util.Calendar;

/**
    @author Jeff Jones
 */
@XmlSeeAlso({
    EwsCreateAccount.class,
    EwsQueryAccount.class,
    EwsAddService.class,
    EwsResetPin.class,
    EwsBasePin.class,
    EwsUpdatePin.class,
    EwsValidateBank.class,
    EwsUpdateBank.class,
    EwsUpdateAccount.class,
    EwsQueryOffer.class,
    EwsQuerySubscriptions.class,
    EwsValidateSubscription.class,
    EwsUpdateBillingDetails.class,
    EwsEinServiceEligibility.class,
    EwsMigrateAccount.class,
    EwsMigrateEntitlement.class,
    EwsDeactivateService.class
})

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "ipAddress",
        "dateTimeStamp"
})
public class EwsRequest implements Cloneable {

    @XmlElement(name = "IpAddress", required = true)
    protected String ipAddress;

    @XmlElement(name = "DateTimeStamp", required = true)
    protected Calendar dateTimeStamp;

    public EwsRequest clone() throws CloneNotSupportedException {
        return (EwsRequest) super.clone();
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setDateTimeStamp(Calendar dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    public Calendar getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void validate() throws Exception {
        PspPrincipal principal = Application.getCurrentPrincipal();
        if (!principal.isAgent()) {
            if (!Validation.validateValue(this.getIpAddress(), false, "\\b\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b")) {
                throw new EwsException(EwsMessages.fieldDataNotValid("IPAddress", "Request"));
            }
        }
    }
}
