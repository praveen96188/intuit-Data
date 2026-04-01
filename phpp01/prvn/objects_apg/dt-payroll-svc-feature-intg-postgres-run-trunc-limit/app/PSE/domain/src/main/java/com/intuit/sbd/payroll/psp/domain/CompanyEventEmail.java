package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.domain.util.EmailUtils;
import com.intuit.sbd.payroll.psp.domain.util.EnumUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import java.util.*;

/**
 * Hand-written business logic
 */
public class CompanyEventEmail extends BaseCompanyEventEmail {
    public static SpcfLogger logger = SpcfLogManager.getLogger(CompanyEventEmail.class);
    public static final String DELETE_TEXT = "Deleted";
    public static final String VOID_TEXT = "Voided";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Static create/update
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void createEmailForEvents(List<CompanyEvent> pEventQueue) {
        if (pEventQueue != null) {
            String oldPayrollAdminEmail = null;
            List<CompanyEvent> eebaChangeList = new Vector<CompanyEvent>();
            List<CompanyEvent> pbaChangeList = new Vector<CompanyEvent>();

            // Iterate all company events in the list and process them for client email.
            for (CompanyEvent event : pEventQueue) {
                // create the company event email normally.  If there is an OverrideRecipientEmailAddress, pass it in.  Otherwise, this value
                //will be null and the event will be created with the email address appropriate to the template type
                String overrideRecipient = EmailUtils.getDetailString(event, EventDetailTypeCode.OverrideRecipientEmailAddress);
                createEmailForEvent(event, overrideRecipient);

                switch (event.getEventTypeCd()) {
                    //
                    // Save the EE BA change events for later use in the fraud check.
                    // (see comments on CompanyContactEmailChanged case for more info.)
                    //
                    case EmployeeBankAccountChange:
                        eebaChangeList.add(event);
                        break;

                    case PayeeBankAccountChange:
                        pbaChangeList.add(event);
                        break;

                    //
                    // We're doing two separate things with the CompanyContactEmailChanged event:
                    // 1) We want to send an email to both the old and new email addresses. The above call to
                    //    createEmailForEvent() will send an email to the new email address, so we need to force
                    //    the case for sending an email to the old email address.
                    // 2) We need to perform a fraud check to see if the PayrollAdmin email address has changed
                    //    at the same time as one or more EE bank account(s) have changed. If this is a PA change,
                    //    we will save the old email address for later use in the below fraud check.
                    //
                    case CompanyContactEmailChanged:
                        String recipient = EmailUtils.getDetailString(event, EventDetailTypeCode.OldStringValue);

                        if (recipient != null) {
                            createEmailForEvent(event, recipient);
                        }

                        // Check to see if it is the PayrollAdmin that has changed (used in below EEBA fraud check)
                        Contact contact = EmailUtils.getContact(event);

                        if ((contact != null) && (contact.getContactRoleCd() == ContactRole.PayrollAdmin)) {
                            oldPayrollAdminEmail = recipient;
                        }
                        break;
                }
            }

            //
            // If the PA contact email has changed and there was one or more EE BA change(s), we need to send
            // an email to the old PA email address (to keep the client informed of potential fraud).
            // (we need to do this outside the above loop to ensure event order doesn't skew the logic)
            //
            if ((oldPayrollAdminEmail != null) && !eebaChangeList.isEmpty()) {
                //
                // Since both the PA contact email has changed and there were EE BA changes, we need to send
                // an email to the old PA email address to ensure the client knows what�s happening.
                //
                for (CompanyEvent eebaEvent : eebaChangeList) {
                    createEmailForEvent(eebaEvent, oldPayrollAdminEmail);
                }
            }

            // Create Email for Payee BA change
            if ((oldPayrollAdminEmail != null) && !pbaChangeList.isEmpty()) {
                for (CompanyEvent pbaEvent : pbaChangeList) {
                    createEmailForEvent(pbaEvent, oldPayrollAdminEmail);
                }
            }
        }
    }

    /**
     * Create the CompanyEventEmail entries to facilitate client email via the EmailGateway.
     *
     * @param pCompanyEvent           The company event for which to create a client email event.
     * @param pRecipientEmailOverride Override the default email recipient for the given event type (usually null).
     */
    public static void createEmailForEvent(CompanyEvent pCompanyEvent, String pRecipientEmailOverride) {
        //
        // Catch all email param formatting exceptions and don't let them propagate out of this method
        // (we don't want to stop the business process because we couldn't create an email)
        //

        // used to ensure we only log errors once
        boolean errorLogged = false;

        CompanyEventEmail eventEmail = new CompanyEventEmail();
        eventEmail.setCompanyEvent(pCompanyEvent);
        eventEmail.setCompany(pCompanyEvent.getCompany());
        eventEmail.setStatusCd(EventEmailStatus.Pending);
        eventEmail.setStatusEffectiveDate(PSPDate.getPSPTime());
        eventEmail.setEmailTemplateTypeCd(null);
        eventEmail = Application.save(eventEmail);

        try {
            switch (pCompanyEvent.getEventTypeCd()) {
                case CustomerSignedUp:
                    eventEmail.createCustomerSignedUpEmail();
                    break;
                case PINCreated:
                case BankAccountVerified:
                    eventEmail.createPINCreatedEmailParams();
                    break;
                case NonAchPaymentReceived:
                    eventEmail.createNonAchPaymentReceivedEmailParams();
                    break;
                case PayrollReceived:
                    eventEmail.createPayrollReceivedEmailParams();
                    break;
                case FeeCreated:
                    eventEmail.createFeeCreatedEmailParams();
                    break;
                case FeeRebilled:
                    eventEmail.createFeeRebilledEmailParams();
                    break;
                case FeeRefunded:
                    eventEmail.createFeeRefundedEmailParams();
                    break;
                case ReversalOK:
                    eventEmail.createReversalOKEmailParams();
                    break;
                case ReversalReturn:
                    eventEmail.createReversalReturnEmailParams();
                    break;
                case ReversalRequested:
                    eventEmail.createReversalRequestedEmailParams();
                    break;
                case ServiceStatusChange:
                    eventEmail.createServiceStatusChangeEmailParams();
                    break;
                case PINUpdated:
                    eventEmail.createPINUpdatedEmailParams();
                    break;
                case CompanyBankAccountChange:
                    eventEmail.createCompanyBankAccountChangeEmailParams();
                    break;
                case EmployeeBankAccountChange:
                    eventEmail.createEmployeeBankAccountChangeEmailParams(pRecipientEmailOverride);
                    break;
                case PayeeBankAccountChange:
                    eventEmail.createPayeeBankAccountChangeEmailParams(pRecipientEmailOverride);
                    break;
                case NOC:
                    eventEmail.createNOCEmailParams();
                    break;
                case ERLoanNOC:
                    eventEmail.createERLoanNOCEmailParams();
                    break;
                case CompanyContactEmailChanged:
                    eventEmail.createCompanyContactEmailChangedEmailParams(pRecipientEmailOverride);
                    break;
                case CBAVerifyReturn:
                    eventEmail.createCBAVerifyReturnEmailParams();
                    break;
                case NSF:
                    eventEmail.createNSFEmailParams();
                    break;
                case DDDebitReturn:
                    eventEmail.createDDDebitReturnEmailParams();
                    break;
                case ChangeRedebitToWireExpected:
                    eventEmail.createChangeRedebitToWireExpectedEmailParams();
                    break;
                case ManualRedebitCreated:
                    eventEmail.createManualRedebitCreatedEmailParams();
                    break;
                case LastChanceNotify:
                    eventEmail.createLastChanceNotifyEmailParams();
                    break;
                case PayrollCancelPending:
                    eventEmail.createPayrollCancelPendingEmailParams();
                    break;
                case PayrollCancelled:
                    eventEmail.createPayrollCancelledEmailParams();
                    break;
                case DDReject:
                    eventEmail.createDDRejectEmailParams();
                    break;
//                case EnrollmentStatusChanged:
//                    eventEmail.createEnrollmentStatusChangedParams();
//                    break;
                case TOKNotifiedOfCompanyFraud:
                    eventEmail.createTOKNotifiedOfCompanyFraudParams(pRecipientEmailOverride);
                    break;
                case DeletedPaycheckAlreadyOffloadedToTOK:
                    eventEmail.createTOKNotifiedOfOperationAfterOffloadParams(DELETE_TEXT, pRecipientEmailOverride);
                    break;
                case VoidedPaycheckAlreadyOffloadedToTOK:
                    eventEmail.createTOKNotifiedOfOperationAfterOffloadParams(VOID_TEXT, pRecipientEmailOverride);
                    break;
                case BillPaymentOffloaded:
                    eventEmail.createBillPaymentOffloadedParams();
                    break;
                case BillPaymentReceived:
                    eventEmail.createBillPaymentReceivedParams();
                    break;
                case NonPrintChecks:
                    eventEmail.createNoPrintChecksParams();
                    break;
                case PreOffload401kValidationAlert:
                    eventEmail.createOffload401kValidationAlert(true);
                    break;
                case PostOffload401kValidationAlert:
                    eventEmail.createOffload401kValidationAlert(false);
                    break;
                case AssistedFailedEnrollment:
                    eventEmail.createAssistedEnrollmentFailedEmailParams();
                    break;
                case AssistedPayrollConfirmation:
                    eventEmail.createAssistedPayrollConfirmationEmailParams();
                    break;
                case ServiceKeyUpdated:
                    eventEmail.createServiceKeyUpdatedEmailParams();
                    break;
                case SUIEoqDebitCreated:
                case SUIEoqCreditCreated:
                case SUIImmediateCreditCreated:
                case SUIImmediateDebitCreated:
                    eventEmail.createSUIAdjustmentTransactionParams(pCompanyEvent.getEventTypeCd());
                    break;
                case WelcomeEmail:
                    eventEmail.createWelcomeEmail();
                    break;
                case UsageBilling25DaysIntoSubscription:
                    eventEmail.createUsageBillingDetailsHowTosEmail();
                    break;
                case UsageBilling15DaysIntoSubscription:
                    eventEmail.createUsageBillingMidTrial();
                    break;
                case CreditReduction:
                    eventEmail.createCreditReductionEmailParams();
                    break;
                case VmpSignUpEmployeeEmail:
                    eventEmail.createVmpSignUpEmployeeEmailParams();
                    break;
                case VmpSignUpEmployerEmail:
                    eventEmail.createVmpSignUpEmployerEmailParams();
                    break;
                case PaystubCreated:
                    //PaystubCreated emails start out in FormatError status because the IamEmailAddressProcessor needs to
                    //add the IAM email address to them still
                    eventEmail.setStatusCd(EventEmailStatus.FormatError);
                    eventEmail.createVmpPaystubNotificationEmailParams();
                    break;
                case MonthlyFeeCreated:
                    eventEmail.createMonthlyFeeCreatedParams(pCompanyEvent.getEventTypeCd());
                    break;
                case InvalidVendorEmail:
                    eventEmail.createInvalidVendorEmailParams();
                    break;
                case SUICreditsApplied:
                    eventEmail.createSUICreditsAppliedEmailParams();
                    break;
                case EntitlementUnitAdded:
                    eventEmail.createNewEntitlementEmailParams(pRecipientEmailOverride);
                    break;
                case LegacySubscriptionMigration:
                    eventEmail.createLegacySubscriptionMigrationEmailParams(pRecipientEmailOverride);
                    break;
                case PendingPaymentRefunded:
                    eventEmail.createPendingRefundEmailParams();
                    break;
                default:
                    throw new RuntimeException(
                            EmailUtils.formatErrorMsg(eventEmail,
                                                      "Specified event is not a valid event type for email."));
            }
        } catch (Throwable t) {
            // flag this as an email related event that failed to format email params
            eventEmail.setStatusCd(EventEmailStatus.FormatError);
            eventEmail.setStatusEffectiveDate(PSPDate.getPSPTime());

            logger.warn("Company event email formatting error. ", t);

            errorLogged = true;
        } finally {
            // if we have a template type assigned, save the email, otherwise delete it.
            if (eventEmail.getEmailTemplateTypeCd() != null) {
                Application.save(eventEmail);
            } else {
                // if we haven't already logged the problem, log it now
                if (!errorLogged) {
                    logger.error(EmailUtils.formatErrorMsg(eventEmail,
                                                           "Company event email formatting error (deleting email)."));
                }

                // email is incomplete, so we need to clean up email and params
                for (CompanyEventEmailParam param : eventEmail.getEmailParamForEmailEvent()) {
                    Application.delete(param);
                }

                Application.delete(eventEmail);
            }
        }
    }

    public static void createEmailForEvent(CompanyEvent pCompanyEvent, Contact pContact) {
        //
        // Catch all email param formatting exceptions and don't let them propagate out of this method
        // (we don't want to stop the business process because we couldn't create an email)
        //

        // used to ensure we only log errors once
        boolean errorLogged = false;

        CompanyEventEmail eventEmail = new CompanyEventEmail();
        eventEmail.setCompanyEvent(pCompanyEvent);
        eventEmail.setCompany(pCompanyEvent.getCompany());
        eventEmail.setStatusCd(EventEmailStatus.Pending);
        eventEmail.setStatusEffectiveDate(PSPDate.getPSPTime());
        eventEmail.setEmailTemplateTypeCd(null);
        eventEmail = Application.save(eventEmail);

        try {
            switch (pCompanyEvent.getEventTypeCd()) {
                case CustomerSignedUp:
                    eventEmail.createCustomerSignedUpEmailParams(pContact);
                    break;

                default:
                    throw new RuntimeException(
                            EmailUtils.formatErrorMsg(eventEmail,
                                                      "Specified event is not a valid event type for email."));
            }
        } catch (Throwable t) {
            // flag this as an email related event that failed to format email params
            eventEmail.setStatusCd(EventEmailStatus.FormatError);
            eventEmail.setStatusEffectiveDate(PSPDate.getPSPTime());

            logger.warn("Company event email formatting error. ", t);

            errorLogged = true;
        } finally {
            // if we have a template type assigned, save the email, otherwise delete it.
            if (eventEmail.getEmailTemplateTypeCd() != null) {
                Application.save(eventEmail);
            } else {
                // if we haven't already logged the problem, log it now
                if (!errorLogged) {
                    logger.error(EmailUtils.formatErrorMsg(eventEmail,
                                                           "Company event email formatting error (deleting email)."));
                }

                // email is incomplete, so we need to clean up email and params
                for (CompanyEventEmailParam param : eventEmail.getEmailParamForEmailEvent()) {
                    Application.delete(param);
                }

                Application.delete(eventEmail);
            }
        }
    }

