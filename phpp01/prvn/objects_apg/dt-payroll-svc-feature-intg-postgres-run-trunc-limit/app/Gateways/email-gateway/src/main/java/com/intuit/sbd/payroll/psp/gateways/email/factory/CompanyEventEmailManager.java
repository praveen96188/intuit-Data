package com.intuit.sbd.payroll.psp.gateways.email.factory;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmailTemplate;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventRepository;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import com.intuit.sbd.payroll.psp.gateways.email.util.EventStatus;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 26, 2008
 * Time: 10:46:55 PM
 */
public class CompanyEventEmailManager implements IEventRepository {
    private static final String sfNonPayrollKey = "<nonpayroll>";
    private String mSourceCompanyId;
    private String mSourceSystemCd;
    private List<EventStatus> mMasterEventList = new Vector<EventStatus>();
    private Map<EventEmailTemplateTypeCode, EventEmailTemplate> mTemplateMap =
            new Hashtable<EventEmailTemplateTypeCode, EventEmailTemplate>();

    public CompanyEventEmailManager(Company pCompany) {
        mSourceCompanyId = pCompany.getSourceCompanyId();
        mSourceSystemCd = pCompany.getSourceSystemCd().toString();
    }

    public String getCompanyId() {
        return mSourceCompanyId;
    }

    public EventStatus addEvent(CompanyEventEmail pEvent) {
        EventStatus event = new EventStatus(pEvent, this);

        mMasterEventList.add(event);

        return event;
    }

    public void mergeTemplatesIntoMaster(List<EventEmailTemplate> pMasterTemplateList) {
        for (EventEmailTemplate masterTemplate : pMasterTemplateList) {
            masterTemplate.merge(mTemplateMap.get(masterTemplate.getTemplateId())); // handles nulls
        }
    }

    public void persistEventStatuses() {
        for (EventStatus eventStatus : mMasterEventList) {
            eventStatus.persistStatus();
        }
    }

