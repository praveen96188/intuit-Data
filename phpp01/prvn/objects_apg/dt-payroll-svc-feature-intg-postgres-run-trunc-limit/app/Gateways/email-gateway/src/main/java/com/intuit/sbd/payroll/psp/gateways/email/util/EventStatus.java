package com.intuit.sbd.payroll.psp.gateways.email.util;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventRepository;

import java.util.List;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 26, 2008
 * Time: 5:17:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventStatus {
    private enum Status {
        unassigned,     // newly created event (as yet unassigned to an email)
        assigned,       // the event nas been assigned to an email and is pending transmission
        deferred,       // defer for later processing (email group is incomplete (i.e. ReversalOK/ReversalReturn))
        sent,           // the event was successfully sent via email
        error,          // the event failed validation or failed to be sent (details in error list)
        skipped,        // the event skipped to send (Recipient Email address is invalid)
        listDetectiveError    // the event failed Error Code "SEND-NTS-31010", reason is "(Error Code: 24 - Subscriber was excluded by List Detective.)"
    }

    private IEventRepository mRepository;
    private CompanyEventEmail mEvent;
    private Status mEventStatus;
    private EventStatus mParent = null;
    private boolean mAllowRetry = true;
    private boolean mStatusChanged = false;
    private boolean mValidated = false; // flags if event passed validation and was assigned to an email template
    private List<EventStatus> mLinkedEvents = new Vector<EventStatus>();
    private List<EventStatusError> mErrorList = new Vector<EventStatusError>();

    public EventStatus(CompanyEventEmail pEvent, IEventRepository pRepository) {
        mRepository = pRepository;
        mEvent = pEvent;
        setStatus(Status.unassigned); // set default status (want mStatusChanged true to start)
    }

    public CompanyEventEmail getEvent() {
        return mEvent;
    }

    public EventStatus spawnChild(CompanyEventEmail pEvent) {
        EventStatus child = mRepository.addEvent(pEvent);
        linkStatusEvent(child);
        return child;
    }

    public void setTemplateId(EventEmailTemplateTypeCode pTemplateId) {
        if (mEvent.getEmailTemplateTypeCd() != pTemplateId) {
            mEvent.setEmailTemplateTypeCd(pTemplateId);
            mEvent = Application.save(mEvent);
        }

        for (EventStatus status : mLinkedEvents) {
            status.setTemplateId(pTemplateId);
        }
    }

    public EventEmailTemplateTypeCode getTemplateId() {
        return mEvent.getEmailTemplateTypeCd();
    }

    public void linkStatusEvent(EventStatus pStatusEvent) {
        if (pStatusEvent == this) return;
        mLinkedEvents.add(pStatusEvent);
        pStatusEvent.mParent = this;
        pStatusEvent.setStatus(mEventStatus);
    }

    public boolean isLinkedEvent() {
        return mParent != null;
    }

    private void setStatus(Status pEventStatus) {
        if (mEventStatus != pEventStatus) {
            mEventStatus = pEventStatus;
            mStatusChanged = true;
        }

        for (EventStatus status : mLinkedEvents) {
            status.setStatus(mEventStatus);
        }
    }

    public boolean isRetry() {
        return (mEvent.getStatusCd() == EventEmailStatus.Resend);
    }

    public boolean isUnassigned() {
        return (mEventStatus == Status.unassigned);
    }

    public boolean isValidated() {
        return mValidated;
    }

    public void setAssigned() {
        setStatus(Status.assigned);
        mValidated = true;
    }

    public void setSkipped() {
        setStatus(Status.skipped);
        mValidated = true;
    }

    public boolean isAssigned() {
        return (mEventStatus == Status.assigned);
    }

    public void setDeferred() {
        setStatus(Status.deferred);
    }

    public boolean isDeferred() {
        return (mEventStatus == Status.deferred);
    }

    public void setSent() {
        setStatus(Status.sent);
    }

    public boolean isSent() {
        return (mEventStatus == Status.sent);
    }

    public boolean isError() {
        return (mEventStatus == Status.error);
    }

    private void addStatusError(EventStatusError pStatusError) {
        mErrorList.add(pStatusError);

        for (EventStatus status : mLinkedEvents) {
            status.addStatusError(pStatusError);
        }
    }

    public void addError(EventStatusError pStatusError) {
        addStatusError(pStatusError);

        setStatus(Status.error);

        // once false, should stay false
        setAllowRetry(mAllowRetry && (pStatusError.getErrorType() != EventStatusErrorType.FailedValidation));
    }

    public void setListDetectiveError() {
        setStatus(Status.listDetectiveError);
        //Do not retry for list detective error
        setAllowRetry(false);
    }

    private void setAllowRetry(boolean pAllowRetry) {
        mAllowRetry = pAllowRetry;

        for (EventStatus status : mLinkedEvents) {
            status.setAllowRetry(mAllowRetry);
        }
    }

    public void persistLinkedEvents() {
        for (EventStatus status : mLinkedEvents) {
            status.persistStatus();
        }
    }

    public void persistStatus() {
        // For safety, we only want to persist if mStatusChanged is true so we don't unduely increment the retry count
        // in the event this method is called multiple times with an mEventStatus of 'error' (we could still force
        // this condition if we set mEventStatus to 'error', call persistStatus(), change to another status, call
        // persistStatus() again, then change back to 'error' and call persistStatus() a final time.)

        if (mStatusChanged) {
            switch (mEventStatus) {
                // for errors, check to see if we fail or retry
                case error:
                    // PSRV003826 - Adding max retries as System Parameter (set to 0 to disable retries)
                    int maxRetry = SystemParameter.findIntValue(SystemParameter.Code.EMAIL_GATEWAY_SEND_MAX_RETRY, 5);
                    int retryCount = mEvent.getRetryCount();
                    boolean doRetry = (mAllowRetry && (++retryCount <= maxRetry));

                    // if we've already retried five times, then fail
                    if (doRetry) {
                        for (EventStatusError error : mErrorList) {
                            doRetry = doRetry &&
                                      (error.getErrorType() != EventStatusErrorType.FailedValidation) &&
                                      (error.getErrorType() != EventStatusErrorType.Unknown);
                        }
                    }

                    if (doRetry) {
                        mEvent.setStatusCd(EventEmailStatus.Resend);
                        mEvent.setRetryCount(retryCount);
                    } else {
                        mEvent.setStatusCd(EventEmailStatus.SendFailed);
                    }

                    mEvent.setStatusEffectiveDate(PSPDate.getPSPTime());
                    break;

                case assigned:
                    // PSRV003705 - Multiple emails (stop marking events left in an 'assigned' state as SendFailed)
                    // Do nothing (leave event in original entry state to allow it to be processed on next run)
                    break;

                // if we're in unassigned state when persisting, it's an error
                // (unassigned might occur if the event type is not supported by email)
                case unassigned:
                    // force into an error state for reporting
                    addError(new EventStatusError("Event left in unassigned state.", EventStatusErrorType.Unknown));
                    mEvent.setStatusCd(EventEmailStatus.SendFailed);
                    mEvent.setStatusEffectiveDate(PSPDate.getPSPTime());
                    break;

                // if we're deferred, then the email group is incomplete
                case deferred:
                    mEvent.setStatusCd(EventEmailStatus.GroupIncomplete);
                    mEvent.setStatusEffectiveDate(PSPDate.getPSPTime());
                    break;

                case sent:
                    mEvent.setStatusCd(EventEmailStatus.Sent);
                    mEvent.setStatusEffectiveDate(PSPDate.getPSPTime());
                    break;

                case skipped:
                    mEvent.setStatusCd(EventEmailStatus.SendSkippedInvalidEmailId);
                    mEvent.setStatusEffectiveDate(PSPDate.getPSPTime());
                    break;

                case listDetectiveError:
                    mEvent.setStatusCd(EventEmailStatus.SendFailedInvalidEmailId);
                    mEvent.setStatusEffectiveDate(PSPDate.getPSPTime());
                    break;
            }

            mEvent = Application.save(mEvent);

            mStatusChanged = false;
        }

        persistLinkedEvents();
    }

    public StringBuffer reportErrors() {
        StringBuffer err = new StringBuffer();

        if (mEvent.getStatusCd().equals(EventEmailStatus.SendFailed)) {
            CompanyEvent companyEvent = mEvent.getCompanyEvent();

            err.append(EmailUtils.sfNewLine);

            err.append("> Event status errors for event: ");
            err.append(companyEvent.getEventTypeCd().toString());

            err.append(EmailUtils.sfNewLine);

            for (EventStatusError error : mErrorList) {
                err.append("  [ ");
                err.append(error.getErrorType().toString());
                err.append(" ] ");
                err.append(error.getErrorDetail());
                err.append(EmailUtils.sfNewLine);
            }

            // keep the cache clear of noise
            Application.evict(companyEvent);
        }

        return err;
    }
}