    public static DomainEntitySet<CompanyEventEmail> findCompanyEventEmailsByEventDetailAndEmailTarget(Company company, EventTypeCode eventType, EventDetailTypeCode code, String[] values, String emailAddress) {
        String[] paramNames = new String[5];
        paramNames[0] = "company";
        paramNames[1] = "eventType";
        paramNames[2] = "eventDetailTypeCode";
        paramNames[3] = "values";
        paramNames[4] = "emailAddress";

        Object[] paramValues = new Object[5];
        paramValues[0] = company;
        paramValues[1] = eventType;
        paramValues[2] = code;
        paramValues[3] = values;
        paramValues[4] = emailAddress;

        return Application.findByNamedQuery("findCompanyEventEmailsByEventDetailAndEmailTarget", paramNames, paramValues);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public CompanyEventEmail() {
        super();
    }

    public static DomainEntitySet<CompanyEventEmail> findEmailEventsByTemplateAndStatus(EventEmailStatus pStatus, EventEmailTemplateTypeCode... pTemplateTypeCode) {
        Expression<CompanyEventEmail> query =
                new Query<CompanyEventEmail>()
                        .Where(CompanyEventEmail.EmailTemplateTypeCd().in(pTemplateTypeCode)
                                                .And(CompanyEventEmail.StatusCd().equalTo(pStatus)))
                        .OrderBy(CompanyEventEmail.CreatedDate());

        return Application.find(CompanyEventEmail.class, query);
    }

    public static DomainEntitySet<CompanyEventEmail> findEmailEventsByCompanyAndTemplate(Company pCompany, EventEmailTemplateTypeCode... pTemplateTypeCode) {
        Expression<CompanyEventEmail> query =
                new Query<CompanyEventEmail>()
                        .Where(CompanyEventEmail.EmailTemplateTypeCd().in(pTemplateTypeCode)
                                                .And(CompanyEventEmail.CompanyEvent().Company().equalTo(pCompany)).And(CompanyEventEmail.Company().equalTo(pCompany)))
                        .OrderBy(CompanyEventEmail.CreatedDate().Descending());

        return Application.find(CompanyEventEmail.class, query);
    }

    public static DomainEntitySet<CompanyEventEmail> findEmailEventsByStatus(EventEmailStatus... pStatusList) {
        return findEmailEventsByStatus(0, pStatusList);
    }

    public static DomainEntitySet<CompanyEventEmail> findEmailEventsByStatus(int pMaxResults, EventEmailStatus... pStatusList) {
        Query<CompanyEventEmail> query = new Query<CompanyEventEmail>();

        Criterion<CompanyEventEmail> emailEventEmailCriterion = CompanyEventEmail.StatusCd().in(pStatusList);
        if(Company.isDGDeleteFeatureEnabled()){
            emailEventEmailCriterion = emailEventEmailCriterion.And(CompanyEventEmail.CompanyEvent().Company().IsDgDisassociated().equalTo(Boolean.FALSE));
        }

        emailEventEmailCriterion = emailEventEmailCriterion.And(CompanyEventEmail.Company().equalTo(CompanyEventEmail.CompanyEvent().Company()));
        query.Where(emailEventEmailCriterion);
        query.EagerLoad(CompanyEventEmail.CompanyEvent(),
                        CompanyEventEmail.CompanyEvent().Company());
        query.OrderBy(CompanyEventEmail.CreatedDate());

        if (pMaxResults > 0) {
            query.LimitResults(0, pMaxResults);
        }

        return Application.find(CompanyEventEmail.class, query);
    }

    public DomainEntitySet<CompanyEventEmailParam> getEmailParamsForEmailEvent() {
        return getEmailParamForEmailEvent();
    }

    public DomainEntitySet<CompanyEventEmailParam> getEmailParamForEmailEvent(EventEmailParamTypeCode... pParamType) {
        Criterion<CompanyEventEmailParam> where = CompanyEventEmailParam.CompanyEventEmail().equalTo(this);

        if ((pParamType != null) && (pParamType.length > 0)) {
            where = where.And(CompanyEventEmailParam.ParamTypeCd().in(pParamType));
        }

        Expression<CompanyEventEmailParam> query = new Query<CompanyEventEmailParam>().Where(where).OrderBy(CreatedDate());

        return Application.find(CompanyEventEmailParam.class, query);
    }

    public String getEmailParamValue(EventEmailParamTypeCode pParamType) {
        DomainEntitySet<CompanyEventEmailParam> emailParamForEmailEvent = getEmailParamForEmailEvent(pParamType);
        if (emailParamForEmailEvent.size() > 1) {
            throw new RuntimeException("Multiple values for same email parameter");
        }
        CompanyEventEmailParam param = emailParamForEmailEvent.getFirst();
        if (param == null) {
            return null;
        }

        return param.getValue();
    }


    //
    // Email parameter population methods
    //

    protected void setCommonEmailParams(EventEmailTemplateTypeCode pTemplateType,
                                        ContactRole pLookupContactRole,
                                        ContactRole pRecipientContactRole,
                                        String pRecipientEmailOverride) {
        Company company = getCompanyEvent().getCompany();
        Contact contact = company.getContactByRoleCode(pLookupContactRole);

        setCommonEmailParams(pTemplateType, contact, pRecipientContactRole, pRecipientEmailOverride);
    }

    protected void setCommonEmailParams(EventEmailTemplateTypeCode pTemplateType,
                                        ContactRole pLookupContactRole,
                                        ContactRole pRecipientContactRole) {
        setCommonEmailParams(pTemplateType, pLookupContactRole, pRecipientContactRole, null);
    }

    protected void setCommonEmailParams(EventEmailTemplateTypeCode pTemplateType,
                                        ContactRole pContactRole,
                                        String pRecipientEmailOverride) {
        setCommonEmailParams(pTemplateType, pContactRole, pContactRole, pRecipientEmailOverride);
    }

    protected void setCommonEmailParams(EventEmailTemplateTypeCode pTemplateType,
                                        ContactRole pContactRole) {
        setCommonEmailParams(pTemplateType, pContactRole, pContactRole, null);
    }

    public void setCommonEmailParams(
            EventEmailTemplateTypeCode pTemplateType,
            Contact pContact,
            ContactRole pRecipientContactRole,
            String pRecipientEmailOverride) {
        EventEmailParamTypeCode firstNameParam;
        EventEmailParamTypeCode lastNameParam;
        EventEmailParamTypeCode emailAddressParam;

        if (pContact == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this, "Specified contact is null."));
        }

        switch (pRecipientContactRole) {
            case PayrollAdmin:
                firstNameParam = EventEmailParamTypeCode.PayrollAdminFirstName;
                lastNameParam = EventEmailParamTypeCode.PayrollAdminLastName;
                emailAddressParam = EventEmailParamTypeCode.PayrollAdminEmail;
                break;

            case PrimaryPrincipal:
                firstNameParam = EventEmailParamTypeCode.PrimaryPrincipalFirstName;
                lastNameParam = EventEmailParamTypeCode.PrimaryPrincipalLastName;
                emailAddressParam = EventEmailParamTypeCode.PrimaryPrincipalEmail;
                break;

            case SecondaryPrincipal:
                firstNameParam = EventEmailParamTypeCode.PrimaryPrincipalFirstName;
                lastNameParam = EventEmailParamTypeCode.PrimaryPrincipalLastName;
                emailAddressParam = EventEmailParamTypeCode.PrimaryPrincipalEmail;
                break;
            default:
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Invalid contact specified; must be either PrimaryPrincipal or PayrollAdmin."));
        }

        Company company = getCompanyEvent().getCompany();
        SourceSystemCode sourceSystem = company.getSourceSystemCd();
        String firstNameValue = WordUtils.capitalize(pContact.getFirstName().toLowerCase());
        String lastNameValue = WordUtils.capitalize(pContact.getLastName().toLowerCase());
        String emailAddressValue;

        if ((pRecipientEmailOverride != null) && (pRecipientEmailOverride.length() > 0)) {
            emailAddressValue =pRecipientEmailOverride  ;
        } else {
            emailAddressValue = pContact.getEmail();
        }