    public void createEmailTemplates() {
        mTemplateMap.clear();

        ////////////////////////////////////////////////////////////////////////////////////
        // First, break out events by payroll run id
        ////////////////////////////////////////////////////////////////////////////////////

        // each payroll run requires its own emails
        Map<String, List<EventStatus>> eventMap = new Hashtable<String, List<EventStatus>>();

        // Key values are the payroll run id; if non-payroll event, use sfNonPayrollKey as key.
        for (EventStatus event : mMasterEventList) {
            try {
                String eventKey = getKeyForEvent(event); // payroll run id or sfNonPayrollKey
                List<EventStatus> list = eventMap.get(eventKey);

                if (list != null) {
                    list.add(event);
                } else {
                    list = new Vector<EventStatus>();
                    list.add(event);
                    eventMap.put(eventKey, list);
                }
            } catch (Exception e) {
                EmailUtils.formatFailedValidationError(e.getMessage(), event);
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////
        // Next, for each collection of payroll run events, create the appropriate templates
        ////////////////////////////////////////////////////////////////////////////////////

        // the same template id may be required for more than one list, in which case we need to merge them
        // (i.e. if two payroll runs for this company were transmitted within the email polling window)
        for (List<EventStatus> eventList : eventMap.values()) {
            Collection<EventEmailTemplate> newTemplateList =
                    EventEmailTemplateFactory.createTemplatesForEvents(eventList);

            // find the matching local template (if any) and either merge the two templates or add the new one
            for (EventEmailTemplate template : newTemplateList) {
                EventEmailTemplate localTemplate = mTemplateMap.get(template.getTemplateId());

                if (localTemplate != null) {
                    localTemplate.merge(template);
                } else {
                    mTemplateMap.put(template.getTemplateId(), template);
                }
            }
        }
    }

    private String getKeyForEvent(EventStatus pEvent) {
        String key;
        PayrollRun payrollRun;

        CompanyEvent event = pEvent.getEvent().getCompanyEvent();

        switch (event.getEventTypeCd()) {
            case PayrollReceived: // retrieve PayrollRun id
            case BillPaymentReceived:
            case PayrollCancelPending:
            case PayrollCancelled:
            case BillPaymentOffloaded:
            case LastChanceNotify:
            case ChangeRedebitToWireExpected:
            case DeletedPaycheckAlreadyOffloadedToTOK:
            case VoidedPaycheckAlreadyOffloadedToTOK:
            case SUIEoqDebitCreated:
            case SUIEoqCreditCreated:
            case SUIImmediateCreditCreated:
            case SUIImmediateDebitCreated:
            case InvalidVendorEmail:
            case MonthlyFeeCreated:
            case CreditReduction:
            case SUICreditsApplied:
            case PendingPaymentRefunded:
               key = EmailUtils.getDetailString(pEvent, EventDetailTypeCode.PayrollRunId);

                if (key == null) {
                    throw new RuntimeException("Required event detail PayrollRunId is missing or invalid " +
                                               "[event type is " + event.getEventTypeCd().toString() + "]");
                }
                break;

            case NonAchPaymentReceived: // retrieve FinancialTransaction, then PayrollRun id
            case FeeCreated:
            case ReversalOK:
            case ReversalReturn:
            case ReversalRequested:
            case NSF:
            case DDDebitReturn:
            case DDReject:
            case ManualRedebitCreated:
                FinancialTransaction ft = EmailUtils.getFinancialTransaction(pEvent);

                if (ft == null) {
                    throw new RuntimeException("Required event detail FinancialTransactionId is missing or invalid " +
                                               "[event type is " + event.getEventTypeCd().toString() + "]");
                }

                payrollRun = ft.getPayrollRun();

                key = payrollRun.getId().toString();

                // keep the cache clear of noise
                Application.evict(payrollRun);
                Application.evict(ft);
                break;

            case FeeRebilled: // retrieve BillingDetail, then PayrollRun id
                BillingDetail bd = EmailUtils.getFeeBillingDetail(pEvent);

                if (bd == null) {
                    throw new RuntimeException("Required event detail FeeBillingDetailId is missing or invalid " +
                                               "[event type is " + event.getEventTypeCd().toString() + "]");
                }

                payrollRun = bd.getPayrollRun();

                key = payrollRun.getId().toString();

                // keep the cache clear of noise
                Application.evict(payrollRun);
                Application.evict(bd);
                break;

            case CustomerSignedUp: // non-payroll events use the default key (sfNonPayrollKey)
            case PINCreated:
            case BankAccountVerified:
            case ServiceStatusChange:
            case PINUpdated:
            case CompanyBankAccountChange:
            case EmployeeBankAccountChange:
            case PayeeBankAccountChange:
            case ERLoanNOC:
            case NOC:
            case CompanyContactEmailChanged:
            case EnrollmentStatusChanged:
            case CBAVerifyReturn:
            case TOKNotifiedOfCompanyFraud:
            case PreOffload401kValidationAlert:
            case PostOffload401kValidationAlert:
            case NonPrintChecks:
            case AssistedFailedEnrollment:
            case AssistedPayrollConfirmation:
            case ServiceKeyUpdated:
            case WelcomeEmail:
            case UsageBilling25DaysIntoSubscription:
            case UsageBilling15DaysIntoSubscription:
            case VmpSignUpEmployerEmail:
            case VmpSignUpEmployeeEmail:
            case PaystubCreated:
            case FeeRefunded:
            case EntitlementUnitAdded:
            case LegacySubscriptionMigration:
                key = sfNonPayrollKey;
                break;

            default:
                throw new RuntimeException("Specified event type is not a valid event type for email " +
                                           "[event type is " + event.getEventTypeCd().toString() + "]");
        }

        return key;
    }

    public StringBuffer reportErrors() {
        StringBuffer err = new StringBuffer();
        StringBuffer subErr = new StringBuffer();
        List<EventStatus> unprocessedEvents = new Vector<EventStatus>();

        // Need to report errors for events that didn't make it into an EventEmailTemplate
        // - all events must pass validation before they can be assigned to an email template
        // - all events that do pass validation are automatically assigned to an email template
        // - events can fail validation for reasons other than direct errors (i.e. deferred events)
        for (EventStatus event : mMasterEventList) {
            if (!event.isValidated()) {
                unprocessedEvents.add(event);
            }
        }

        // report errors for all unprocessed, non-templated events (if any)
        if (!unprocessedEvents.isEmpty()) {
            StringBuffer validationErr = new StringBuffer();

            // this will only report out events that are signaled with an error
            // (events in a non-error state (i.e. deferred events) will not be reported here)
            for (EventStatus event : unprocessedEvents) {
                validationErr.append(event.reportErrors());
            }

            // if we've had any validation errors, go ahead and init the header.
            if (validationErr.length() > 0) {
                subErr.append(EmailUtils.sfNewLine);

                subErr.append("*** ");
                subErr.append("Company Events failing validation");
                subErr.append(" ***");
                subErr.append(EmailUtils.sfNewLine);

                subErr.append(validationErr);
            }
        }

        // report errors for all templated events
        for (EventEmailTemplate template : mTemplateMap.values()) {
            subErr.append(template.reportErrors());
        }

        if (subErr.length() > 0) {
            err.append(EmailUtils.sfNewLine);

            err.append("********************************************************************************");
            err.append(EmailUtils.sfNewLine);
            err.append("* Company Event Email Errors For Company ID: ");
            err.append(mSourceSystemCd);
            err.append(":");
            err.append(mSourceCompanyId);
            err.append(EmailUtils.sfNewLine);
            err.append("********************************************************************************");
            err.append(EmailUtils.sfNewLine);

            err.append(subErr);
        }

        return err;
    }
}
