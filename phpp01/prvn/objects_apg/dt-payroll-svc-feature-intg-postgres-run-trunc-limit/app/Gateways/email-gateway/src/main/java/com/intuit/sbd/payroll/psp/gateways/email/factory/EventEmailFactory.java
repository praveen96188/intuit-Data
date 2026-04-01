package com.intuit.sbd.payroll.psp.gateways.email.factory;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmail;
import com.intuit.sbd.payroll.psp.gateways.email.util.EventStatus;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode;
import com.intuit.sbd.payroll.psp.domain.EventEmailParamTypeCode;
import com.intuit.sbd.payroll.psp.domain.CompanyEventEmail;
import com.intuit.sbd.payroll.psp.domain.CompanyEventEmailParam;
import com.intuit.sbd.payroll.psp.domain.EventTypeCode;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 26, 2008
 * Time: 3:02:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventEmailFactory {
    public static List<EventEmail> createEventEmailForEvents(List<EventStatus> pEvents) {
        List<EventEmail> emailList = new Vector<EventEmail>();
        Stack<EventStatus> eventStack = new Stack<EventStatus>();
        CompanyEvent companyEvent;
        EventStatus eventStatus;
        EventEmail email;

        // make a copy because we'll be dropping members from this list
        eventStack.addAll(pEvents);

        while (!eventStack.isEmpty()) {
            eventStatus = eventStack.pop();

            if (eventStatus.isUnassigned()) {
                companyEvent = eventStatus.getEvent().getCompanyEvent();

                switch (companyEvent.getEventTypeCd()) {
                    case FeeRebilled:
                        email = handleFeeRebilledEmail(eventStatus, eventStack);
                        break;

                    case FeeRefunded:
                        email = handleFeeRefundedEmail(eventStatus, eventStack);
                        break;

                    case NOC:
                        email = handleNOCEmail(eventStatus, eventStack);
                        break;

                    case EmployeeBankAccountChange:
                        email = handleEmployeeBankAccountChangeEmail(eventStatus, eventStack);
                        break;

                    case PayeeBankAccountChange:
                        email = handlePayeeBankAccountChangeEmail(eventStatus, eventStack);
                        break;

                    case ReversalRequested:
                        email = handleReversalRequestedEmail(eventStatus, eventStack);
                        break;

                    case ReversalOK:
                    case ReversalReturn:
                        email = handleReversalReturnOrReversalOKEmail(eventStatus, eventStack);
                        break;

                    case BillPaymentReceived:
                        email = handleBillPaymentReceived(eventStatus, eventStack);
                        break;
                    case PayrollReceived:
                        email = handlePayrollReceived(eventStatus, eventStack);
                        break;
                    case AssistedPayrollConfirmation:
                        email = handlePayrollReceived(eventStatus, eventStack);
                        break;
                    default: // no special handling required
                        email = new EventEmail(eventStatus);
                        break;
                }

                if ((email != null) && email.readyToSend()) {
                    emailList.add(email);
                }

                // keep the cache clear of noise (note: intentionally not using try/finally
                // since we want errors to retain their company event object in the cache)
                Application.evict(companyEvent);
            }
        }

        return emailList;
    }

    private static EventEmail handleFeeRebilledEmail(EventStatus pMasterEvent, List<EventStatus> pEventList) {
        EventEmail email = null;

        // find all the FeeRebilled events and link them to the master event
        List<EventStatus> eventList = EmailUtils.findAndLinkLikeEvents(pEventList, pMasterEvent);
        TreeSet<String> feeRebillList = new TreeSet<String>(); // sorted list of fee rebill params
        TreeSet<String> feeRefundList = new TreeSet<String>(); // sorted list of fee refund params

        // assemble the params for the email
        for (EventStatus event : eventList) {
            DomainEntitySet<CompanyEventEmailParam> params = event.getEvent().getEmailParamsForEmailEvent();

            for (CompanyEventEmailParam param : params) {
                switch (param.getParamTypeCd()) {
                    case BilledFeeList:
                        feeRebillList.add(param.getValue());
                        break;

                    case RefundedFeeList:
                        feeRefundList.add(param.getValue());
                        break;
                }

                // keep the cache clear of noise
                Application.evict(param);
            }
        }

        // if we have valid params, create the email
        if (!feeRebillList.isEmpty() && !feeRefundList.isEmpty()) {
            StringBuffer rebill = new StringBuffer();

            for (String str : feeRebillList) {
                rebill.append(str);
            }

            StringBuffer refund = new StringBuffer();

            for (String str : feeRefundList) {
                refund.append(str);
            }

            email = new EventEmail(pMasterEvent);
            email.addProperty(EventEmailParamTypeCode.BilledFeeList, rebill.toString());
            email.addProperty(EventEmailParamTypeCode.RefundedFeeList, refund.toString());
        }

        return email;
    }

    private static EventEmail handleFeeRefundedEmail(EventStatus pMasterEvent, List<EventStatus> pEventList) {
        EventEmail email = null;

        // find all the FeeRefunded events and link them to the master event
        List<EventStatus> eventList = EmailUtils.findAndLinkLikeEvents(pEventList, pMasterEvent);
        TreeSet<String> feeRefundList = new TreeSet<String>(); // sorted list of fee refund params

        // assemble the params for the email
        for (EventStatus event : eventList) {
            DomainEntitySet<CompanyEventEmailParam> params = event.getEvent().getEmailParamsForEmailEvent();

            for (CompanyEventEmailParam param : params) {
                switch (param.getParamTypeCd()) {
                    case RefundedFeeList:
                        feeRefundList.add(param.getValue());
                        break;
                }

                // keep the cache clear of noise
                Application.evict(param);
            }
        }

        // if we have valid params, create the email
        if (!feeRefundList.isEmpty()) {
            StringBuffer value = new StringBuffer();

            for (String str : feeRefundList) {
                value.append(str);
            }

            email = new EventEmail(pMasterEvent);
            email.addProperty(EventEmailParamTypeCode.RefundedFeeList, value.toString());
        }

        return email;
    }

    private static EventEmail handleNOCEmail(EventStatus pMasterEvent, List<EventStatus> pEventList) {
        // find all the NOC events and link them to the master event
        List<EventStatus> eventList = EmailUtils.findAndLinkLikeEvents(pEventList, pMasterEvent);
        TreeSet<String> eeNameList = new TreeSet<String>(); // sorted list of fee refund params
        int erCount = 0, eeCount = 0;
        String eeName;

        // assemble the params for the email
        for (EventStatus event : eventList) {
            DomainEntitySet<CompanyEventEmailParam> params = event.getEvent().getEmailParamsForEmailEvent();

            eeName = null;

            for (CompanyEventEmailParam param : params) {
                switch (param.getParamTypeCd()) {
                    case EmployeeList:
                        eeName = param.getValue();
                        break;
                }

                // keep the cache clear of noise
                Application.evict(param);
            }

            // if the NOC event contains no EmployeeList param, this is an employer NOC
            if (eeName == null) {
                ++erCount;
            } else {
                ++eeCount;
                eeNameList.add(eeName);
            }
        }

        StringBuffer value = new StringBuffer();

        for (String str : eeNameList) {
            value.append(str);
        }

        EventEmail email = new EventEmail(pMasterEvent);

        if(value.length() > 0) {
            email.addProperty(EventEmailParamTypeCode.EmployeeList, value.toString());
        }

        return email;
    }

    private static EventEmail handleEmployeeBankAccountChangeEmail(EventStatus pMasterEvent, List<EventStatus> pEventList) {
        EventEmail email = null;

        // find all the EmployeeBankAccountChange events and link them to the master event
        // ('like' email events are those destined for the same email account)
        List<EventStatus> eventList = EmailUtils.findAndLinkLikeEventsByEmail(pEventList, pMasterEvent);
        TreeSet<String> paramList = new TreeSet<String>(); // sorted list of EmployeeList params

        // assemble the params for the email
        for (EventStatus event : eventList) {
            DomainEntitySet<CompanyEventEmailParam> params = event.getEvent().getEmailParamsForEmailEvent();

            for (CompanyEventEmailParam param : params) {
                switch (param.getParamTypeCd()) {
                    case EmployeeList:
                        paramList.add(param.getValue());
                        break;
                }

                // keep the cache clear of noise
                Application.evict(param);
            }
        }

        // if we have valid params, create the email
        if (!paramList.isEmpty()) {
            StringBuffer value = new StringBuffer();

            for (String str : paramList) {
                value.append(str);
            }

            email = new EventEmail(pMasterEvent);

            email.addProperty(EventEmailParamTypeCode.EmployeeList, value.toString());
        }

        return email;
    }

    private static EventEmail handlePayeeBankAccountChangeEmail(EventStatus pMasterEvent, List<EventStatus> pEventList) {
        EventEmail email = null;

        // find all the PayeeBankAccountChange events and link them to the master event
        // ('like' email events are those destined for the same email account)
        List<EventStatus> eventList = EmailUtils.findAndLinkLikeEventsByEmail(pEventList, pMasterEvent);
        TreeSet<String> paramList = new TreeSet<String>(); // sorted list of PayeeList params

        // assemble the params for the email
        for (EventStatus event : eventList) {
            DomainEntitySet<CompanyEventEmailParam> params = event.getEvent().getEmailParamsForEmailEvent();

            for (CompanyEventEmailParam param : params) {
                switch (param.getParamTypeCd()) {
                    case PayeeList:
                        paramList.add(param.getValue());
                        break;
                }

                // keep the cache clear of noise
                Application.evict(param);
            }
        }

        // if we have valid params, create the email
        if (!paramList.isEmpty()) {
            StringBuffer value = new StringBuffer();

            for (String str : paramList) {
                value.append(str);
            }

            email = new EventEmail(pMasterEvent);

            email.addProperty(EventEmailParamTypeCode.PayeeList, value.toString());
        }

        return email;
    }

    private static EventEmail handleReversalRequestedEmail(EventStatus pMasterEvent, List<EventStatus> pEventList) {
        EventEmail email = null;

        // find all the ReversalRequested events and link them to the master event
        List<EventStatus> eventList = EmailUtils.findAndLinkLikeEvents(pEventList, pMasterEvent);
        TreeSet<String> reversalList = new TreeSet<String>(); // sorted list of reversal params

        // assemble the params for the email
        for (EventStatus event : eventList) {
            DomainEntitySet<CompanyEventEmailParam> params = event.getEvent().getEmailParamsForEmailEvent();

            for (CompanyEventEmailParam param : params) {
                switch (param.getParamTypeCd()) {
                    case ReversalPendingList:
                        reversalList.add(param.getValue());
                        break;
                }

                // keep the cache clear of noise
                Application.evict(param);
            }
        }

        // if we have valid params, create the email
        if (!reversalList.isEmpty()) {
            StringBuffer value = new StringBuffer();

            for (String str : reversalList) {
                value.append(str);
            }

            email = new EventEmail(pMasterEvent);

            email.addProperty(EventEmailParamTypeCode.ReversalPendingList, value.toString());
        }

        return email;
    }

    private static EventEmail handleBillPaymentReceived(EventStatus pMasterEvent, List<EventStatus> pEventList) {
        EventEmail email = new EventEmail(pMasterEvent);

        DomainEntitySet<CompanyEventEmailParam> vendorPaymentParams = pMasterEvent.getEvent().getEmailParamForEmailEvent(EventEmailParamTypeCode.VendorPaymentList);
        if (vendorPaymentParams.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            StringBuilder suffix = new StringBuilder();
            StringBuilder vendorPaymentListString = new StringBuilder();
            for (CompanyEventEmailParam vendorPaymentParam : vendorPaymentParams) {
                if(vendorPaymentParam.getValue().contains("<table")){
                    vendorPaymentListString.append(vendorPaymentParam.getValue());
                }else if(vendorPaymentParam.getValue().contains("</table>")){
                    suffix.append(vendorPaymentParam.getValue());
                }else {
                    stringBuilder.append(vendorPaymentParam.getValue());
                }
            }
            vendorPaymentListString.append(stringBuilder.toString()).append(suffix.toString());
            email.addProperty(EventEmailParamTypeCode.VendorPaymentList, vendorPaymentListString.toString());
        }

        return email;
    }

    private static EventEmail handlePayrollReceived(EventStatus pMasterEvent, List<EventStatus> pEventList) {
        EventEmail email = new EventEmail(pMasterEvent);

        DomainEntitySet<CompanyEventEmailParam> employeePayrollParams = pMasterEvent.getEvent().getEmailParamForEmailEvent(EventEmailParamTypeCode.EmployeeList);
        if (employeePayrollParams.size() > 1) {
            StringBuilder stringBuilder = new StringBuilder();
            for (CompanyEventEmailParam employeePayrollParam : employeePayrollParams) {
                stringBuilder.append(employeePayrollParam.getValue());
            }
            email.addProperty(EventEmailParamTypeCode.EmployeeList, stringBuilder.toString());
        }

        return email;
    }

    private static EventEmail handleReversalReturnOrReversalOKEmail(EventStatus pMasterEvent, List<EventStatus> pEventList) {
        //
        // For email events with no template type yet assigned, we need to determine the template type
        // based upon the state of the system as related to the given event (i.e. all ReversalOK and
        // ReversalReturn events (if any) must be mapped to their corresponding ReversalRequested events)
        //

        /**
         * This inner class is used to assemble the appropriate EventEmail properties for a group of returns
         */
        class ReversalEmailBuilder {
            private EventEmailTemplateTypeCode mTemplateId = null;
            private TreeSet<String> mSuccessList = new TreeSet<String>(); // sorted list of successful reversals
            private TreeSet<String> mFailureList = new TreeSet<String>(); // sorted list of failed reversals
            private String mCbaLastFour = null;
            private String mPostingDate = null;

            public ReversalEmailBuilder(EventEmailTemplateTypeCode pTemplateId) {
                mTemplateId = pTemplateId;
            }

            public EventEmail createEventEmail(EventStatus pEventStatus) {
                // set template id on master and all linked events
                pEventStatus.setTemplateId(mTemplateId);

                EventEmail email = new EventEmail(pEventStatus);

                // if there are any success messages, add them to the email properties
                if (!mSuccessList.isEmpty()) {
                    StringBuffer value = new StringBuffer();

                    for (String str : mSuccessList) {
                        value.append(str);
                    }

                    email.addProperty(EventEmailParamTypeCode.ReversalSuccessfulList, value.toString());
                }

                // if there are any fail messages, add them to the email properties
                if (!mFailureList.isEmpty()) {
                    StringBuffer value = new StringBuffer();

                    for (String str : mFailureList) {
                        value.append(str);
                    }

                    email.addProperty(EventEmailParamTypeCode.ReversalFailedList, value.toString());
                }

                // if there is a cba, add it to the email properties
                if (mCbaLastFour != null) {
                    email.addProperty(EventEmailParamTypeCode.CompanyBankAccountLastFour, mCbaLastFour);
                }

                // if there is a posting date, add it to the email properties
                if (mPostingDate != null) {
                    email.addProperty(EventEmailParamTypeCode.EffectiveCreditPostingDate, mPostingDate);
                }

                return email;
            }

            public void parseEvent(CompanyEventEmail pEventEmail) {
                DomainEntitySet<CompanyEventEmailParam> params = pEventEmail.getEmailParamsForEmailEvent();

                for (CompanyEventEmailParam param : params) {
                    switch (param.getParamTypeCd()) {
                        case ReversalSuccessfulList:
                            mSuccessList.add(param.getValue());
                            break;

                        case ReversalFailedList:
                            mFailureList.add(param.getValue());
                            break;

                        // the cba will be the same for all params, so only set once
                        case CompanyBankAccountLastFour:
                            if (mCbaLastFour == null) {
                                mCbaLastFour = param.getValue();
                            }
                            break;

                        // the posting date will be the same for all params, so only set once
                        case EffectiveCreditPostingDate:
                            if (mPostingDate == null) {
                                mPostingDate = param.getValue();
                            }
                            break;
                    }

                    // keep the cache clear of noise
                    Application.evict(param);
                }
            }
        }

        Hashtable<String, EventTypeCode> txnStatusMap = EmailUtils.createReversalStatusMap(pMasterEvent);
        List<EventStatus> eventList = EmailUtils.correlateEventsToStatusMap(pEventList, pMasterEvent, txnStatusMap);

        if (eventList.isEmpty()) {
            EmailUtils.formatFailedValidationError("No EmployeeDdReversalDebit transaction found in database matching " +
                                                   pMasterEvent.getEvent().getCompanyEvent().getEventTypeCd().toString() +
                                                   " event", pMasterEvent);
            return null;
        }

        //////////////////////////////////////////////////////////////////////////////////////////////////
        //
        // At this point, one of the following will be true for txnStatusMap:
        //
        //   1) All statuses are of type EventTypeCode.ReversalOK
        //       Result: set template id to AllPaycheckReversalsSuccessful
        //
        //   2) All statuses are of type EventTypeCode.ReversalReturn
        //       Result: set template id to AllPaycheckReversalsFailed
        //
        //   3) Statuses are a mix of ReversalOK and ReversalReturn (no ReversalRequested)
        //       Result: set template id to PartialPaycheckReversal
        //
        //   4) Statuses are a mix of ReversalRequested and ReversalReturn (no ReversalOK)
        //       Result: set all events to deferred (resulting in group imcomplete)
        //
        //   5) Statuses are a mix of ReversalOK and ReversalRequested (no ReversalReturn - this is an error state)
        //
        //   6) Statuses are all still ReversalRequested (this is an error state)
        //
        //   7) Statuses are a mix of all three types (this is an error state)
        //
        //   Note: If we have any ReversalOK events, then we must either send an email or fail
        //
        //////////////////////////////////////////////////////////////////////////////////////////////////

        int revOk = 0, revRet = 0, other = 0;
        for (EventTypeCode eventType : txnStatusMap.values()) {
            switch (eventType) {
                case ReversalOK:
                    ++revOk;
                    break;
                case ReversalReturn:
                    ++revRet;
                    break;
                default:
                    ++other;
                    break;
            }
        }

        // next, we need to determine the email template type if possible
        ReversalEmailBuilder emailBuilder = null;

        if (other > 0) {
            if (revOk > 0) {
                // technically, this should never happen; if it does, it's an error.
                EmailUtils.formatFailedValidationError("Unresolved EmployeeDdReversalDebit transactions found, " +
                                                       "unable to process email.", pMasterEvent);
            } else if (revRet == 0) { // by inferrence, revOk must also be zero here
                // technically, this should never happen; if it does, it's an error.
                EmailUtils.formatFailedValidationError("Failed to correlate reversal event(s) to " +
                                                       "EmployeeDdReversalDebit transaction(s), " +
                                                       "unable to process email.", pMasterEvent);
            } else {
                // since we're still awaiting ReversalOK, we need to defer processing to a later time.
                pMasterEvent.setDeferred();
            }
        } else if (revRet > 0) {
            emailBuilder = (revOk > 0) ?
                           new ReversalEmailBuilder(EventEmailTemplateTypeCode.PartialPaycheckReversal1) :
                           new ReversalEmailBuilder(EventEmailTemplateTypeCode.AllPaycheckReversalsFailed1);
        } else {
            // all events are ReversalOK
            emailBuilder = new ReversalEmailBuilder(EventEmailTemplateTypeCode.AllPaycheckReversalsSuccessful1);
        }

        // if we have a ReversalEmailBuilder, build the reversal email
        if (emailBuilder != null) {
            for (EventStatus event : eventList) {
                emailBuilder.parseEvent(event.getEvent());
            }
        }

        return (emailBuilder != null) ? emailBuilder.createEventEmail(pMasterEvent) : null;
    }
}