        // if the contact info is invalid, we can't proceed with this email
        if ((firstNameValue == null) || (lastNameValue == null)) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve valid first/last name for specified contact role " +
                                                                         pContact.getContactRoleCd().toString() + "."));
        } else if (emailAddressValue == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve valid email address for specified contact role " +
                                                                         pContact.getContactRoleCd().toString() + "."));
        }

        // set contact first name param
        addEmailParameter(firstNameParam, firstNameValue);

        // set contact last name param
        addEmailParameter(lastNameParam, lastNameValue);

        // set contact email address param
        addEmailParameter(emailAddressParam, emailAddressValue);

        // set company legal name param
        addEmailParameter(EventEmailParamTypeCode.CompanyLegalName, company.getLegalName());

        // set source payroll system param
        addEmailParameter(EventEmailParamTypeCode.SourcePayrollSystem, sourceSystem.toString());

        CompanyService companyService = EmailUtils.getCompanyService(getCompanyEvent());
        if (companyService != null) {
            addEmailParameter(EventEmailParamTypeCode.ServiceType, EmailUtils.getServiceTypeDescription(companyService));
        }

        setEmailTemplateTypeCd(pTemplateType);
    }

    public void setCommonEmailParams(EventEmailTemplateTypeCode pTemplateType, Entitlement pEntitlement) {
        if (pEntitlement == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this, "Specified entitlement is null."));
        }

        // set CAN
        String can = pEntitlement.getCustomerId();
        if (can == null) {
            can = "Customer Account Number not available";
        }
        addEmailParameter(EventEmailParamTypeCode.CustomerAccountNnumber, can);

        // set contact name
        String contactName = pEntitlement.getContactName();
        if (contactName == null) {
            contactName = "Customer";
        }
        addEmailParameter(EventEmailParamTypeCode.BillingContactName, contactName);

        // set contact email address param
        // todo do we need a new email parameter?
        // todo if we send an invalid email address what happens?
        addEmailParameter(EventEmailParamTypeCode.PayrollAdminEmail, pEntitlement.getContactEmail());

        // set company legal name param
        Company company = getCompanyEvent().getCompany();
        addEmailParameter(EventEmailParamTypeCode.CompanyLegalName, company.getLegalName());

        // set source payroll system param
        addEmailParameter(EventEmailParamTypeCode.SourcePayrollSystem, company.getSourceSystemCd().toString());

        CompanyService companyService = EmailUtils.getCompanyService(getCompanyEvent());
        if (companyService != null) {
            addEmailParameter(EventEmailParamTypeCode.ServiceType, EmailUtils.getServiceTypeDescription(companyService));
        }

        setEmailTemplateTypeCd(pTemplateType);
    }


    public void setCommonEmailParams(
            EventEmailTemplateTypeCode pTemplateType,
            ContactRole pRecipientContactRole,
            String pRecipientEmailOverride,
            boolean pUseRoleForNameParam) {
        Company company = getCompanyEvent().getCompany();
        Contact contact = company.getContactByRoleCode(pRecipientContactRole);
        EventEmailParamTypeCode firstNameParam = EventEmailParamTypeCode.PayrollAdminFirstName;
        EventEmailParamTypeCode lastNameParam = EventEmailParamTypeCode.PayrollAdminLastName;
        EventEmailParamTypeCode emailAddressParam = EventEmailParamTypeCode.PayrollAdminEmail;

        if (contact == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this, "Specified contact is null."));
        }
        if (pUseRoleForNameParam) {
            switch (pRecipientContactRole) {
                case PayrollAdmin:
                    firstNameParam = EventEmailParamTypeCode.PayrollAdminFirstName;
                    lastNameParam = EventEmailParamTypeCode.PayrollAdminLastName;
                    emailAddressParam = EventEmailParamTypeCode.PayrollAdminEmail;
                    break;

                case PrimaryPrincipal:
                    firstNameParam = EventEmailParamTypeCode.PrimaryPrincipalFirstName;
                    lastNameParam = EventEmailParamTypeCode.PrimaryPrincipalLastName;
                    emailAddressParam = EventEmailParamTypeCode.PrimaryPrincipalEmail;
                    break;

                case SecondaryPrincipal:
                    firstNameParam = EventEmailParamTypeCode.PrimaryPrincipalFirstName;
                    lastNameParam = EventEmailParamTypeCode.PrimaryPrincipalLastName;
                    emailAddressParam = EventEmailParamTypeCode.PrimaryPrincipalEmail;
                    break;
                default:
                    throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                         "Invalid contact specified; must be either PrimaryPrincipal or PayrollAdmin."));
            }
        }

        SourceSystemCode sourceSystem = company.getSourceSystemCd();
        String firstNameValue = WordUtils.capitalize(contact.getFirstName().toLowerCase());
        String lastNameValue = WordUtils.capitalize(contact.getLastName().toLowerCase());
        String emailAddressValue;

        if ((pRecipientEmailOverride != null) && (pRecipientEmailOverride.length() > 0)) {
            emailAddressValue = pRecipientEmailOverride;
        } else {
            emailAddressValue = contact.getEmail();
        }

        // if the contact info is invalid, we can't proceed with this email
        if ((firstNameValue == null) || (lastNameValue == null)) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve valid first/last name for specified contact role " +
                                                                         contact.getContactRoleCd().toString() + "."));
        } else if (emailAddressValue == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve valid email address for specified contact role " +
                                                                         contact.getContactRoleCd().toString() + "."));
        }

        // set contact first name param
        addEmailParameter(firstNameParam, firstNameValue);

        // set contact last name param
        addEmailParameter(lastNameParam, lastNameValue);

        // set contact email address param
        addEmailParameter(emailAddressParam, emailAddressValue);

        // set company legal name param
        addEmailParameter(EventEmailParamTypeCode.CompanyLegalName, company.getLegalName());

        // set source payroll system param
        addEmailParameter(EventEmailParamTypeCode.SourcePayrollSystem, sourceSystem.toString());

        CompanyService companyService = EmailUtils.getCompanyService(getCompanyEvent());
        if (companyService != null) {
            addEmailParameter(EventEmailParamTypeCode.ServiceType, EmailUtils.getServiceTypeDescription(companyService));
        }

        setEmailTemplateTypeCd(pTemplateType);
    }

    public void setCommonEmailParams(
            EventEmailTemplateTypeCode pTemplateType,
            String pRecipientName,
            String pRecipientEmail) {
        // if the  info is invalid, we can't proceed with this email
        if (pRecipientName == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this, "Specified recipient is null."));
        }

        if (pRecipientEmail == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this, "Specified recipient�s email is null."));
        }

        if (!Validator.isValidEmail(pRecipientEmail)) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this, "Specified recipient�s email is invalid: " + pRecipientEmail));
        }

        Company company = getCompanyEvent().getCompany();
        SourceSystemCode sourceSystem = company.getSourceSystemCd();


        // set contact first name param
        addEmailParameter(EventEmailParamTypeCode.PayrollAdminFirstName, pRecipientName);

        // set contact last name param
        addEmailParameter(EventEmailParamTypeCode.PayrollAdminLastName, ",");

        // set contact email address param
        addEmailParameter(EventEmailParamTypeCode.PayrollAdminEmail, pRecipientEmail);

        // set company legal name param
        addEmailParameter(EventEmailParamTypeCode.CompanyLegalName, company.getLegalName());

        // set source payroll system param
        addEmailParameter(EventEmailParamTypeCode.SourcePayrollSystem, sourceSystem.toString());

        setEmailTemplateTypeCd(pTemplateType);
    }

    protected void createWelcomeEmail() {
        EntitlementUnit eu = EmailUtils.getEntitlementUnit(getCompanyEvent());
        Entitlement e = eu.getEntitlement();

        String emailTemplateType = EmailUtils.getDetailString(getCompanyEvent(), EventDetailTypeCode.EmailTemplateType);
        if (emailTemplateType != null) {
            EventEmailTemplateTypeCode template = EventEmailTemplateTypeCode.valueOf(emailTemplateType);

            setCommonEmailParams(template, ContactRole.PayrollAdmin);
            addEmailParameter(EventEmailParamTypeCode.SubTypeDescription, EmailUtils.getSubTypeDescription(e.getEntitlementCode()));
            addEmailParameter(EventEmailParamTypeCode.ServiceKey, eu.getFullServiceKey());
        } else {
            logger.warn("Unknown EventEmailTemplateTypeCode for CompanyEvent:" + getCompanyEvent().getId());
        }
    }

    protected void createUsageBillingDetailsHowTosEmail() {

        EntitlementUnit eu = EmailUtils.getEntitlementUnit(getCompanyEvent());
        Entitlement e = eu.getEntitlement();

         /* check for SymphonyWelcomeOneMonthReactivation product */
        if (checkForSymphonyOneMonthReactivation(e)) {
            return;
        }
        if (e.getEntitlementCode().getIsUsageBilling()) {
            EntitlementCode entitlementCode = e.getEntitlementCode();
            if (entitlementCode != null) {
                boolean isAnnualSubscription = entitlementCode.getIsUsageBilling() && BillingFrequencyType.Annually.equals(entitlementCode.getBillingFrequencyType());
                EventEmailTemplateTypeCode template = isAnnualSubscription ? EventEmailTemplateTypeCode.SymphonyBillingDetailsAnnual : EventEmailTemplateTypeCode.SymphonyBillingDetailsMonthly;
                String cbaLastFour = EmailUtils.getCbaLastFour(getCompanyEvent().getCompany());
                setCommonEmailParams(template, ContactRole.PayrollAdmin);
                addEmailParameter(EventEmailParamTypeCode.CustomerAccountNnumber, e.getCustomerId());
                addEmailParameter(EventEmailParamTypeCode.SubTypeDescription, EmailUtils.getSubTypeDescription(e.getEntitlementCode()));
                addEmailParameter(EventEmailParamTypeCode.SubscriptionStartDate, EmailUtils.formatDate(e.getSubscriptionStartDate()));
                if (cbaLastFour != null && !StringUtils.isEmpty(cbaLastFour)) {
                    addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, cbaLastFour);
                } else {
                    addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, "NOT ON FILE");
                }
            }
        }
    }

    protected void createUsageBillingMidTrial() {
        EntitlementUnit eu = EmailUtils.getEntitlementUnit(getCompanyEvent());
        Entitlement e = eu.getEntitlement();
         /* check for SymphonyWelcomeOneMonthReactivation product */
        if (checkForSymphonyOneMonthReactivation(e)) {
            return;
        }
        if (e.getEntitlementCode().getIsUsageBilling()) {
            setCommonEmailParams(EventEmailTemplateTypeCode.UsageBillingMidTrial, ContactRole.PayrollAdmin);
        }
    }

    protected void createCustomerSignedUpEmail() {
        CompanyService companyService = EmailUtils.getCompanyService(getCompanyEvent());
        if (companyService == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "No company service could be found."));
        }

        switch (companyService.getService().getServiceCd()) {
            case BillPayment:
                //always send email for PA (on 'this')
                setCommonEmailParams(EventEmailTemplateTypeCode.VendorPaymentSignupConfirmation, ContactRole.PayrollAdmin);

                // Create a list of e-mail addresses so that we do not duplicate.
                Company company = getCompanyEvent().getCompany();
                String payrollAdminEmail = company.getContactByRoleCode(ContactRole.PayrollAdmin).getEmail();
                List<String> alreadySent = new ArrayList<String>();
                alreadySent.add(payrollAdminEmail);

                //send to PP if there is one (on new email)
                Contact primaryPrincipal = company.getContactByRoleCode(ContactRole.PrimaryPrincipal);
                if (primaryPrincipal != null && primaryPrincipal.getEmail() != null && !alreadySent.contains(primaryPrincipal.getEmail())) {
                    createEmailForEvent(getCompanyEvent(), primaryPrincipal);
                    alreadySent.add(primaryPrincipal.getEmail());
                }

                //send to any SPs if there are any (on new emails)
                for (Contact secondaryPrincipal : company.getContactsByRoleCode(ContactRole.SecondaryPrincipal)) {
                    if (secondaryPrincipal.getEmail() != null && !alreadySent.contains(secondaryPrincipal.getEmail())) {
                        createEmailForEvent(getCompanyEvent(), secondaryPrincipal);
                        alreadySent.add(secondaryPrincipal.getEmail());
                    }
                }
                break;
            case DirectDeposit:
                setCommonEmailParams(EventEmailTemplateTypeCode.DDSignupConfirmation, ContactRole.PayrollAdmin);
                break;
            default:
                setCommonEmailParams(EventEmailTemplateTypeCode.DDSignupConfirmation, ContactRole.PayrollAdmin);
                break;
        }
    }
    //
    // The following methods set the email parameters specific to each email template type (beyond the defaults)
    //

    protected void createCustomerSignedUpEmailParams(Contact pContact) {
        CompanyService companyService = EmailUtils.getCompanyService(getCompanyEvent());
        if (companyService == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "No company service could be found."));
        }
        ContactRole contactRole = pContact.getContactRoleCd();
        switch (companyService.getService().getServiceCd()) {
            case BillPayment:
                setCommonEmailParams(EventEmailTemplateTypeCode.VendorPaymentSignupConfirmation, contactRole, pContact.getEmail(), false);
                break;
            case DirectDeposit:
                setCommonEmailParams(EventEmailTemplateTypeCode.DDSignupConfirmation, contactRole);
                break;
            default:
                setCommonEmailParams(EventEmailTemplateTypeCode.DDSignupConfirmation, contactRole);
                break;

        }

    }

    protected void createPINCreatedEmailParams() {
        // Find all active BankAccountVerified events for this company
        // We want the latest verification event with a valid company bank account
        // (need to run our own query here since we need a different sort order than the BE/API provides.)
        Expression<CompanyEvent> query =
                new Query<CompanyEvent>()
                        .Where(CompanyEvent.Company().equalTo(getCompanyEvent().getCompany())
                                           .And(CompanyEvent.EventTypeCd().equalTo(EventTypeCode.BankAccountVerified))
                                           .And(CompanyEvent.StatusCd().equalTo(CompanyEventStatus.Active)))
                        .OrderBy(CreatedDate().Descending());

        DomainEntitySet<CompanyEvent> bankAccountVerifiedEvents = Application.find(CompanyEvent.class, query);

        if (bankAccountVerifiedEvents.isEmpty()) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "No active BankAccountVerified event(s) could be found while processing PINCreated event."));
        }

        String cbaLastFour = null;

        // we want the latest verification event with a valid company bank account
        for (CompanyEvent event : bankAccountVerifiedEvents) {
            cbaLastFour = EmailUtils.getCbaLastFour(event);

            // once we find a valid bank account, we're done
            if (cbaLastFour != null) {
                break;
            }
        }

        if (cbaLastFour == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Valid Company bank account could not be found while processing PINCreated event."));
        }

        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, cbaLastFour);

        setCommonEmailParams(EventEmailTemplateTypeCode.DDBankVerificationSuccessful, ContactRole.PayrollAdmin);
    }

    protected void createNonAchPaymentReceivedEmailParams() {
        FinancialTransaction ft = EmailUtils.getFinancialTransaction(getCompanyEvent());

        if (ft == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        PayrollRun payrollRun = ft.getPayrollRun();

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve payroll run from financial transaction."));
        }

        SpcfDecimal nsfFeesDue = EmailUtils.getIntuitHandlingFee(payrollRun);
        SpcfDecimal balanceDue = EmailUtils.getCompanyBalanceDue(ft.getCompany());

        balanceDue = balanceDue.add(nsfFeesDue);

        // Rules:
        //
        // If [Company Balance Due] > 0
        // If [Company Balance Due] <= 0 & [Strikes] >= 4
        // If [Company Balance Due] <= 0

        EventEmailTemplateTypeCode templateType;
        int strikeCount = EmailUtils.getCompanyStrikeCount(ft.getCompany());
        if (balanceDue.compareTo(SpcfDecimal.createInstance("0.00")) == 1) { // if client owes Intuit money
            templateType = EventEmailTemplateTypeCode.NonACHPMTReceivedLiabOutstanding1;
            addEmailParameter(EventEmailParamTypeCode.CompanyBalanceDue, EmailUtils.formatMoney(balanceDue.abs()));
        } else if (strikeCount >= 4) { // check strike count
            templateType = EventEmailTemplateTypeCode.NonACHPaymentReceivedInFullActionRequired;
            addEmailParameter(EventEmailParamTypeCode.NumberOfStrikes, Integer.toString(strikeCount));
            addEmailParameter(EventEmailParamTypeCode.NextBusinessDate, EmailUtils.formatDateAddBusinessDays(getCompanyEvent().getEventTimeStamp(), 1));
        } else {
            templateType = EventEmailTemplateTypeCode.NonACHPaymentReceivedInFull1;
        }

        setCommonEmailParams(templateType, ContactRole.PayrollAdmin);
    }

    protected void createPayrollReceivedEmailParams() {
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_MTL_EMAIL_DIY_ENABLED,false)){
            CompanyService companyService = getCompanyEvent().getCompany().getCompanyService(ServiceCode.DirectDeposit);
            if (companyService == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                        "Company Service not found"));
            }
            logger.info("Sending mail via MTL params for DIY: ");
            createMTLCompliancePayrollReceivedEmailParams(EmailUtils.getServiceTypeDescription(companyService));
            return;
        }
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollRunId is missing or invalid."));
        }

        // look for created debit txn
        DomainEntitySet<FinancialTransaction> txnList =
                FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(
                        payrollRun, TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);

        if (txnList.isEmpty()) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve non-Cancelled employer debit transaction from payroll run."));
        }

        FinancialTransaction ft = txnList.get(0);
        MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();

        if (mmt == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve money movement transaction from financial transaction."));
        }

        SpcfCalendar runDate = payrollRun.getPayrollRunDate().toLocal();

        addEmailParameter(EventEmailParamTypeCode.PayrollRunDate, EmailUtils.formatDate(runDate));
        addEmailParameter(EventEmailParamTypeCode.PayrollRunTime, EmailUtils.formatTime(runDate));
        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, EmailUtils.getCbaLastFour(txnList.get(0)));
        addEmailParameter(EventEmailParamTypeCode.PayrollDebitSettlementDate, EmailUtils.formatDate(mmt.getDueDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.PayrollDebitAmount, EmailUtils.formatMoney(mmt.getMoneyMovementTransactionAmount()));
        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));

        setCommonEmailParams(EventEmailTemplateTypeCode.QBDTPayrollConfirmation, ContactRole.PayrollAdmin);
    }

    protected void createMTLCompliancePayrollReceivedEmailParams(String sServiceType) {
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                    "Required event detail PayrollRunId is missing or invalid."));
        }
        // look for created debit txn
        DomainEntitySet<FinancialTransaction> txnList =
                FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(
                        payrollRun, TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);

        // retrieve fee charged wrt a payroll run
        DomainEntitySet<FinancialTransaction> feeTxnList =
                FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(
                        payrollRun, TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Cancelled);

        // retrieve taxes charged wrt a payroll run
        DomainEntitySet<FinancialTransaction> salesTaxTxnList =
                FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(
                        payrollRun, TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Cancelled);

        if (txnList.isEmpty()) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                    "Unable to retrieve non-Cancelled employer debit transaction from payroll run."));
        }

        FinancialTransaction ft = txnList.get(0);
        MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();

        if (mmt == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                    "Unable to retrieve money movement transaction from financial transaction."));
        }
        SpcfDecimal payrollDebitAmount = SpcfDecimal.createInstance("0.00");
        SpcfDecimal mmtAmount = mmt.getMoneyMovementTransactionAmount();
        SpcfDecimal tax = getFinancialTransactionAmountSum(salesTaxTxnList);
        SpcfDecimal fee = getFinancialTransactionAmountSum(feeTxnList);
        payrollDebitAmount = payrollDebitAmount.add(mmtAmount).subtract(tax).subtract(fee);
        String transactionNumber = mmt.getTransactionNumber();


        SpcfCalendar runDate = payrollRun.getPayrollRunDate().toLocal();
        // Taxes Charged to be sent in email as part of MTL Compliance
        addEmailParameter(EventEmailParamTypeCode.SalesTaxAmount,
                EmailUtils.formatMoney(tax));
        // Fee Charged to be sent in email as part of MTL Compliance
        addEmailParameter(EventEmailParamTypeCode.IntuitHandlingFee,
                EmailUtils.formatMoney(fee));

        // get list of employees for a payroll run to be sent in email as part of MTL Compliance
        List<String> employees = getDDEmployeeList(payrollRun);
        for(String employee : employees){
            addEmailParameter(EventEmailParamTypeCode.EmployeeList, employee);
        }

        //trace number to be sent in email as part of MTL Compliance
        addEmailParameter(EventEmailParamTypeCode.TransactionNumber, getTransactionIdPrefixWithZero(transactionNumber));
        addEmailParameter(EventEmailParamTypeCode.PayrollRunDate, EmailUtils.formatDate(runDate));
        addEmailParameter(EventEmailParamTypeCode.PayrollRunTime, EmailUtils.formatTime(runDate));
        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, EmailUtils.getCbaLastFour(txnList.get(0)));
        addEmailParameter(EventEmailParamTypeCode.PayrollDebitSettlementDate, EmailUtils.formatDate(mmt.getDueDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.PayrollDebitAmount, EmailUtils.formatMoney(payrollDebitAmount));
        addEmailParameter(EventEmailParamTypeCode.PaymentAmount, EmailUtils.formatMoney(mmtAmount));
        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.ServiceType, sServiceType);


        setCommonEmailParams(EventEmailTemplateTypeCode.QBDTPayrollConfirmationMTL, ContactRole.PayrollAdmin);
    }

    protected void createBillPaymentReceivedParams() {
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_MTL_EMAIL_VENDOR_ENABLED,false)){
            logger.info("Sending mail via MTL params for Vendor: ");
            createMTLComplianceBillPaymentReceivedParams();
            return;
        }
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollRunId is missing or invalid."));
        }

        // look for created debit txn
        DomainEntitySet<FinancialTransaction> txnList =
                FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(
                        payrollRun, TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);

        if (txnList.isEmpty()) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve non-Cancelled employer debit transaction from payroll run."));
        }

        FinancialTransaction ft = txnList.get(0);
        MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();

        if (mmt == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve money movement transaction from financial transaction."));
        }

        Criterion<BillPayment> where = BillPayment.Status().equalTo(BillPaymentStatusCode.Active);
        DomainEntitySet<BillPayment> activeBillPayments = payrollRun.getBillPaymentCollection().find(where);


        StringBuilder vendorPaymentList = new StringBuilder();
        for (BillPayment bp : activeBillPayments) {

            vendorPaymentList.append(bp.getPayee().getName())
                             .append(" for ")
                             .append(bp.getAmount())
                             .append("<br/>");
            if (vendorPaymentList.length() > 3500) {
                addEmailParameter(EventEmailParamTypeCode.VendorPaymentList, vendorPaymentList.toString());
                vendorPaymentList = new StringBuilder();
            }
        }

        SpcfCalendar runDate = payrollRun.getPayrollRunDate().toLocal();
        addEmailParameter(EventEmailParamTypeCode.PayrollRunDate, EmailUtils.formatDate(runDate));
        addEmailParameter(EventEmailParamTypeCode.PayrollRunTime, EmailUtils.formatTime(runDate));
        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, EmailUtils.getCbaLastFour(ft));
        addEmailParameter(EventEmailParamTypeCode.PayrollDebitSettlementDate, EmailUtils.formatDate(mmt.getDueDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.VendorPaymentList, vendorPaymentList.toString());

        setCommonEmailParams(EventEmailTemplateTypeCode.VendorPaymentReceived1, ContactRole.PayrollAdmin);
    }

    protected void createMTLComplianceBillPaymentReceivedParams() {
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                    "Required event detail PayrollRunId is missing or invalid."));
        }

        // look for created debit txn
        DomainEntitySet<FinancialTransaction> txnList =
                FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(
                        payrollRun, TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Cancelled);
        //
        DomainEntitySet<FinancialTransaction> vendortxnList =
                FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(
                        payrollRun, TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
        // Get the Fee List
        DomainEntitySet<FinancialTransaction> payrollFeeList =
                FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(
                        payrollRun, TransactionTypeCode.EmployerFeeDebit, TransactionStateCode.Cancelled);
        // Get the Taxes List
        DomainEntitySet<FinancialTransaction> payrollTaxList =
                FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(
                        payrollRun, TransactionTypeCode.ServiceSalesAndUseTax, TransactionStateCode.Cancelled);

        if (txnList.isEmpty()) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                    "Unable to retrieve non-Cancelled employer debit transaction from payroll run."));
        }

        FinancialTransaction ft = txnList.get(0);
        MoneyMovementTransaction mmt = ft.getMoneyMovementTransaction();

        if (mmt == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                    "Unable to retrieve money movement transaction from financial transaction."));
        }

        // get list vendors to be added as param value
        List<String> vendors = getVendorListWithDetails(txnList, vendortxnList);
        for(String vendor : vendors){
            addEmailParameter(EventEmailParamTypeCode.VendorPaymentList, vendor);
        }

        // to sum up the amount+tax+fee
        SpcfDecimal totalAmount = SpcfDecimal.createInstance("0.00");
        SpcfDecimal totalVendorPayment = getFinancialTransactionAmountSum(txnList);
        SpcfDecimal taxes = getFinancialTransactionAmountSum(payrollTaxList);
        SpcfDecimal fee = getFinancialTransactionAmountSum(payrollFeeList);
        totalAmount = totalAmount.add(totalVendorPayment).add(taxes).add(fee);

        SpcfCalendar runDate = payrollRun.getPayrollRunDate().toLocal();
        // Taxes Charged to be sent in email as part of MTL Compliance
        addEmailParameter(EventEmailParamTypeCode.SalesTaxAmount,
                EmailUtils.formatMoney(taxes));
        // Fee Charged to be sent in email as part of MTL Compliance
        addEmailParameter(EventEmailParamTypeCode.IntuitHandlingFee,
                EmailUtils.formatMoney(fee));
        //trace number to be sent in email as part of MTL Compliance
        //addEmailParameter(EventEmailParamTypeCode.TransactionNumber, getTransactionIdPrefixWithZero(mmt));
        addEmailParameter(EventEmailParamTypeCode.PayrollRunDate, EmailUtils.formatDate(runDate));
        addEmailParameter(EventEmailParamTypeCode.PayrollRunTime, EmailUtils.formatTime(runDate));
        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, EmailUtils.getCbaLastFour(ft));
        addEmailParameter(EventEmailParamTypeCode.PayrollDebitSettlementDate, EmailUtils.formatDate(mmt.getDueDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        // Total DD Vendor amount to be sent in email as part of MTL Compliance
        addEmailParameter(EventEmailParamTypeCode.PayrollDebitAmount, EmailUtils.formatMoney(totalVendorPayment));
        addEmailParameter(EventEmailParamTypeCode.PaymentAmount, EmailUtils.formatMoney(totalAmount));

        setCommonEmailParams(EventEmailTemplateTypeCode.VendorPaymentReceived1MTL, ContactRole.PayrollAdmin);
    }

    protected void createAssistedEnrollmentFailedEmailParams() {
        setCommonEmailParams(EventEmailTemplateTypeCode.AssistedFailedEnrollment, ContactRole.PayrollAdmin);
    }

    protected void createCreditReductionEmailParams() {
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        DomainEntitySet<FinancialTransaction> taxTransactions = payrollRun.getFinancialTransactions(TransactionTypeCode.AgencyTaxCredit, TransactionTypeCode.AgencyTaxDebit);
        Law law = taxTransactions.getFirst().getLaw();
        MoneyMovementTransaction createdPayment = taxTransactions.getFirst().getMoneyMovementTransaction();

        FinancialTransaction debitTransaction = payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxDebit).getFirst(); //no email if no debit

        addEmailParameter(EventEmailParamTypeCode.Quarter, Integer.toString(CalendarUtils.getQuarterAsInt(createdPayment.getPaymentPeriodEnd())));
        addEmailParameter(EventEmailParamTypeCode.Year, Integer.toString(createdPayment.getPaymentPeriodEnd().getYear()));
        addEmailParameter(EventEmailParamTypeCode.LawId, CompanyLaw.findCompanyLaw(payrollRun.getCompany(), law.getLawId()).getSourceDescription());
        addEmailParameter(EventEmailParamTypeCode.Amount, debitTransaction.getFinancialTransactionAmount().toString());
        addEmailParameter(EventEmailParamTypeCode.DebitSettlementDate, EmailUtils.formatDate(debitTransaction.getSettlementDate()));


        EventEmailTemplateTypeCode eventTemplateCode;

        if (law.getPaymentTemplate().isIRS940()) {
            eventTemplateCode = EventEmailTemplateTypeCode.FUTACreditReduction;
        } else if (law.getPaymentTemplate().getPaymentTemplateCd().equals("MA-1700HI-PAYMENT")) {
            eventTemplateCode = EventEmailTemplateTypeCode.SameDayMAUHIDebitNotification;
        } else if (law.getPaymentTemplate().getPaymentTemplateCd().equals("MO-MODES-PAYMENT")) {
            eventTemplateCode = EventEmailTemplateTypeCode.SameDayMoFedAssessmentDebit;
        } else if (law.isAdditionalMedicare()) {
            eventTemplateCode = EventEmailTemplateTypeCode.AdditionalMedicareTaxDebitNotification;
        } else if (law.getPaymentTemplate().getPaymentTemplateCd().equals("NV-NUCS4072-PAYMENT")) {
            eventTemplateCode = EventEmailTemplateTypeCode.SameDayNVBondDebitNotification;
        } else if (law.getPaymentTemplate().getPaymentTemplateCd().equals("NY-MTA305-PAYMENT")) {
            eventTemplateCode = EventEmailTemplateTypeCode.BulkCreditDebitNotificationSUPNY;
        } else {
            eventTemplateCode = EventEmailTemplateTypeCode.BulkCreditDebitNotification;
        }
        setCommonEmailParams(eventTemplateCode, ContactRole.PayrollAdmin);
    }

    protected void createPendingRefundEmailParams() {
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());
        String memo = EmailUtils.getDetailString(getCompanyEvent(), EventDetailTypeCode.NoteText);
        MoneyMovementTransaction mmt = EmailUtils.getMoneyMovementTransaction(getCompanyEvent());
        DomainEntitySet<FinancialTransaction> employerTaxCredit = payrollRun.getFinancialTransactions(TransactionTypeCode.EmployerTaxCredit);

        addEmailParameter(EventEmailParamTypeCode.Quarter, Integer.toString(CalendarUtils.getQuarterAsInt(mmt.getPaymentPeriodEnd())));
        addEmailParameter(EventEmailParamTypeCode.Year, Integer.toString(mmt.getPaymentPeriodEnd().getYear()));
        addEmailParameter(EventEmailParamTypeCode.Amount, mmt.getMoneyMovementTransactionAmount().toString());
        addEmailParameter(EventEmailParamTypeCode.DebitSettlementDate, EmailUtils.formatDate(employerTaxCredit.getFirst().getSettlementDate()));
        addEmailParameter(EventEmailParamTypeCode.Memo, memo);
        addEmailParameter(EventEmailParamTypeCode.TodaysDate, EmailUtils.formatDate(PSPDate.getPSPTime()));

        EventEmailTemplateTypeCode eventTemplateCode = EventEmailTemplateTypeCode.RefundPaymentNotification;

        setCommonEmailParams(eventTemplateCode, ContactRole.PayrollAdmin);
    }


    protected void createAssistedPayrollConfirmationEmailParams() {
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_MTL_EMAIL_ASSISTED_ENABLED,false)){
            CompanyService companyService = getCompanyEvent().getCompany().getCompanyService(ServiceCode.Tax);
            if (companyService == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                        "Company Service not found"));
            }
            logger.info("Sending mail via MTL params for Assisted: ");
            createMTLCompliancePayrollReceivedEmailParams(EmailUtils.getServiceTypeDescription(companyService));
            return;
        }
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());
        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollRunId is missing or invalid."));
        }

        addEmailParameter(EventEmailParamTypeCode.PayrollRunDate, EmailUtils.formatDate(payrollRun.getPayrollRunDate()));
        addEmailParameter(EventEmailParamTypeCode.PayrollRunTime, EmailUtils.formatTime(payrollRun.getPayrollRunDate()));

        setCommonEmailParams(EventEmailTemplateTypeCode.AssistedPayrollConfirmation, ContactRole.PayrollAdmin);
    }

    protected void createServiceKeyUpdatedEmailParams() {

        Entitlement entitlement = EmailUtils.getEntitlement(getCompanyEvent());

        if (entitlement == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail Entitlement Id is missing or invalid."));
        }

        addEmailParameter(EventEmailParamTypeCode.AgreementNumber, entitlement.getSubscriptionNumber());

        // PSRV002773 - 10.1:  Disk Delivery Key Email combines Service Key and Extension Key together
        // NewStringValue contains both the Service Key and Disk Delivery Key (where applicable), separated by a space.
        // We need to split this into the separate key values and populate the individual email params accordingly.
        String newKey = EmailUtils.getDetailString(getCompanyEvent(), EventDetailTypeCode.NewStringValue);
        String[] keys = newKey.split(" "); // keys[0] == Service Key, keys[1] == Disk Delivery Key (if applicable)

        addEmailParameter(EventEmailParamTypeCode.ServiceKey, keys[0]); // Service Key

        // Define which email template to use based on the entitlement code

        EventEmailTemplateTypeCode emailTemplate = null;
        EntitlementCode entitlementCode = entitlement.getEntitlementCode();
        switch (entitlementCode.getAssetItemCd()) {
            case DIYDiskDelivery:
                if (keys.length > 1) {
                    addEmailParameter(EventEmailParamTypeCode.ExtensionKey, keys[1]); // Disk Delivery Key (aka Extension Key)
                } else {
                    addEmailParameter(EventEmailParamTypeCode.ExtensionKey, "N/A");
                }
                emailTemplate = EventEmailTemplateTypeCode.SKDiskDeliveryKey1;
                break;
            case DIY:
                if (entitlement.hasDummyEntitlementCode()) {
                    emailTemplate = EventEmailTemplateTypeCode.SKDefaultKey1;
                    break;
                }

                switch (entitlementCode.getEditionType()) {
                    case Basic:
                        if (entitlementCode.getNumberOfEmployeesType() != null && entitlementCode.getNumberOfEmployeesType().equals(NumberOfEmployeesType.ONE)) {
                            emailTemplate = EventEmailTemplateTypeCode.SKFreeBasicKey1;
                        } else {
                            emailTemplate = EventEmailTemplateTypeCode.SKBasicKey1;
                        }
                        break;
                    case Standard:
                        emailTemplate = EventEmailTemplateTypeCode.SKStandardKey1;
                        break;
                    case Enhanced:
                        emailTemplate = EventEmailTemplateTypeCode.SKEnhancedKey1;
                        break;
                    case EnhancedAccountant:
                    case EnhancedAccountantProAdvisor:
                        emailTemplate = EventEmailTemplateTypeCode.SKEnhancedKeyAccount1;
                        break;
                    default:
                        emailTemplate = EventEmailTemplateTypeCode.SKDefaultKey1;
                        break;
                }
        }

        setCommonEmailParams(emailTemplate, ContactRole.PayrollAdmin);
    }


    protected void createFeeCreatedEmailParams() {
        DomainEntitySet<FinancialTransaction> fts = EmailUtils.getFinancialTransactions(getCompanyEvent());

        if (fts.isEmpty()) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        String cbaLastFour = null;
        String settlementDate = null;
        StringBuilder billed = new StringBuilder();
        for (FinancialTransaction ft : fts) {
            if (cbaLastFour == null) {
                cbaLastFour = EmailUtils.getCbaLastFour(ft);
            }

            if (settlementDate == null) {
                settlementDate = EmailUtils.formatDate(ft.getSettlementDate().toLocal());
            }

            String feeDescription = EmailUtils.getFeeDescription(ft);
            if (feeDescription == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Unable to retrieve fee description from financial transaction."));
            }

            String amount = EmailUtils.formatMoney(ft.getFinancialTransactionAmount());
            billed.append(String.format("Billed: %s for %s on %s <br>", amount, feeDescription, settlementDate));
        }

        if (billed.length() > 0) {
            addEmailParameter(EventEmailParamTypeCode.BilledFeeList, billed.toString());
        }
        if (cbaLastFour != null) {
            addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, cbaLastFour);
        }
        if (settlementDate != null) {
            addEmailParameter(EventEmailParamTypeCode.NonPayrollFeeSettlementDate, settlementDate);
        }

        setCommonEmailParams(EventEmailTemplateTypeCode.BilledNonPayrollRelatedFee2, ContactRole.PayrollAdmin);
    }

    protected void createFeeRebilledEmailParams() {
        // Required Format: "Billed: [ChargeAmount] for [ChargeType] on [BilledSettlementDate]"
        // Required Format: "Refunded: [RefundAmount] for [ChargeType] on [RefundSettlementDate]"

        BillingDetail feeDetail = EmailUtils.getFeeBillingDetail(getCompanyEvent());

        if (feeDetail == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FeeBillingDetailId is missing or invalid."));
        }

        BillingDetail refundDetail = EmailUtils.getRefundBillingDetail(getCompanyEvent());

        if (refundDetail == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail RefundedFeeBillingDetailId is missing or invalid."));
        }

        FinancialTransaction feeTxn = feeDetail.getFinancialTransactionCollection().iterator().next();

        if (feeTxn == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to locate fee transaction for FeeRebilled event."));
        }

        String cbaLastFour = EmailUtils.getCbaLastFour(feeTxn);

        if (cbaLastFour == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail CompanyBankAccountLastFour is missing or invalid."));
        }

        // the settlement date is the same for both the refund and rebill txns
        String settlementDate = EmailUtils.formatDate(feeTxn.getSettlementDate().toLocal());
        String refundAmount = EmailUtils.getDetailString(getCompanyEvent(), EventDetailTypeCode.RefundAmount);
        StringBuilder billed = new StringBuilder();
        StringBuilder refunded = new StringBuilder();

        billed.append("Billed: ")
              .append(EmailUtils.formatMoney(feeDetail.getItemTotal()))
              .append(" for ")
              .append(feeDetail.getItemName())
              .append(" will occur on ")
              .append(settlementDate)
              .append("<br>");

        refunded.append("Refunded: ")
                .append(EmailUtils.formatMoney(SpcfDecimal.createInstance(refundAmount)))
                .append(" for ")
                .append(refundDetail.getItemName())
                .append(" will occur on ")
                .append(settlementDate)
                .append("<br>");

        addEmailParameter(EventEmailParamTypeCode.BilledFeeList, billed.toString());
        addEmailParameter(EventEmailParamTypeCode.RefundedFeeList, refunded.toString());
        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, cbaLastFour);

        setCommonEmailParams(EventEmailTemplateTypeCode.RefundWithRebillFeeAmount1, ContactRole.PayrollAdmin);
    }

    protected void createFeeRefundedEmailParams() {
        // Required Format: "Refunded: [RefundAmount] for [ChargeType] on [RefundSettlementDate]"

        BillingDetail refundDetail = EmailUtils.getRefundBillingDetail(getCompanyEvent());

        FinancialTransaction ft = EmailUtils.getFinancialTransaction(getCompanyEvent());

        if (ft == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        String cbaLastFour = EmailUtils.getCbaLastFour(ft);

        if (cbaLastFour == null) {
            cbaLastFour = "";
        }

        StringBuilder refunded = new StringBuilder();

        refunded.append("Refunded: ")
                .append(EmailUtils.formatMoney(ft.getFinancialTransactionAmount()))
                .append(" for ")
                .append(refundDetail != null ? refundDetail.getItemName() : "courtesy refund")
                .append(" on ")
                .append(EmailUtils.formatDate(ft.getSettlementDate().toLocal()))
                .append("<br>");

        addEmailParameter(EventEmailParamTypeCode.RefundedFeeList, refunded.toString());
        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, cbaLastFour);

        setCommonEmailParams(EventEmailTemplateTypeCode.RefundedFeeAmount1, ContactRole.PayrollAdmin);
    }

    protected void createReversalOKEmailParams() {
        // Format: "[EmployeeFirstName] [EmployeeLastNameFirstInitial]'s direct deposit in the amount [NetPaycheckAmount] was successfully credited back to your bank account on [ReversalSuccessDate]"

        // retrieve the employee reversal debit transaction
        FinancialTransaction rvsTxn = EmailUtils.getFinancialTransaction(getCompanyEvent());

        if (rvsTxn == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        // retrieve the employer reversal refund transaction
        FinancialTransaction refundTxn = EmailUtils.retrieveCreditForReversal(rvsTxn);

        if (refundTxn == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve employer refund transaction from reversal transaction."));
        }

        // retrieve the original employee credit transaction
        FinancialTransaction origTxn = rvsTxn.getOriginalTransaction();

        if (origTxn == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve original employee credit transaction from reversal transaction."));
        }

        PaycheckSplit paycheckSplit = origTxn.getPaycheckSplit();
        BillPaymentSplit billPaymentSplit = origTxn.getBillPaymentSplit();

        if (paycheckSplit == null && billPaymentSplit == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve paycheck or billPayment split from reversal transaction."));
        }

        Paycheck paycheck = null;
        BillPayment billPayment = null;
        if (paycheckSplit != null) {
            paycheck = paycheckSplit.getPaycheck();
        } else if (billPaymentSplit != null) {
            billPayment = billPaymentSplit.getBillPayment();
        }

        if (paycheck == null && billPayment == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve paycheck or bill payment from reversal transaction."));
        }

        Employee employee = null;
        Payee payee = null;
        if (paycheck != null) {
            employee = paycheck.getDDEmployee();
        } else if (billPayment != null) {
            payee = billPayment.getPayee();
        }

        if (employee == null && payee == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve employee or payee from reversal transaction."));
        }

        StringBuilder msg = new StringBuilder();
        if (employee != null) {
            msg.append(employee.getFirstName());
            msg.append(" ");
            msg.append(employee.getLastName().substring(0, 1));
        } else if (payee != null) {
            msg.append(payee.getName());
        }
        msg.append("&#8217;s direct deposit in the amount ");
        msg.append(EmailUtils.formatMoney(origTxn.getFinancialTransactionAmount()));
        msg.append(" was successfully reversed on ");
        msg.append(EmailUtils.formatDate(rvsTxn.getSettlementDate().toLocal()));
        msg.append("<br>");

        addEmailParameter(EventEmailParamTypeCode.ReversalSuccessfulList, msg.toString());
        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, EmailUtils.getCbaLastFour(refundTxn));
        addEmailParameter(EventEmailParamTypeCode.EffectiveCreditPostingDate, EmailUtils.formatDate(refundTxn.getSettlementDate().toLocal()));

        // Workaround: Some email templates are parameterized with property names bound to PrimaryPrincipal,
        //  however QBOE companies don't have a PrimaryPrincipal contact. In this case, we need to
        //  pull the contact information from the PayrollAdmin contact info and populate the email
        //  template using the PrimaryPrincipal parameters.
        SourceSystemCode ssc = getCompanyEvent().getCompany().getSourceSystemCd();

        // setting the template type to AllPaycheckReversalsSuccessful is only temporary here
        // (multiple reversal ok/return events are grouped into the same email for this payroll
        // run and their actual template type will be determined by the email gateway)
        setCommonEmailParams(
                EventEmailTemplateTypeCode.AllPaycheckReversalsSuccessful1,
                (ssc == SourceSystemCode.QBDT) ? ContactRole.PrimaryPrincipal : ContactRole.PayrollAdmin,
                ContactRole.PrimaryPrincipal);
    }

    protected void createReversalReturnEmailParams() {
        // Format: "[EmployeeFirstName] [EmployeeLastNameFirstInitial]'s direct deposit in the amount [NetPaycheckAmount] could not be reversed because [Failure Reason]"

        // retrieve the employee reversal debit transaction
        FinancialTransaction rvsTxn = EmailUtils.getFinancialTransaction(getCompanyEvent());

        if (rvsTxn == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        // retrieve the original employee credit transaction
        FinancialTransaction origTxn = rvsTxn.getOriginalTransaction(); // get the original employee transaction

        if (origTxn == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve original employee credit transaction from reversal transaction."));
        }

        PaycheckSplit paycheckSplit = origTxn.getPaycheckSplit();
        BillPaymentSplit billPaymentSplit = origTxn.getBillPaymentSplit();

        if (paycheckSplit == null && billPaymentSplit == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve paycheck or billPayment split from reversal transaction."));
        }
        Paycheck paycheck = null;
        BillPayment billPayment = null;
        if (paycheckSplit != null) {
            paycheck = paycheckSplit.getPaycheck();
        }

        if (billPaymentSplit != null) {
            billPayment = billPaymentSplit.getBillPayment();
        }

        if (paycheck == null && billPayment == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve paycheck or bill payment from reversal transaction."));
        }

        Employee employee = null;
        Payee payee = null;
        if (paycheck != null) {
            employee = paycheck.getDDEmployee();
        }
        if (billPayment != null) {
            payee = billPayment.getPayee();
        }

        if (employee == null && payee == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve employee or payee from reversal transaction."));
        }

        StringBuilder msg = new StringBuilder();
        if (employee != null) {
            msg.append(employee.getFirstName());
            msg.append(" ");
            msg.append(employee.getLastName().substring(0, 1));
        }
        if (payee != null) {
            msg.append(payee.getName());
        }
        msg.append("&#8217;s direct deposit in the amount ");
        msg.append(EmailUtils.formatMoney(origTxn.getFinancialTransactionAmount()));
        msg.append(" could not be reversed because ");
        msg.append(EmailUtils.getReadableReturnFailureReason(getCompanyEvent()));
        msg.append("<br>");

        addEmailParameter(EventEmailParamTypeCode.ReversalFailedList, msg.toString());

        // Workaround: Some email templates are parameterized with property names bound to PrimaryPrincipal,
        //  however QBOE companies don't have a PrimaryPrincipal contact. In this case, we need to
        //  pull the contact information from the PayrollAdmin contact info and populate the email
        //  template using the PrimaryPrincipal parameters.
        SourceSystemCode ssc = getCompanyEvent().getCompany().getSourceSystemCd();

        // setting the template type to AllPaycheckReversalsFailed is only temporary here
        // (multiple reversal ok/return events are grouped into the same email for this payroll
        // run and their actual template type will be determined by the email gateway)
        setCommonEmailParams(
                EventEmailTemplateTypeCode.AllPaycheckReversalsFailed1,
                (ssc == SourceSystemCode.QBDT) ? ContactRole.PrimaryPrincipal : ContactRole.PayrollAdmin,
                ContactRole.PrimaryPrincipal);
    }


    protected void createReversalRequestedEmailParams() {
        // Required Format: "[EmployeeFirstName] [EmployeeLastNameFirstInitial]'s direct deposit in the amount [NetPaycheckAmount] will be reversed"

        FinancialTransaction reversalTxn = EmailUtils.getFinancialTransaction(getCompanyEvent());

        if (reversalTxn == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        // get the original employee transaction
        FinancialTransaction eeCreditTxn = reversalTxn.getOriginalTransaction();

        if (eeCreditTxn == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve original employee credit transaction from reversal transaction."));
        }
        StringBuilder reversed = new StringBuilder();
        SpcfDecimal amount;
        PayrollRun payrollRun = eeCreditTxn.getPayrollRun();
        if (payrollRun.getPayrollRunType().equals(PayrollType.Regular)) {
            PaycheckSplit paycheckSplit = eeCreditTxn.getPaycheckSplit();

            if (paycheckSplit == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Unable to retrieve paycheck split from reversal transaction."));
            }

            Paycheck paycheck = paycheckSplit.getPaycheck();

            if (paycheck == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Unable to retrieve paycheck from reversal transaction."));
            }

            Employee employee = paycheck.getDDEmployee();

            if (employee == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Unable to retrieve employee from reversal transaction."));
            }
            reversed.append(employee.getFirstName())
                    .append(" ")
                    .append(employee.getLastName().substring(0, 1));
            amount = eeCreditTxn.getFinancialTransactionAmount();

        } else {
            BillPaymentSplit billPaymentSplit = eeCreditTxn.getBillPaymentSplit();

            if (billPaymentSplit == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Unable to retrieve bill payment split from reversal transaction."));
            }

            BillPayment billPayment = billPaymentSplit.getBillPayment();

            if (billPayment == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Unable to retrieve bill payment from reversal transaction."));
            }

            Payee payee = billPayment.getPayee();

            if (payee == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Unable to retrieve payee from reversal transaction."));
            }
            amount = eeCreditTxn.getFinancialTransactionAmount();
        }

        reversed.append("&#8217;s direct deposit in the amount ")
                .append(EmailUtils.formatMoney(amount))
                .append(" will be reversed")
                .append("<br>");

        addEmailParameter(EventEmailParamTypeCode.ReversalPendingList, reversed.toString());

        // Workaround: Some email templates are parameterized with property names bound to PrimaryPrincipal,
        //  however QBOE companies don't have a PrimaryPrincipal contact. In this case, we need to
        //  pull the contact information from the PayrollAdmin contact info and populate the email
        //  template using the PrimaryPrincipal parameters.
        SourceSystemCode ssc = getCompanyEvent().getCompany().getSourceSystemCd();

        setCommonEmailParams(
                EventEmailTemplateTypeCode.CustomerInitiatedDDReversal1,
                (ssc == SourceSystemCode.QBDT) ? ContactRole.PrimaryPrincipal : ContactRole.PayrollAdmin,
                ContactRole.PrimaryPrincipal);
    }

    protected void createServiceStatusChangeEmailParams() {
        DomainEntitySet<CompanyEventDetail> newEventDetails = getCompanyEvent().getCompanyEventDetails(EventDetailTypeCode.NewOnHoldReason);
        DomainEntitySet<CompanyEventDetail> oldEventDetails = getCompanyEvent().getCompanyEventDetails(EventDetailTypeCode.OldOnHoldReason);
        boolean amlHoldChangePresent = false;
        for (CompanyEventDetail detail : newEventDetails) {
            if (EnumUtils.getReadableName(ServiceSubStatusCode.AMLHold).equals(detail.getValue())) {
                setCommonEmailParams(EventEmailTemplateTypeCode.DesktopAMLHoldApplied, ContactRole.PrimaryPrincipal);
                amlHoldChangePresent = true;
                break;
            }
        }
        for (CompanyEventDetail detail : oldEventDetails) {
            if (amlHoldChangePresent)
                break;
            if (EnumUtils.getReadableName(ServiceSubStatusCode.AMLHold).equals(detail.getValue())) {
                setCommonEmailParams(EventEmailTemplateTypeCode.DesktopAMLHoldRemoved, ContactRole.PrimaryPrincipal);
                amlHoldChangePresent = true;
                break;
            }
        }
        if (!amlHoldChangePresent) {
            setCommonEmailParams(EventEmailTemplateTypeCode.ServiceCancelledConfirmation1, ContactRole.PrimaryPrincipal);
            CompanyService companyService = EmailUtils.getCompanyService(getCompanyEvent());
            if (companyService == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                        "No company service could be found."));
            }
        }
    }

    protected void createPINUpdatedEmailParams() {
        setCommonEmailParams(EventEmailTemplateTypeCode.DDPINChangeConfirmation1, ContactRole.PayrollAdmin);
    }

    protected void createCompanyBankAccountChangeEmailParams() {
        CompanyService companyService = EmailUtils.getCompanyService(getCompanyEvent());

        if (companyService == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this, "No company service could be found."));
        }

        switch (companyService.getService().getServiceCd()) {
            case Tax:
                setCommonEmailParams(EventEmailTemplateTypeCode.DDERBankAccountChangeAssisted, ContactRole.PayrollAdmin);
                break;
            default:
                setCommonEmailParams(EventEmailTemplateTypeCode.DDERBankAccountChange, ContactRole.PayrollAdmin);
        }
    }

    protected void createEmployeeBankAccountChangeEmailParams(String pRecipientEmail) {
        String oldBankAccountId = EmailUtils.getDetailString(getCompanyEvent(),
                                                             EventDetailTypeCode.OldEmployeeBankAccountId);

        EmployeeBankAccount oldEmployeeBankAccount = null;
        if(oldBankAccountId != null){
        	oldEmployeeBankAccount =
                Application.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(oldBankAccountId));
        }

        String newBankAccountId = EmailUtils.getDetailString(getCompanyEvent(),
                                                             EventDetailTypeCode.NewEmployeeBankAccountId);

        if (newBankAccountId == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve new employee bank account id from EBA change event."));
        }

        EmployeeBankAccount newEmployeeBankAccount =
                Application.findById(EmployeeBankAccount.class, SpcfUniqueId.createInstance(newBankAccountId));

        if (newEmployeeBankAccount == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve new employee bank account from EBA change event."));
        }

        Employee employee = newEmployeeBankAccount.getEmployee();

        if (employee == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve employee from employee bank account."));
        }

        StringBuilder nameString = new StringBuilder();

        String lastName = employee.getLastName() == null ? "" : employee.getLastName().substring(0, 1);

        if(oldEmployeeBankAccount == null){
		   nameString.append("<b>")
					 .append(employee.getFirstName())
					 .append(" ")
					 .append(lastName)
					 .append("</b>")
					 .append("&#8217;s account number (ending in) ")
					 .append("<b>")
					 .append(EmailUtils.getBALastFourDigit(newEmployeeBankAccount.getBankAccount()))
					 .append("</b>")
					 .append(" was added ")
					 .append("<br>");
        } else {
	        nameString.append("<b>")
	                  .append(employee.getFirstName())
	                  .append(" ")
	                  .append(lastName)
	                  .append("</b>")
	                  .append("&#8217;s old account number (ending in) ")
	                  .append("<b>")
	                  .append(EmailUtils.getBALastFourDigit(oldEmployeeBankAccount.getBankAccount()))
	                  .append("</b>")
	                  .append(" was changed to new account number (ending in) ")
	                  .append("<b>")
	                  .append(EmailUtils.getBALastFourDigit(newEmployeeBankAccount.getBankAccount()))
	                  .append("</b>")
	                  .append("<br>");
        }
        addEmailParameter(EventEmailParamTypeCode.EmployeeList, nameString.toString());

        setCommonEmailParams(EventEmailTemplateTypeCode.DDEEBankAccountChange1,
                             ContactRole.PayrollAdmin,
                             pRecipientEmail);
    }

    protected void createPayeeBankAccountChangeEmailParams(String pRecipientEmail) {
        String oldBankAccountNum = EmailUtils.getDetailString(getCompanyEvent(),
                EventDetailTypeCode.OldPayeeBankAccountNumber);

        String newBankAccountNum = EmailUtils.getDetailString(getCompanyEvent(),
                EventDetailTypeCode.NewPayeeBankAccountNumber);

        if (newBankAccountNum == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                    "Unable to retrieve new Payee bank account id from PBA change event."));
        }

        Payee payee = PayeeBankAccount.findPayeeBankAccount(getCompanyEvent().getCompany(), newBankAccountNum).getPayee();

        if (payee == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                    "Unable to retrieve Payee from Payee bank account."));
        }

        StringBuilder nameString = new StringBuilder();

        if(oldBankAccountNum == null){
            nameString.append("<b>")
                    .append(payee.getName())
                    .append("</b>")
                    .append("&#8217;s account number (ending in) ")
                    .append("<b>")
                    .append(EmailUtils.getBALastFourDigit(newBankAccountNum))
                    .append("</b>")
                    .append(" was added ")
                    .append("<br>");
        } else {
            nameString.append("<b>")
                    .append(payee.getName())
                    .append("</b>")
                    .append("&#8217;s old account number (ending in) ")
                    .append("<b>")
                    .append(EmailUtils.getBALastFourDigit(oldBankAccountNum))
                    .append("</b>")
                    .append(" was changed to new account number (ending in) ")
                    .append("<b>")
                    .append(EmailUtils.getBALastFourDigit(newBankAccountNum))
                    .append("</b>")
                    .append("<br>");
        }
        addEmailParameter(com.intuit.sbd.payroll.psp.domain.EventEmailParamTypeCode.PayeeList, nameString.toString());

        setCommonEmailParams(com.intuit.sbd.payroll.psp.domain.EventEmailTemplateTypeCode.DDPayeeBankAccountChange,
                com.intuit.sbd.payroll.psp.domain.ContactRole.PayrollAdmin,
                pRecipientEmail);
    }

    protected void createNOCEmailParams() {
        StringBuilder nameString = new StringBuilder();
        String bankAccountId = EmailUtils.getDetailString(getCompanyEvent(),
                                                          EventDetailTypeCode.CompanyBankAccountId);

        // if this isn't an employer NOC, check for employee or payee bank account info
        if (bankAccountId == null) {
            bankAccountId = EmailUtils.getDetailString(getCompanyEvent(),
                                                       EventDetailTypeCode.EmployeeBankAccountId);

            if (bankAccountId == null) {
                bankAccountId = EmailUtils.getDetailString(getCompanyEvent(),
                                                           EventDetailTypeCode.PayeeBankAccountId);
                if (bankAccountId == null) {
                    throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                         "Unable to retrieve employer,employee or payee bank account from NOC event."));
                }
            }

            EmployeeBankAccount employeeBankAccount =
                    Application.findById(EmployeeBankAccount.class,
                                         SpcfUniqueId.createInstance(bankAccountId));
            String name;

            if (employeeBankAccount == null) {
                PayeeBankAccount payeeBankAccount = Application.findById(PayeeBankAccount.class, SpcfUniqueId.createInstance(bankAccountId));
                if (payeeBankAccount == null) {
                    throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                         "Unable to retrieve employee/payee bank account from NOC event."));
                } else {
                    Payee payee = payeeBankAccount.getPayee();
                    if (payee == null) {
                        throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                             "Unable to retrieve payee from payee bank account."));
                    }
                    name = payee.getName();
                }
            } else {
                Employee employee = employeeBankAccount.getEmployee();

                if (employee == null) {
                    throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                         "Unable to retrieve employee from employee bank account."));
                }
                name = employee.getFirstName() + " " + employee.getLastName().substring(0, 1);
            }


            nameString.append("<b>")
                      .append(name)
                      .append(".")
                      .append("</b>")
                      .append("<br>");
        }

        EventEmailTemplateTypeCode templateType;

        // if this is an employee NOC, add the additional param (employer NOC�s don't have any additional params)  // Todo
        if (nameString.length() == 0) {
            templateType = EventEmailTemplateTypeCode.EmployerNOC1;
        } else {
            CompanyService companyService = EmailUtils.getCompanyService(getCompanyEvent());

            if (companyService != null && companyService.getService().getServiceCd() == ServiceCode.Tax) {
                templateType = EventEmailTemplateTypeCode.EmployeeNOCAssisted;
            } else {
                templateType = EventEmailTemplateTypeCode.EmployeeNOC2;
            }

            addEmailParameter(EventEmailParamTypeCode.EmployeeList, nameString.toString());
        }

        // setting the template type here is only temporary; the email gateway can change this based on other NOC�s.
        // (multiple NOC events are grouped into the same email for this payroll
        // run and their actual template type will be determined by the email gateway)
        setCommonEmailParams(templateType, ContactRole.PayrollAdmin);
    }

    protected void createERLoanNOCEmailParams() {
        StringBuilder nameString = new StringBuilder();
        String bankAccountId = EmailUtils.getDetailString(getCompanyEvent(),
                                                          EventDetailTypeCode.CompanyBankAccountId);

        EventEmailTemplateTypeCode templateType = EventEmailTemplateTypeCode.EmployerNOC52LoanAccount;

        setCommonEmailParams(templateType, ContactRole.PayrollAdmin);
    }

    protected void createCompanyContactEmailChangedEmailParams(String pRecipientEmail) {
        Contact contact = EmailUtils.getContact(getCompanyEvent());

        if (contact == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail ContactId is missing or invalid."));
        }

        String newEmailAddress = EmailUtils.getDetailString(getCompanyEvent(),
                                                            EventDetailTypeCode.NewStringValue);
        String oldEmailAddress = EmailUtils.getDetailString(getCompanyEvent(),
                                                            EventDetailTypeCode.OldStringValue);

        addEmailParameter(EventEmailParamTypeCode.EmailFirstName, contact.getFirstName());
        addEmailParameter(EventEmailParamTypeCode.EmailLastName, contact.getLastName());
        addEmailParameter(EventEmailParamTypeCode.CurrentEmail, newEmailAddress);
        addEmailParameter(EventEmailParamTypeCode.PriorEmail, oldEmailAddress);

        setCommonEmailParams(
                EventEmailTemplateTypeCode.EmailChangeNotification,
                contact,
                contact.getContactRoleCd(),
                pRecipientEmail);
    }

    protected void createCBAVerifyReturnEmailParams() {
        FinancialTransaction ft = EmailUtils.getFinancialTransaction(getCompanyEvent());

        if (ft == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, EmailUtils.getCbaLastFour(ft));

        SourceSystemCode ssc = getCompanyEvent().getCompany().getSourceSystemCd();
        EventEmailTemplateTypeCode templateType;
        ContactRole role;

        if (ssc == SourceSystemCode.QBDT) { // QBDT
            templateType = EventEmailTemplateTypeCode.BankVerificationFailed1;
            role = ContactRole.PayrollAdmin;
        } else { // GEMINI
            templateType = EventEmailTemplateTypeCode.BankVerifyAttemptFailed;
            role = ContactRole.PrimaryPrincipal;
        }

        setCommonEmailParams(templateType, role);
    }

    protected void createNSFAutoRedebitEmailParams() {
        DomainEntitySet<FinancialTransaction> txnList =
                EmailUtils.getFinancialTransactions(getCompanyEvent());

        if (txnList.isEmpty()) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        String returnReason = EmailUtils.getReadableReturnFailureReason(getCompanyEvent());

        if (returnReason == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve return reason description from company event."));
        }

        // Payrollrun, cba, and settlement date will be the same for all included financial transactions
        FinancialTransaction ft = txnList.get(0);
        PayrollRun payrollRun = ft.getPayrollRun();

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve payroll run from financial transaction."));
        }

        // element 0 contains the payroll amount less fees
        // element 1 contains the fees
        SpcfDecimal[] payrollDebitsAndFees = EmailUtils.getPayrollDebitsAndFees(ft.getPayrollRun());
        SpcfCalendar today = getCompanyEvent().getEventTimeStamp();
        int strikeCount = EmailUtils.getCompanyStrikeCount(ft.getCompany());

        // Rules:
        //
        // If [Strikes] >= 4
        // If [Strikes] < 4

        EventEmailTemplateTypeCode templateType;
        if (strikeCount >= 4) { // check strike count
            templateType = EventEmailTemplateTypeCode.AutoRedebitFourStrikes;
            addEmailParameter(EventEmailParamTypeCode.NumberOfStrikes, Integer.toString(strikeCount));
            addEmailParameter(EventEmailParamTypeCode.TodaysDatePlus14CalendarDays, EmailUtils.formatDateAddCalendarDays(today, 14));
        } else {
            templateType = EventEmailTemplateTypeCode.AutoRedebit3;
        }

        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.RedebitCompletedDate, EmailUtils.formatDateAddBusinessDays(today, 5));
        addEmailParameter(EventEmailParamTypeCode.FailureReason, returnReason);
        addEmailParameter(EventEmailParamTypeCode.NextBusinessDate, EmailUtils.formatDateAddBusinessDays(today, 1));
        addEmailParameter(EventEmailParamTypeCode.PayrollDebitAmount, EmailUtils.formatMoney(payrollDebitsAndFees[0]));
        addEmailParameter(EventEmailParamTypeCode.IntuitHandlingFee, EmailUtils.formatMoney(payrollDebitsAndFees[1]));
        addEmailParameter(EventEmailParamTypeCode.AdjustedPayrollDebitAmount, EmailUtils.formatMoney(payrollDebitsAndFees[0].add(payrollDebitsAndFees[1])));
        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, EmailUtils.getCbaLastFour(ft.getCompany()));
        addEmailParameter(EventEmailParamTypeCode.TodaysDate, EmailUtils.formatDate(today));

        setCommonEmailParams(templateType, ContactRole.PayrollAdmin);
    }

    protected void createSecondNSFEmailParams() {
        FinancialTransaction ft = EmailUtils.getFinancialTransaction(getCompanyEvent());

        if (ft == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        PayrollRun payrollRun = ft.getPayrollRun();

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve payroll run from financial transaction."));
        }

        // element 0 contains the payroll amount less fees
        // element 1 contains the fees
        SpcfDecimal[] payrollDebitsAndFees = EmailUtils.getPayrollDebitsAndFees(ft.getPayrollRun());

        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.AdjustedPayrollDebitAmount, EmailUtils.formatMoney(payrollDebitsAndFees[0].add(payrollDebitsAndFees[1])));
        addEmailParameter(EventEmailParamTypeCode.NextBusinessDate, EmailUtils.formatDateAddBusinessDays(getCompanyEvent().getEventTimeStamp(), 1));
        addEmailParameter(EventEmailParamTypeCode.CompanyID, ft.getCompany().getSourceCompanyId());

        setCommonEmailParams(EventEmailTemplateTypeCode.RedebitFailed1, ContactRole.PayrollAdmin);
    }

    protected void createNSFEmailParams() {
        String subTypeString = EmailUtils.getDetailString(getCompanyEvent(),
                                                          EventDetailTypeCode.NSFSubType);

        if (subTypeString == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail NSFSubType is missing or invalid."));
        }

        NSFSubTypeType subType = EnumUtils.getEnumForReadableName(NSFSubTypeType.class, subTypeString);

        switch (subType) {
            case NSFAutoRedebit:
                createNSFAutoRedebitEmailParams();
                break;

            case SecondNSF:
                createSecondNSFEmailParams();
                break;
        }
    }

    protected void createReturnedTwiceEmailParams() {
        // todo: for now, just send the RedebitFailed (SecondNSF) email
        // todo: ask the business which takes precedence (non-nsf email or returned twice email)
        createSecondNSFEmailParams();
    }

    protected void createDDDebitReturnNonNsfEmailParams() {
        FinancialTransaction ft = EmailUtils.getFinancialTransaction(getCompanyEvent());

        if (ft == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        PayrollRun payrollRun = ft.getPayrollRun();

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve payroll run from financial transaction."));
        }

        String returnReason = EmailUtils.getReadableReturnFailureReason(getCompanyEvent());

        if (returnReason == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve return reason description from company event."));
        }

        // element 0 contains the payroll amount less fees
        // element 1 contains the fees
        SpcfDecimal[] payrollDebitsAndFees = EmailUtils.getPayrollDebitsAndFees(ft.getPayrollRun());
        SpcfCalendar today = getCompanyEvent().getEventTimeStamp();
        int strikeCount = EmailUtils.getCompanyStrikeCount(ft.getCompany());

        // Rules:
        //
        // If [Strikes] >= 4
        // If [Strikes] < 4

        EventEmailTemplateTypeCode templateType;
        if (strikeCount >= 4) { // check strike count
            templateType = EventEmailTemplateTypeCode.DebitReturnedFourStrikes3;
            addEmailParameter(EventEmailParamTypeCode.NumberOfStrikes, Integer.toString(strikeCount));
            addEmailParameter(EventEmailParamTypeCode.TodaysDatePlus14CalendarDays, EmailUtils.formatDateAddCalendarDays(today, 14));
        } else {
            templateType = EventEmailTemplateTypeCode.DebitReturned4;
        }

        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.FailureReason, returnReason);
        addEmailParameter(EventEmailParamTypeCode.NextBusinessDate, EmailUtils.formatDateAddBusinessDays(today, 1));
        addEmailParameter(EventEmailParamTypeCode.PayrollDebitAmount, EmailUtils.formatMoney(payrollDebitsAndFees[0]));
        addEmailParameter(EventEmailParamTypeCode.IntuitHandlingFee, EmailUtils.formatMoney(payrollDebitsAndFees[1]));
        addEmailParameter(EventEmailParamTypeCode.AdjustedPayrollDebitAmount, EmailUtils.formatMoney(payrollDebitsAndFees[0].add(payrollDebitsAndFees[1])));
        addEmailParameter(EventEmailParamTypeCode.CompanyID, ft.getCompany().getSourceCompanyId());

        setCommonEmailParams(templateType, ContactRole.PayrollAdmin);
    }

    protected void createDDDebitReturnEmailParams() {
        String payrollStatusString = EmailUtils.getDetailString(getCompanyEvent(),
                                                                EventDetailTypeCode.PayrollStatus);

        if (payrollStatusString == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollStatus is missing or invalid."));
        }

        PayrollStatus payrollStatus = EnumUtils.getEnumForReadableName(PayrollStatus.class, payrollStatusString);

        if (payrollStatus == PayrollStatus.ReturnedTwice) {
            createReturnedTwiceEmailParams();
        } else {
            createDDDebitReturnNonNsfEmailParams();
        }
    }

    protected void createChangeRedebitToWireExpectedEmailParams() {
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollRunId is missing or invalid."));
        }

        // first look for returned redebit txn.  If DD+Tax, will find one or the other, but will have the same PR and return code on both.
        FinancialTransaction ft = FinancialTransaction.findFinancialTransactions(payrollRun, new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit, TransactionTypeCode.EmployerTaxRedebit},
                                                                                 new TransactionStateCode[]{TransactionStateCode.Returned}).getFirst();

        // if no redebit txn, look for returned debit txn
        if (ft == null) {
            ft = FinancialTransaction.findFinancialTransactions(payrollRun, new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerTaxDebit},
                                                                new TransactionStateCode[]{TransactionStateCode.Returned}).getFirst();
        }

        if (ft == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve 'Returned' employer debit or redebit transaction from PayrollRun."));
        }

        // element 0 contains the payroll amount less fees
        // element 1 contains the fees
        SpcfDecimal[] payrollDebitsAndFees = EmailUtils.getPayrollDebitsAndFees(ft.getPayrollRun());

        String returnReason = null;

        // locate the company event for the returned financial transaction
        DomainEntitySet<CompanyEventDetail> returnsForDebitOrRedebit =
                CompanyEvent.findCompanyEventDetailForEventDetailValue(
                        payrollRun.getCompany(),
                        EventDetailTypeCode.ACHReturnReasonCode,
                        EventDetailTypeCode.FinancialTransactionId,
                        ft.getId().toString());

        if (!returnsForDebitOrRedebit.isEmpty()) {
            returnReason = EmailUtils.getReadableReturnFailureReason(returnsForDebitOrRedebit.get(0).getCompanyEvent());
        }

        if (returnReason == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve return reason description from company event."));
        }

        String wireExpectedDate = EmailUtils.getDetailString(getCompanyEvent(),
                                                             EventDetailTypeCode.WireExpectedDate);

        if (wireExpectedDate == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail WireExpectedDate is missing or invalid."));
        }

        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.FailureReason, returnReason);
        addEmailParameter(EventEmailParamTypeCode.WireExpectedDate, EmailUtils.formatDate(EmailUtils.parseAsLocalDateTime("yyyyMMdd", wireExpectedDate)));
        addEmailParameter(EventEmailParamTypeCode.AdjustedPayrollDebitAmount, EmailUtils.formatMoney(payrollDebitsAndFees[0].add(payrollDebitsAndFees[1])));
        addEmailParameter(EventEmailParamTypeCode.CompanyID, payrollRun.getCompany().getSourceCompanyId());

        setCommonEmailParams(EventEmailTemplateTypeCode.WireExpectedNotification4, ContactRole.PayrollAdmin);
    }

    protected void createManualRedebitCreatedEmailParams() {
        DomainEntitySet<FinancialTransaction> txnList =
                EmailUtils.getFinancialTransactions(getCompanyEvent());

        if (txnList.isEmpty()) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        FinancialTransaction ft = txnList.get(0);
        PayrollRun payrollRun = ft.getPayrollRun();

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve payroll run from financial transaction."));
        }

        // Payrollrun, cba, and settlement date will be the same for all included financial transactions
        addEmailParameter(EventEmailParamTypeCode.CompanyBankAccountLastFour, EmailUtils.getCbaLastFour(ft));
        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.RedebitSettlementDate, EmailUtils.formatDate(ft.getSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.RedebitCompletedDate, EmailUtils.formatDateAddBusinessDays(ft.getInitiationDate().toLocal(), 5));
        addEmailParameter(EventEmailParamTypeCode.AdjustedPayrollDebitAmount, EmailUtils.formatMoney(EmailUtils.getTotalRedebitAmount(txnList)));

        setCommonEmailParams(EventEmailTemplateTypeCode.ManualRedebit3, ContactRole.PayrollAdmin);
    }

    protected void createLastChanceNotifyEmailParams() {
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollRunId is missing or invalid."));
        }

        // first look for returned redebit txn
        FinancialTransaction ft = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployerDdRedebit, TransactionTypeCode.EmployerTaxRedebit},
                                                                      new TransactionStateCode[]{TransactionStateCode.Returned}).getFirst();

        // if no redebit txn, look for returned debit txn
        if (ft == null) {
            ft = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit, TransactionTypeCode.EmployerTaxDebit},
                                                     new TransactionStateCode[]{TransactionStateCode.Returned}).getFirst();
        }

        if (ft == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve 'Returned' employer debit or redebit transaction from PayrollRun."));
        }

        SpcfCalendar wireExpectedDate;
        String wireExpectedDateString = EmailUtils.getDetailString(getCompanyEvent(),
                                                                   EventDetailTypeCode.WireExpectedDate);

        if (wireExpectedDateString == null) {
            wireExpectedDate = PSPDate.getPSPTime();
        } else {
            wireExpectedDate = SpcfCalendar.parse("yyyyMMdd", wireExpectedDateString);
        }

        // element 0 contains the payroll amount less fees
        // element 1 contains the fees
        SpcfDecimal[] payrollDebitsAndFees = EmailUtils.getPayrollDebitsAndFees(ft.getPayrollRun());

        addEmailParameter(EventEmailParamTypeCode.CompanyID, payrollRun.getCompany().getSourceCompanyId());
        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.NextBusinessDate, EmailUtils.formatDate(wireExpectedDate));
        addEmailParameter(EventEmailParamTypeCode.AdjustedPayrollDebitAmount, EmailUtils.formatMoney(payrollDebitsAndFees[0].add(payrollDebitsAndFees[1])));

        setCommonEmailParams(EventEmailTemplateTypeCode.LastChanceEmail4, ContactRole.PayrollAdmin);
    }

    protected void createPayrollCancelPendingEmailParams() {
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollRunId is missing or invalid."));
        }

        String cancellationDate = EmailUtils.getDetailString(getCompanyEvent(),
                                                             EventDetailTypeCode.CancellationDateTime);

        if (cancellationDate == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail CancellationDateTime is missing or invalid."));
        }

        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.PayrollCancelDate, EmailUtils.formatDate(EmailUtils.parseAsLocalDateTime("yyyyMMdd", cancellationDate)));

        setCommonEmailParams(EventEmailTemplateTypeCode.PayrollCancellationNotification2, ContactRole.PayrollAdmin);
    }

    protected void createPayrollCancelledEmailParams() {
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollRunId is missing or invalid."));
        }

        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));

        setCommonEmailParams(EventEmailTemplateTypeCode.PayrollCancelledNotification2, ContactRole.PayrollAdmin);
    }

    protected void createDDRejectEmailParams() {
        FinancialTransaction ft = EmailUtils.getFinancialTransaction(getCompanyEvent());

        if (ft == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail FinancialTransactionId is missing or invalid."));
        }

        PayrollRun payrollRun = ft.getPayrollRun();

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve payroll run from reject transaction."));
        }

        if (payrollRun.getPayrollRunType().equals(PayrollType.BillPayment)) {
            createDDRejectBillPaymentEmailParams(ft);
        } else {
            PaycheckSplit paycheckSplit = ft.getPaycheckSplit();

            if (paycheckSplit == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Unable to retrieve paycheck split from reject transaction."));
            }

            Paycheck paycheck = paycheckSplit.getPaycheck();

            if (paycheck == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Unable to retrieve paycheck from reject transaction."));
            }

            Employee employee = paycheck.getDDEmployee();

            if (employee == null) {
                throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                     "Unable to retrieve employee from reject transaction."));
            }

            addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
            addEmailParameter(EventEmailParamTypeCode.EmployeeFirstName, employee.getFirstName());
            addEmailParameter(EventEmailParamTypeCode.EmployeeLastNameFirstInitial, employee.getLastName().substring(0, 1));

            setCommonEmailParams(EventEmailTemplateTypeCode.EEDDREJECT1, ContactRole.PayrollAdmin);
        }
    }

    protected void createDDRejectBillPaymentEmailParams(FinancialTransaction pFT) {


        PayrollRun payrollRun = pFT.getPayrollRun();

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve payroll run from reject transaction."));
        }

        BillPaymentSplit billPaymentSplit = pFT.getBillPaymentSplit();

        if (billPaymentSplit == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve bill payment split from reject transaction."));
        }

        BillPayment billPayment = billPaymentSplit.getBillPayment();

        if (billPayment == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve bill payment from reject transaction."));
        }

        Payee payee = billPayment.getPayee();

        if (payee == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve payee from reject transaction."));
        }

        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.EmployeeFirstName, payee.getName());
        addEmailParameter(EventEmailParamTypeCode.EmployeeLastNameFirstInitial, ".");

        setCommonEmailParams(EventEmailTemplateTypeCode.EEDDREJECT1, ContactRole.PayrollAdmin);
    }

