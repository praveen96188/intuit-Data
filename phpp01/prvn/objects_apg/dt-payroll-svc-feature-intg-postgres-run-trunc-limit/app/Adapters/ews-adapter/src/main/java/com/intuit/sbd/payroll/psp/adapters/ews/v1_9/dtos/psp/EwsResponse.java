package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

import javax.xml.bind.annotation.*;
import java.util.Calendar;

/**
    @author Jeff Jones
 */


@XmlSeeAlso({
    EwsCreateAccountResponse.class,
    EwsQueryAccountResponse.class,
    EwsAddServiceResponse.class,
    EwsResetPinResponse.class,
    EwsBasePinResponse.class,
    EwsBankResponse.class,
    EwsUpdateAccountResponse.class,
    EwsQueryOfferResponse.class,
    EwsQuerySubscriptionsResponse.class,
    EwsValidateSubscriptionResponse.class,
    EwsEinServiceEligibilityResponse.class,
    EwsMigrateAccountResponse.class,
    EwsMigrateEntitlementResponse.class
})

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "dateTimeStamp",
        "ewsResponseStatus"
})
public class EwsResponse implements Cloneable {

    @XmlElement(name = "DateTimeStamp", required = false)
    protected Calendar dateTimeStamp;

    @XmlElement(name = "ResponseStatus", required = false)
    protected EwsResponseStatus ewsResponseStatus;

    public EwsResponse() {
        this.dateTimeStamp = CalendarUtils.convertToCalendar(PSPDate.getPSPTime());
        this.ewsResponseStatus = new EwsResponseStatus();
    }

    public EwsResponse clone() throws CloneNotSupportedException {
        EwsResponse clone = (EwsResponse) super.clone();

        if (ewsResponseStatus != null) {
            clone.setEwsResponseStatus(ewsResponseStatus.clone());
        }

        return clone;
    }

    public Calendar getDateTimeStamp() {
        return dateTimeStamp;
    }

    public EwsResponseStatus getEwsResponseStatus() {
        return ewsResponseStatus;
    }

    public void setEwsResponseStatus(EwsResponseStatus ewsResponseStatus) {
        this.ewsResponseStatus = ewsResponseStatus;
    }

    public void validate() throws Exception {
    }    

}
