package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.EntitlementMessageStatusCode;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 8, 2010
 * Time: 8:00:14 AM
 */
public class EntitlementMessageDTO {
    private String mOrderNumber;
    private String mLicenseNumber;
    private String mEntitlementOfferingCode;
    private EntitlementMessageStatusCode mEntitlementMessageStatusCode;
    private long mToken;
    private String mMessage;
    private String mEventReason;
    private SpcfCalendar mMessageTimestamp;

    public String getOrderNumber() {
        return mOrderNumber;
    }

    public void setOrderNumber(String pOrderNumber) {
        mOrderNumber = pOrderNumber;
    }

    public String getLicenseNumber() {
        return mLicenseNumber;
    }

    public void setLicenseNumber(String pLicenseNumber) {
        mLicenseNumber = pLicenseNumber;
    }

    public String getEntitlementOfferingCode() {
        return mEntitlementOfferingCode;
    }

    public void setEntitlementOfferingCode(String pEntitlementOfferingCode) {
        mEntitlementOfferingCode = pEntitlementOfferingCode;
    }

    public EntitlementMessageStatusCode getEntitlementMessageStatusCode() {
        return mEntitlementMessageStatusCode;
    }

    public void setEntitlementMessageStatusCode(EntitlementMessageStatusCode pEntitlementMessageStatusCode) {
        mEntitlementMessageStatusCode = pEntitlementMessageStatusCode;
    }

    public long getToken() {
        return mToken;
    }

    public void setToken(long pToken) {
        mToken = pToken;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String pMessage) {
        mMessage = pMessage;
    }

    public String getEventReason() {
        return mEventReason;
    }

    public void setEventReason(String pEventReason) {
        mEventReason = pEventReason;
    }

    public SpcfCalendar getMessageTimestamp() {
        return mMessageTimestamp;
    }

    public void setMessageTimestamp(SpcfCalendar pMessageTimestamp) {
        mMessageTimestamp = pMessageTimestamp;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (getLicenseNumber() == null || !Validator.isValidLength(getLicenseNumber(), 1, 20)) {
            validationResult.getMessages().InvalidValue(EntityName.EntitlementMessage, getLicenseNumber() == null ? "null" : getLicenseNumber(), "LicenseNumber");
        }

        if (getEntitlementOfferingCode() != null && !Validator.isValidLength(getEntitlementOfferingCode(), 1, 20)) {
            validationResult.getMessages().InvalidValue(EntityName.EntitlementMessage, getEntitlementOfferingCode() == null ? "null" : getEntitlementOfferingCode(), "EntitlementOfferingCode");
        }

        if(getOrderNumber() != null && !Validator.isValidLength(getOrderNumber(), 1, 20)){
            validationResult.getMessages().InvalidValue(EntityName.EntitlementMessage, getOrderNumber(), "OrderNumber");
        }

        if(getMessage() == null){
            validationResult.getMessages().InvalidValue(EntityName.EntitlementMessage, "null", "Message");
        }

        return validationResult;
    }
}