//    protected void createEnrollmentStatusChangedParams() {
//        EventEmailTemplateTypeCode templateType = null;
//
//        // only want email for EIN or Name mismatches
//        if (EftpsEnrollment.isRejectEINMismatch(getCompanyEvent().getCompanyEventDetailCollection())) {
//            templateType = EventEmailTemplateTypeCode.EFTPSEnrollmentRejectedEIN;
//        } else if (EftpsEnrollment.isRejectNameMismatch(getCompanyEvent().getCompanyEventDetailCollection())) {
//            templateType = EventEmailTemplateTypeCode.EFTPSEnrollmentRejectedName;
//
//            CompanyEventEmailParam 
//            
//            addEmailParameter(EventEmailParamTypeCode.CompanyDBAName, getCompanyEvent().getCompany().getDbaName());
//            
//        }
//
//        if (templateType != null) {
//            setCommonEmailParams(templateType, ContactRole.PrimaryPrincipal);
//        }
//    }

    protected void createBillPaymentOffloadedParams() {

        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve payroll run from event."));
        }

        BillPaymentSplit billPaymentSplit = EmailUtils.getBillPaymentSplit(getCompanyEvent());

        if (billPaymentSplit == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve bill payment split from event."));
        }

        BillPayment billPayment = billPaymentSplit.getBillPayment();

        if (billPayment == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve bill payment."));
        }

        Payee payee = billPayment.getPayee();

        if (payee == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve payee."));
        }

        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(payrollRun.getPaycheckSettlementDate().toLocal()));
        addEmailParameter(EventEmailParamTypeCode.PaymentAmount, EmailUtils.formatMoney(billPaymentSplit.getFinancialTransaction().getFinancialTransactionAmount()));

        String memo = billPayment.getMemo();
        if (memo != null) {
            addEmailParameter(EventEmailParamTypeCode.Memo, memo);
        }

        String referenceNumber = billPaymentSplit.getReferenceNumber();
        if (referenceNumber != null) {
            addEmailParameter(EventEmailParamTypeCode.ReferenceNumber, referenceNumber);
        }

        String vendorAccountNumber = billPayment.getPayee().getAccountNumber();
        if (vendorAccountNumber != null) {
            addEmailParameter(EventEmailParamTypeCode.VendorAccountNumber, vendorAccountNumber);
        }

        PayeeBankAccount vendorBankAccount = billPaymentSplit.getPayeeBankAccount();
        String cbaLastFour = null;
        if (vendorBankAccount != null) {
            String accountNumber = vendorBankAccount.getBankAccount().getAccountNumber();

            // if account number is > 4 characters, get last four, else get last digit only
            if (accountNumber.length() > 4) {
                cbaLastFour = accountNumber.substring(accountNumber.length() - 4);
            } else if (accountNumber.length() > 0) {
                cbaLastFour = accountNumber.substring(accountNumber.length() - 1);
            }
        } else {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Unable to retrieve vendor bank account from event."));
        }

        addEmailParameter(EventEmailParamTypeCode.VendorBankAccountLastFour, cbaLastFour);

        String receipientEmail = getCompanyEvent().getCompanyEventDetails(EventDetailTypeCode.RecipientEmailAddress).getFirst() != null
                ? getCompanyEvent().getCompanyEventDetails(EventDetailTypeCode.RecipientEmailAddress).getFirst().getValue()
                : payee.getEmail();

        // For BillPayment initiated from PayBills, we're using a different vendor payment offloaded template.
        BillPaymentTransactionType transactionType = billPayment.getTransactionType();
        if (transactionType != null && transactionType.equals(BillPaymentTransactionType.PayBills))
            setCommonEmailParams(EventEmailTemplateTypeCode.VendorPaymentOffloadedForPayBills, payee.getName(), receipientEmail);
        else
            setCommonEmailParams(EventEmailTemplateTypeCode.VendorPaymentOffloadedForWriteChecks, payee.getName(), receipientEmail);

    }

    protected void createTOKNotifiedOfCompanyFraudParams(String pOverrideEmailAddress) {
        String serviceSubStatus = EmailUtils.getServiceSubStatus(getCompanyEvent());

        addEmailParameter(EventEmailParamTypeCode.CompanyLegalName, getCompanyEvent().getCompany().getLegalName());
        addEmailParameter(EventEmailParamTypeCode.CompanyEIN, getCompanyEvent().getCompany().getFedTaxId());
        addEmailParameter(EventEmailParamTypeCode.HoldReason, serviceSubStatus);

        setCommonEmailParams(EventEmailTemplateTypeCode.TOKFraudNotification,
                             ContactRole.PayrollAdmin,
                             pOverrideEmailAddress);
    }

    protected void createNewEntitlementEmailParams(String pOverrideRecipientEmailAddress) {
        String serviceSubStatus = EmailUtils.getServiceSubStatus(getCompanyEvent());
        EventEmailTemplateTypeCode eventEmailTemplateTypeCode =EmailUtils.getEventEmailTemplateTypeCode(getCompanyEvent());
        if (Objects.nonNull(eventEmailTemplateTypeCode)
                && eventEmailTemplateTypeCode.equals(EventEmailTemplateTypeCode.SKAssistedKey1)) {
            addEmailParameter(EventEmailParamTypeCode.ServiceKey, EmailUtils.getDetailString(getCompanyEvent()
                    , EventDetailTypeCode.ServiceKey));
        } else {
            addEmailParameter(EventEmailParamTypeCode.RecipientEmail, pOverrideRecipientEmailAddress);
        }
        setCommonEmailParams(eventEmailTemplateTypeCode,
                ContactRole.PrimaryPrincipal);
    }

    protected void createTOKNotifiedOfOperationAfterOffloadParams(String pVoidOrDelete, String pOverrideEmailAddress) {

        Paycheck paycheck = EmailUtils.getPaycheck(getCompanyEvent());
        Employee employee = paycheck.getSourceEmployee();

        addEmailParameter(EventEmailParamTypeCode.CompanyEIN, getCompanyEvent().getCompany().getFedTaxId());
        addEmailParameter(EventEmailParamTypeCode.EmployeeFirstName, employee.getFirstName());
        addEmailParameter(EventEmailParamTypeCode.EmployeeLastName, employee.getLastName());
        addEmailParameter(EventEmailParamTypeCode.PayPeriodBeginDate, paycheck.getPayPeriodBeginDate().format("d"));
        addEmailParameter(EventEmailParamTypeCode.PayPeriodEndDate, paycheck.getPayPeriodEndDate().format("d"));
        addEmailParameter(EventEmailParamTypeCode.VoidOrDelete, pVoidOrDelete);

        setCommonEmailParams(EventEmailTemplateTypeCode.TOKVoidDelete,
                             ContactRole.PayrollAdmin,
                             pOverrideEmailAddress);
    }

    private void createNoPrintChecksParams() {
        setCommonEmailParams(EventEmailTemplateTypeCode.NonPrintChecks,
                             ContactRole.PayrollAdmin);
    }

    private void createOffload401kValidationAlert(boolean pPre401kValidation) {
        StringBuilder employeeList = new StringBuilder();
        DomainEntitySet<CompanyEventDetail> companyEventDetails;

        CompanyEvent companyEvent = getCompanyEvent();

        companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.CompanyEventId);
        for (CompanyEventDetail companyEventDetail : companyEventDetails) {
            DomainEntitySet<CompanyEventDetail> invalidDataEventDetails;

            CompanyEvent invalidDataEvent = Application.findById(CompanyEvent.class,
                                                                 SpcfUniqueId.createInstance(companyEventDetail.getValue()));

            invalidDataEventDetails = invalidDataEvent.getCompanyEventDetails(EventDetailTypeCode.EmployeeName);
            String employeeName = "";
            for (CompanyEventDetail invalidDataEventDetail : invalidDataEventDetails) {
                employeeName = invalidDataEventDetail.getValue();
            }

            invalidDataEventDetails = invalidDataEvent.getCompanyEventDetails(EventDetailTypeCode.EmployeeInvalidReason);

            employeeList.append("<table width=\"100%\" cellpadding=\"5\">");
            for (CompanyEventDetail invalidDataEventDetail : invalidDataEventDetails) {
                employeeList.append("<tr><td width=\"30%\" nowrap=\"nowrap\" valign=\"top\">")
                            .append(employeeName)
                            .append("</td><td width=\"70%\">")
                            .append(invalidDataEventDetail.getValue())
                            .append("</td></tr>");
            }
            employeeList.append("</table>");
        }

        addEmailParameter(EventEmailParamTypeCode.EmployeeList, employeeList.toString());

        if (pPre401kValidation) {
            companyEventDetails = companyEvent.getCompanyEventDetails(EventDetailTypeCode.OffloadDate);
            for (CompanyEventDetail companyEventDetail : companyEventDetails) {
                addEmailParameter(EventEmailParamTypeCode.Four01kTransmissionDate, companyEventDetail.getValue());
            }

            setCommonEmailParams(EventEmailTemplateTypeCode.Correct401kEmployeeInfo, ContactRole.PayrollAdmin);
        } else {
            setCommonEmailParams(EventEmailTemplateTypeCode.Correct401kEmployeeInfoAfterSend, ContactRole.PayrollAdmin);
        }
    }

    private void createSUIAdjustmentTransactionParams(EventTypeCode pEventTypeCode) {
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollRunId is missing or invalid."));
        }
        // Get Transactions and Laws
        DomainEntitySet<FinancialTransaction> financialTransactions = payrollRun.getFinancialTransactionCollection()
                                                                                .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.AgencyTaxCredit,
                                                                                                                                                    TransactionTypeCode.AgencyTaxDebit,
                                                                                                                                                    TransactionTypeCode.EmployerSUITaxCollection,
                                                                                                                                                    TransactionTypeCode.EmployerSUITaxRefund)
                                                                                                          .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notIn(TransactionStateCode.Cancelled, TransactionStateCode.Voided)));

        HashMap<Law, SpcfMoney> laws = new HashMap<Law, SpcfMoney>();
        SpcfCalendar paymentPeriodBegin = null;
        for (FinancialTransaction ft : financialTransactions) {
            SpcfMoney ftAmount;
            switch (ft.getTransactionType().getTransactionTypeCd()) {
                case AgencyTaxCredit:
                    ftAmount = ft.getFinancialTransactionAmount();
                    if (!laws.containsKey(ft.getLaw())) {
                        laws.put(ft.getLaw(), ftAmount);
                    } else {
                        ftAmount = new SpcfMoney(ftAmount.add(laws.get(ft.getLaw())));
                        laws.put(ft.getLaw(), ftAmount);
                    }
                    paymentPeriodBegin = ft.getMoneyMovementTransaction().getPaymentPeriodBegin().toLocal();
                    break;
                case AgencyTaxDebit:
                    ftAmount = new SpcfMoney(ft.getFinancialTransactionAmount().negate());
                    if (!laws.containsKey(ft.getLaw())) {
                        laws.put(ft.getLaw(), ftAmount);
                    } else {
                        ftAmount = new SpcfMoney(ftAmount.add(laws.get(ft.getLaw())));
                        laws.put(ft.getLaw(), ftAmount);
                    }
                    paymentPeriodBegin = ft.getMoneyMovementTransaction().getPaymentPeriodBegin().toLocal();
                    break;
                case EmployerSUITaxRefund:
                case EmployerSUITaxCollection:
                    for (FinancialTransaction eoqTransaction : ft.getAssociatedTransactionsCollection()) {
                        ftAmount = eoqTransaction.getFinancialTransactionAmount();
                        if (eoqTransaction.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.AgencyTaxDebit) ||
                                eoqTransaction.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerSUITaxPayable)) {
                            ftAmount = new SpcfMoney(ftAmount.negate());
                        }
                        if (!laws.containsKey(eoqTransaction.getLaw())) {
                            laws.put(eoqTransaction.getLaw(), ftAmount);
                        } else {
                            ftAmount = new SpcfMoney(ftAmount.add(laws.get(eoqTransaction.getLaw())));
                            laws.put(eoqTransaction.getLaw(), ftAmount);

                        }
                        paymentPeriodBegin = eoqTransaction.getMoneyMovementTransaction().getPaymentPeriodBegin().toLocal();
                    }
                    break;
            }
        }
        String lawIdParamValue = "";
        for (Law law : laws.keySet()) {
            CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(payrollRun.getCompany(), law.getLawId());
            lawIdParamValue = lawIdParamValue + " " + companyLaw.getSourceDescription() + " ($" + laws.get(law).toString() + "),";
        }
        lawIdParamValue = lawIdParamValue.substring(0, lawIdParamValue.lastIndexOf(","));

        addEmailParameter(EventEmailParamTypeCode.LawId, lawIdParamValue);
        if (paymentPeriodBegin != null) {
            addEmailParameter(EventEmailParamTypeCode.Quarter, Integer.toString(CalendarUtils.getQuarterAsInt(paymentPeriodBegin)));
            addEmailParameter(EventEmailParamTypeCode.Year, Integer.toString(paymentPeriodBegin.getYear()));
        }

        FinancialTransaction adjustmentFT = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxDebit, TransactionTypeCode.EmployerTaxCredit)).getFirst();

        addEmailParameter(EventEmailParamTypeCode.Amount, adjustmentFT.getFinancialTransactionAmount().toString());

        // set company legal name param
        //addEmailParameter(EventEmailParamTypeCode.CompanyLegalName, payrollRun.getCompany().getLegalName());

        switch (pEventTypeCode) {
            case SUIEoqDebitCreated:
                addEmailParameter(EventEmailParamTypeCode.DebitSettlementDate, EmailUtils.formatDate(adjustmentFT.getSettlementDate()));
                setCommonEmailParams(EventEmailTemplateTypeCode.EndofQuarterSUIDebitNotification4, ContactRole.PayrollAdmin);
                break;
            case SUIImmediateDebitCreated:
                addEmailParameter(EventEmailParamTypeCode.DebitSettlementDate, EmailUtils.formatDate(adjustmentFT.getSettlementDate()));
                setCommonEmailParams(EventEmailTemplateTypeCode.SameDaySUIDebitNotification4, ContactRole.PayrollAdmin);
                break;
            case SUIEoqCreditCreated:
            case SUIImmediateCreditCreated:
                addEmailParameter(EventEmailParamTypeCode.EffectiveCreditPostingDate, EmailUtils.formatDate(adjustmentFT.getSettlementDate()));
                addEmailParameter(EventEmailParamTypeCode.PayrollRunDate, EmailUtils.formatDate(payrollRun.getPayrollRunDate().toLocal()));
                setCommonEmailParams(EventEmailTemplateTypeCode.SUIRefundNotification4, ContactRole.PayrollAdmin);
                break;
        }
    }

    private void createMonthlyFeeCreatedParams(EventTypeCode pEventTypeCode) {
        Company company = getCompanyEvent().getCompany();
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollRunId is missing or invalid."));
        }

        SpcfCalendar runDate = payrollRun.getPayrollRunDate().toLocal();
        addEmailParameter(EventEmailParamTypeCode.PayrollRunDate, EmailUtils.formatDate(runDate));
        addEmailParameter(EventEmailParamTypeCode.CompanyLegalName, company.getLegalName());
        addEmailParameter(EventEmailParamTypeCode.CompanyEIN, company.getFedTaxId());

        setCommonEmailParams(EventEmailTemplateTypeCode.MinimumMonthlyBilling, ContactRole.PayrollAdmin);

    }

    private void createInvalidVendorEmailParams() {
        Company company = getCompanyEvent().getCompany();
        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());

        if (payrollRun == null) {
            throw new RuntimeException(EmailUtils.formatErrorMsg(this,
                                                                 "Required event detail PayrollRunId is missing or invalid."));
        }

        SpcfCalendar settlementDate = payrollRun.getPaycheckSettlementDate().toLocal();
        addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, EmailUtils.formatDate(settlementDate));
        addEmailParameter(EventEmailParamTypeCode.CompanyLegalName, company.getLegalName());
        addEmailParameter(EventEmailParamTypeCode.CompanyEIN, company.getFedTaxId());
        String invalidVendorEmail = getCompanyEvent().getCompanyEventDetails(EventDetailTypeCode.VendorInvalidEmail).getFirst().getValue();
        addEmailParameter(EventEmailParamTypeCode.VendorInvalidEmailAddress, invalidVendorEmail);

        String serviceType = getEmailParamValue(EventEmailParamTypeCode.ServiceType);
        if (serviceType == null) {
            addEmailParameter(EventEmailParamTypeCode.ServiceType, "Payroll");
        }

        setCommonEmailParams(EventEmailTemplateTypeCode.VendorInvalidEmail, ContactRole.PayrollAdmin);

    }


    private void createVmpSignUpEmployeeEmailParams() {
        Company company = getCompanyEvent().getCompany();
        String employeeId = getCompanyEvent().getCompanyEventDetails(EventDetailTypeCode.EmployeeId).getFirst().getValue();
        Employee employee = EmailUtils.getById(Employee.class, employeeId);

        addEmailParameter(EventEmailParamTypeCode.EmployeeFirstName, employee.getFirstName());
        addEmailParameter(EventEmailParamTypeCode.CompanyDBAName, company.getDbaName());
        addEmailParameter(EventEmailParamTypeCode.RecipientFirstName, employee.getFirstName());
        addEmailParameter(EventEmailParamTypeCode.RecipientLastName, employee.getLastName());
        String recipientEmail = getCompanyEvent().getCompanyEventDetails(EventDetailTypeCode.RecipientEmailAddress).getFirst().getValue();
        addEmailParameter(EventEmailParamTypeCode.RecipientEmail, recipientEmail);

        setEmailTemplateTypeCd(EventEmailTemplateTypeCode.VmpEmployeeWelcome);
    }

    private void createVmpSignUpEmployerEmailParams() {
        Company company = getCompanyEvent().getCompany();
        String employeeId = getCompanyEvent().getCompanyEventDetails(EventDetailTypeCode.EmployeeId).getFirst().getValue();
        Employee employee = EmailUtils.getById(Employee.class, employeeId);

        addEmailParameter(EventEmailParamTypeCode.EmployeeFirstName, employee.getFirstName());
        addEmailParameter(EventEmailParamTypeCode.EmployeeLastName, employee.getLastName());
        addEmailParameter(EventEmailParamTypeCode.CompanyDBAName, company.getDbaName());
        setCommonEmailParams(EventEmailTemplateTypeCode.VmpEmployerWelcome, ContactRole.PayrollAdmin);
        //If no service type exists default to payroll
        String serviceType = getEmailParamValue(EventEmailParamTypeCode.ServiceType);
        if (serviceType == null) {
            addEmailParameter(EventEmailParamTypeCode.ServiceType, "Payroll");
        }
    }

    private void createVmpPaystubNotificationEmailParams() {
        String employeeId = getCompanyEvent().getCompanyEventDetails(EventDetailTypeCode.EmployeeId).getFirst().getValue();
        Employee employee = EmailUtils.getById(Employee.class, employeeId);
        String paycheckDate = getCompanyEvent().getCompanyEventDetailValue(EventDetailTypeCode.PaycheckDate);

        addEmailParameter(EventEmailParamTypeCode.EmployeeFirstName, employee.getFirstName());
        addEmailParameter(EventEmailParamTypeCode.EmployeeLastName, employee.getLastName());
        addEmailParameter(EventEmailParamTypeCode.RecipientFirstName, employee.getFirstName());
        addEmailParameter(EventEmailParamTypeCode.RecipientLastName, employee.getLastName());

        //PSP-7097: Research on using email parameter PaycheckSettlementDate
        if (paycheckDate != null) {
            addEmailParameter(EventEmailParamTypeCode.PaycheckSettlementDate, paycheckDate);
        }

        //RecipientEmailAddress is added by the IamEmailAddressProcessor
        setEmailTemplateTypeCd(EventEmailTemplateTypeCode.VmpPaystubNotification);
    }

    private void createSUICreditsAppliedEmailParams() {
        addEmailParameter(EventEmailParamTypeCode.Amount, getCompanyEvent().getCompanyEventDetailValue(EventDetailTypeCode.RefundAmount));
        addEmailParameter(EventEmailParamTypeCode.SUICreditAmount, getCompanyEvent().getCompanyEventDetailValue(EventDetailTypeCode.Amount));
        String[] quarterYear = getCompanyEvent().getCompanyEventDetailValue(EventDetailTypeCode.NewDate).split(" ");
        addEmailParameter(EventEmailParamTypeCode.Year, quarterYear[0]);
        addEmailParameter(EventEmailParamTypeCode.Quarter, quarterYear[1]);
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(getCompanyEvent().getCompany(), getCompanyEvent().getCompanyEventDetailValue(EventDetailTypeCode.Law));
        addEmailParameter(EventEmailParamTypeCode.LawId, companyLaw.getSourceDescription());

        PayrollRun payrollRun = EmailUtils.getPayrollRun(getCompanyEvent());
        SpcfCalendar settlementDate = null;
        if (payrollRun != null) {
            FinancialTransaction creditFT = payrollRun.getFinancialTransactionCollection()
                                                      .find(FinancialTransaction.TransactionType().TransactionTypeCd().in(TransactionTypeCode.EmployerTaxCredit))
                                                      .getFirst();
            if (creditFT != null) {
                settlementDate = creditFT.getSettlementDate().toLocal();
            }
        }
        if (settlementDate == null) {
            settlementDate = PSPDate.getPSPTime();
        }
        addEmailParameter(EventEmailParamTypeCode.EffectiveCreditPostingDate, EmailUtils.formatDate(settlementDate));

        setCommonEmailParams(EventEmailTemplateTypeCode.SUICreditNotification, ContactRole.PayrollAdmin);
    }

    private void createLegacySubscriptionMigrationEmailParams(String pRecipientEmail) {
        String baseRate = EmailUtils.getDetailString(getCompanyEvent(),
                EventDetailTypeCode.BaseRate);
        String billingFrequencyType = StringUtils.lowerCase(EmailUtils.getDetailString(getCompanyEvent(),
                EventDetailTypeCode.BillingFrequencyType));
        EventDetailTypeCode daysTillRenewalEventDetailTypeCode = EventDetailTypeCode.DaysTillRenewal;
        String daysTillRenewal  = EmailUtils.getDetailString(getCompanyEvent(),
                    daysTillRenewalEventDetailTypeCode);
        String licenseNumber = EmailUtils.getDetailString(getCompanyEvent(),
                EventDetailTypeCode.LicenseNumber);
        String eoc = EmailUtils.getDetailString(getCompanyEvent(),
                EventDetailTypeCode.EntitlementOfferingCode);
        String sourceCompanyId = EmailUtils.getDetailString(getCompanyEvent(),
                EventDetailTypeCode.SourceCompanyId);

        EventEmailTemplateTypeCode eventEmailTemplateTypeCode = EventEmailTemplateTypeCode.valueOf(EmailUtils.getDetailString(getCompanyEvent(),
                EventDetailTypeCode.EmailTemplateType));

        addEmailParameter(EventEmailParamTypeCode.BaseRate, baseRate);
        addEmailParameter(EventEmailParamTypeCode.BillingFrequencyType, billingFrequencyType);
        EventEmailParamTypeCode daysTillRenewalEventEmailParamTypeCode = EventEmailParamTypeCode.DaysTillRenewal;
        addEmailParameter(daysTillRenewalEventEmailParamTypeCode, daysTillRenewal);
        addEmailParameter(EventEmailParamTypeCode.LicenseNumber, licenseNumber);
        addEmailParameter(EventEmailParamTypeCode.EntitlementOfferingCode, eoc);
        addEmailParameter(EventEmailParamTypeCode.CompanyID, sourceCompanyId);

        setCommonEmailParams(eventEmailTemplateTypeCode, ContactRole.PayrollAdmin, pRecipientEmail);
    }

    private void createMOFederalAssessmentEmailParams() {

/*        addEmailParameter(EventEmailParamTypeCode.DebitSettlementDate, employee.getFirstName());
        addEmailParameter(EventEmailParamTypeCode.Amount, employee.getFirstName());
        addEmailParameter(EventEmailParamTypeCode.Quarter, employee.getFirstName());
        addEmailParameter(EventEmailParamTypeCode.Year, employee.getFirstName());

        setEmailTemplateTypeCd(EventEmailTemplateTypeCode.);*/

    }

    private void addEmailParameter(EventEmailParamTypeCode pEventEmailParamTypeCode, String pValue) {
        //Email job fails on null / empty values
        if (StringUtils.isNotEmpty(pValue)) {
            CompanyEventEmailParam eventEmailParam = new CompanyEventEmailParam();
            eventEmailParam.setCompanyEventEmail(this);
            eventEmailParam.setCompany(this.getCompanyEvent().getCompany());
            eventEmailParam.setParamTypeCd(pEventEmailParamTypeCode);
            eventEmailParam.setValue(pValue);
            Application.save(eventEmailParam);
        } else {
            logger.warn("Unable to create CompanyEventEmailParam, null or empty value for EventEmailParamTypeCode=" + pEventEmailParamTypeCode);
        }
    }

    /*
        We are using the value for AssetItemNumber as 1101754 to identify the OneMonthReactivation product.
        We can do it by having this value in a system_parameter and then reading it from there instead of hardcoding it here.

        Checking if the entitlement is subscribed to the one month reactivation product SymphonyWelcomeOneMonthReactivation
        If so, then the UsageBillingEmail should not be sent
    */
    private boolean checkForSymphonyOneMonthReactivation(Entitlement e) {
        if (e.getEntitlementCode() != null && ("1101754".equals(e.getEntitlementCode().getAssetItemNumber()))) {
            return true;
        }
        return false;
    }

    //method to get DDEmployeelist associated with Payroll run
    protected List<String> getDDEmployeeList(PayrollRun payrollRun){
        DomainEntitySet<Paycheck> paycheck = payrollRun.getDDPaycheckCollection();
        List<String> employees =  new ArrayList<>();
        StringBuilder employeeList = new StringBuilder();
        for(Paycheck p: paycheck) {
            Employee employee = p.getDDEmployee();
            String name = employee.getFirstName()+" "+employee.getLastName();
            employeeList.append(name)
                    .append("<br/>");

            if (employeeList.length() > 3500) {
                employees.add(employeeList.toString());
                employeeList = new StringBuilder();
            }
        }
        employees.add(employeeList.toString());
        return employees;
    }

    // Method to get TotalTaxes Charged to be sent in email as part of MTL Compliance for a payroll run
    protected SpcfDecimal getFinancialTransactionAmountSum(DomainEntitySet<FinancialTransaction> taxTxnList ) {
        SpcfDecimal totalAmount = SpcfDecimal.createInstance("0.00");
        if(taxTxnList.isEmpty()){
            return totalAmount;
        }
        for (FinancialTransaction txnFT : taxTxnList) {
            totalAmount = totalAmount.add(txnFT.getFinancialTransactionAmount());
            }
        return totalAmount;
    }

    // Method too get txnNumber string prefixed with zeros
    protected String getTransactionIdPrefixWithZero(String txnNumber){
        if(Objects.isNull(txnNumber)){
            return "Not Available";
        }
        StringBuffer stringBuffer =  new StringBuffer();
        for (int i=txnNumber.length(); i<10; i++) {
            stringBuffer.append("0");
        }
        stringBuffer.append(txnNumber);
        return stringBuffer.toString();
    }

    // method to get 1099 vendor list
    protected List<String> getVendorListWithDetails(DomainEntitySet<FinancialTransaction> employerDDDebitList, DomainEntitySet<FinancialTransaction> employeeDDCreditList) {
        StringBuilder vendorPaymentList = new StringBuilder("");
        MoneyMovementTransaction mmt;
        if (employerDDDebitList.isEmpty()) {
            return Collections.singletonList(vendorPaymentList.toString());
        }
        List<String[]> columns = new ArrayList<>();
        for (FinancialTransaction feeTxnFT : employerDDDebitList) {
            mmt = feeTxnFT.getMoneyMovementTransaction();
            if(!feeTxnFT.getFinancialTransactionAmount().equals(mmt.getMoneyMovementTransactionAmount())){
                logger.warn("Financial Transaction Amount is not matching with Money Movement Transaction Amount for "+ feeTxnFT);
            }
            BillPayment billPayment = getBillPaymentForEmployerDDDebit(feeTxnFT, employeeDDCreditList);
            if(Objects.isNull(billPayment)){
                logger.warn("Bill Payment not found for the Employer DD Debit Financial Transaction "+ feeTxnFT);
                continue;
            }
            if(Objects.isNull(billPayment.getPayee())){
                logger.warn("No Payee found for the Vendor Bill Payment "+billPayment);
                continue;
            }
            columns.add(new String[]{billPayment.getPayee().getName(),
                    "$"+mmt.getMoneyMovementTransactionAmount(),
                    getTransactionIdPrefixWithZero(mmt.getTransactionNumber()) });
        }
        return getVendorHtmlTableFormattedString(columns);
    }

    // method to get formatted table vendor list
    private List<String> getVendorHtmlTableFormattedString(List<String[]> columns) {
        StringBuilder vendorPaymentList = new StringBuilder("");
        List<String> vendors = new ArrayList<>();
        String tableStart = "<table border=\"1\" width=\"70%\" cellpadding=\"5\">";
        String tableEnd = "</table>";
        String tableRowStart = "<tr>";
        String tableRowEnd = "</tr>";
        String tableColumnStart = "<td width=\"30%\" nowrap=\"nowrap\" valign=\"top\">";
        String tableColumnEnd = "</td>";
        String boldStart = "<b>";
        String boldEnd = "</b>";
        String[] columnHeaders = new String[] {"Name", "Amount", "Transaction ID"};
        vendorPaymentList.append(tableStart);
        vendorPaymentList.append(tableRowStart);
        for (String columnHeader: columnHeaders) {
            vendorPaymentList.append(tableColumnStart)
                    .append(boldStart)
                    .append(columnHeader)
                    .append(boldEnd)
                    .append(tableColumnEnd);
        }
        vendorPaymentList.append(tableRowEnd);
        for (String[] columnvalues: columns) {
            vendorPaymentList.append(tableRowStart);
            for (String columnvalue :columnvalues) {
                vendorPaymentList.append(tableColumnStart)
                        .append(columnvalue)
                        .append(tableColumnEnd);
            }
            vendorPaymentList.append(tableRowEnd);
            if (vendorPaymentList.length() > 3500) {
                vendors.add(vendorPaymentList.toString());
                vendorPaymentList = new StringBuilder();
            }
        }
        vendorPaymentList.append(tableEnd);
        vendors.add(vendorPaymentList.toString());
        return vendors;
    }
    private BillPayment getBillPaymentForEmployerDDDebit(FinancialTransaction employerDDDebit, DomainEntitySet<FinancialTransaction> employeeDDCreditList) {
        for(FinancialTransaction employeeDDCredit: employeeDDCreditList){
            if(employeeDDCredit.getRelatableTransaction().getId().equals(employerDDDebit.getId())){
                return employeeDDCredit.getBillPaymentSplit().getBillPayment();
            }
        }
        return null;
    }

    /**
     * Returns if the Event Email is MTL supported or not.
     *
     * Checks:
     *
     * if the templateTypeCd is MTL supported.
     *
     *
     * @param pEventEmail
     * @return true if the event is MTL supported
     */
    public static boolean isEventMtlCompliant(final CompanyEventEmail pEventEmail) {
        // Only emails after April 30th 2020 are required by MTL. Value is set in configuration.
        String mtlSupportedTransactions = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "mtl_supported_transaction");
        if(!mtlSupportedTransactions.contains(pEventEmail.getEmailTemplateTypeCd().toString())) {
            return false;
        }
        String mtlConfiguredDate = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "mtl_start_date");
        return isEventDateAfterMtlDate(mtlConfiguredDate, pEventEmail) && isEventInSentState(pEventEmail);
    }

    /**
     * Checks if the email has been sent after a particular date as required by MTL examiner,
     *
     * @param mtlConfiguredDate
     * @param pEventEmail
     * @return true or false
     */
    public static boolean isEventDateAfterMtlDate(String mtlConfiguredDate, CompanyEventEmail pEventEmail) {
        if(Objects.isNull(mtlConfiguredDate)) {
            logger.error("MTL date is not configured.");
            return false;
        }
        SpcfCalendar mtlStartDate = SpcfCalendar.parse("MM/dd/yyyy", mtlConfiguredDate);
        return pEventEmail.getStatusEffectiveDate().after(mtlStartDate);
    }

    /**
     * Checks if the email status is in `Sent` state,
     *
     * @param pEventEmail
     * @return true or false
     */
    public static boolean isEventInSentState(CompanyEventEmail pEventEmail) {
        return pEventEmail.getStatusCd().toString().equals("Sent");
    }
}
