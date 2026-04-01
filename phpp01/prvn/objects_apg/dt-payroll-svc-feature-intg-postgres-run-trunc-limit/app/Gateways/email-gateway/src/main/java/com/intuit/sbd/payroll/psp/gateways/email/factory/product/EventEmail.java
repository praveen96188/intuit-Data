package com.intuit.sbd.payroll.psp.gateways.email.factory.product;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailFormatType;
import com.intuit.sbd.payroll.psp.gateways.email.util.EventStatus;
import com.intuit.sbd.payroll.psp.gateways.email.util.EmailUtils;
import com.intuit.sbd.payroll.psp.gateways.email.intfc.IEventEmail;
import com.intuit.sbd.payroll.psp.Application;

import java.util.*;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: kpaul
 * Date: Jul 20, 2008
 * Time: 11:42:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class EventEmail implements IEventEmail {
    private static final String sfIdPattern = "%templateid%-%eventtype%-%companyid%-%guid%";

    private EventStatus mEventStatus = null;
    private String mRecipientId = null;
    private String mCompanyId = null;
    private Properties mEmailParams = new Properties();

    public EventEmail(EventStatus pEventStatus) {
        mEventStatus = pEventStatus;
        init();
    }

    private void init() {
        CompanyEventEmail companyEventEmail = mEventStatus.getEvent();
        CompanyEvent companyEvent = companyEventEmail.getCompanyEvent();
        Company company = companyEvent.getCompany();

        mCompanyId = company.getSourceCompanyId();

        // set the recipient id (this will be: %templateid%-%eventtype%-%companyid%-%guid%)
        mRecipientId = sfIdPattern.
                replaceFirst("%templateid%", Matcher.quoteReplacement(companyEventEmail.getEmailTemplateTypeCd().toString())).
                replaceFirst("%eventtype%", Matcher.quoteReplacement(companyEvent.getEventTypeCd().toString())).
                replaceFirst("%companyid%", Matcher.quoteReplacement(mCompanyId)).
                replaceFirst("%guid%", Matcher.quoteReplacement(companyEventEmail.getId().toString()));

        if (mRecipientId.length() > 100) {
            mRecipientId = mRecipientId.substring(0, 99);
        }

        for (CompanyEventEmailParam param : companyEventEmail.getEmailParamsForEmailEvent()) {
            addProperty(param.getParamTypeCd(), param.getValue());

            // keep the cache clear of noise
            Application.evict(param);
        }

        // keep the cache clear of noise
        Application.evict(company);

        if(company.isInvalidContactEmailId(getRecipientEmail())) {
            mEventStatus.setSkipped();
            // Create company event to capture email skip.
            CompanyEvent.createSendEmailSkippedEvent(company, companyEventEmail.getEmailTemplateTypeCd(), getRecipientEmail());
        } else {
            mEventStatus.setAssigned();
        }
    }

    public boolean readyToSend() {
        return mEventStatus.isAssigned();
    }

    public boolean isRetry() {
        return mEventStatus.isRetry();
    }

    public void sendSuccessful() {
        mEventStatus.setSent();
    }

    public EventEmailTemplateTypeCode getTemplateId() {
        return mEventStatus.getTemplateId();
    }

    public String getRecipientId() {
        return mRecipientId;
    }

    public String getCompanyId() {
        return mCompanyId;
    }

    public EmailFormatType getPreferredFormat() {
        // todo v2: add preferred email format to Contact in model.
        return EmailFormatType.HTML;
    }

    public String getRecipientName() {
        String firstName, lastName;
        //Allow explicitly setting recipient first or last name to override any other set values
        firstName = mEmailParams.getProperty(EventEmailParamTypeCode.RecipientFirstName.toString());
        if(firstName == null) {
            firstName = mEmailParams.getProperty(EventEmailParamTypeCode.PayrollAdminFirstName.toString());
            if (firstName == null) {
                firstName = mEmailParams.getProperty(EventEmailParamTypeCode.PrimaryPrincipalFirstName.toString());
            }
        }

        lastName = mEmailParams.getProperty(EventEmailParamTypeCode.RecipientLastName.toString());
        if(lastName == null) {
            lastName = mEmailParams.getProperty(EventEmailParamTypeCode.PayrollAdminLastName.toString());
            if (lastName == null) {
                lastName = mEmailParams.getProperty(EventEmailParamTypeCode.PrimaryPrincipalLastName.toString());
            }
        }

        if(firstName == null && lastName == null && mEmailParams.getProperty(EventEmailParamTypeCode.BillingContactName.toString()) != null) {
            return mEmailParams.getProperty(EventEmailParamTypeCode.BillingContactName.toString());
        }

        return firstName + " " + lastName;
    }

    public String getRecipientEmail() {
        String emailAddress;
        //Allow explicitly setting recipient email to override any other set values
        emailAddress = mEmailParams.getProperty(EventEmailParamTypeCode.RecipientEmail.toString());
        if(emailAddress == null) {
            emailAddress = mEmailParams.getProperty(EventEmailParamTypeCode.PayrollAdminEmail.toString());
            if (emailAddress == null) {
                emailAddress = mEmailParams.getProperty(EventEmailParamTypeCode.PrimaryPrincipalEmail.toString());
            }
        }

        return emailAddress;
    }

    public Properties getProperties() {
        return mEmailParams;
    }

    public void addProperty(EventEmailParamTypeCode pParamName, String pParamValue) {
        mEmailParams.setProperty(pParamName.toString(), pParamValue);
    }

    public void failedValidation(String pErrorDetail) {
        EmailUtils.formatFailedValidationError(pErrorDetail, mEventStatus);
    }

    public void failedWithListDetectiveError(String pErrorCd, String pErrorMsg) {
        Company company = mEventStatus.getEvent().getCompanyEvent().getCompany();

        for (Contact contact : Application.find(Contact.class, Contact.Company().equalTo(company).And(Contact.Email().equalTo(getRecipientEmail())))) {
            contact.setHasInvalidEmail(true);
        }

        for (Payee payee : Application.find(Payee.class, Payee.Company().equalTo(company).And(Payee.Email().like("%" + getRecipientEmail() + "%")))) {
            List<String> emailList = Arrays.asList(payee.getEmail().split("[;,]"));
            // if one of vendor's email matches the recipient email, we mark the payee as having invalid email.
            // Note that the other email in the list could have been sent with a success.
            // The wrong email would be logged in the SAP log and the vendor's email would be in error in the UI.
            if(emailList.contains(getRecipientEmail())){
                payee.setHasInvalidEmail(true);
            }

        }
        // Create company event to capture this failure.
        CompanyEvent.createSendEmailFailedEvent(company, mEventStatus.getTemplateId(), getRecipientEmail(), pErrorCd, pErrorMsg);

        mEventStatus.setListDetectiveError();
    }

    public void serviceReturned(String pErrorDetail) {
        EmailUtils.formatServiceReturnedError(pErrorDetail, mEventStatus);
    }

    public void serviceFault(String pErrorDetail) {
        EmailUtils.formatServiceFaultError(pErrorDetail, mEventStatus);
    }

    public StringBuffer reportErrors() {
        StringBuffer err = new StringBuffer();
        StringBuffer subErr = new StringBuffer();

        subErr.append(mEventStatus.reportErrors());

        if (subErr.length() > 0) {
            err.append(EmailUtils.sfNewLine);

            err.append("> Event email parameters for template id: ");
            err.append((getTemplateId() != null) ? getTemplateId().toString() : "<unknown>");
            err.append(EmailUtils.sfNewLine);

            err.append("  [ ");
            err.append("RecipientId");
            err.append(" ] ");
            err.append((mRecipientId != null) ? mRecipientId : "<unknown>");
            err.append(EmailUtils.sfNewLine);

            err.append("  [ ");
            err.append("RecipientName");
            err.append(" ] ");
            err.append((getRecipientName() != null) ? getRecipientName() : "<unknown>");
            err.append(EmailUtils.sfNewLine);

            err.append("  [ ");
            err.append("RecipientEmail");
            err.append(" ] ");
            err.append((getRecipientEmail() != null) ? getRecipientEmail() : "<unknown>");
            err.append(EmailUtils.sfNewLine);

            for (Map.Entry<Object, Object> pair : mEmailParams.entrySet()) {
                err.append("  [ ");
                err.append(pair.getKey().toString());
                err.append(" ] ");
                err.append(pair.getValue().toString());
                err.append(EmailUtils.sfNewLine);
            }

            err.append(subErr);
        }

        return err;
    }
}
