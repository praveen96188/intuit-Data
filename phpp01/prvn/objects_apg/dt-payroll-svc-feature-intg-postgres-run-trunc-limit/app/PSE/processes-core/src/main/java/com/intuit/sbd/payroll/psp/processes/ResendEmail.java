package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.ContactRole;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang.WordUtils;

import java.util.*;

/**
 * User: dweinberg
 * Date: Dec 14, 2009
 * Time: 4:34:58 PM
 */
public class ResendEmail extends Process {

    private String emailId; // This is not email address.
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private boolean resendRelatedEvents = false;
    private String sessionUserEmailAddress;

    private CompanyEventEmail oldEmail;
    private Company company;
    private ContactRole targetRole;
    private Contact contact;
    private List<ResendEmail> relatedEmailProcesses;

    //codes that we don't copy
    private static Set<EventEmailParamTypeCode> contactEmailParamCodes;

    static {
        contactEmailParamCodes = new TreeSet<EventEmailParamTypeCode>();
        contactEmailParamCodes.add(EventEmailParamTypeCode.PayrollAdminFirstName);
        contactEmailParamCodes.add(EventEmailParamTypeCode.PayrollAdminLastName);
        contactEmailParamCodes.add(EventEmailParamTypeCode.PayrollAdminEmail);
        contactEmailParamCodes.add(EventEmailParamTypeCode.PrimaryPrincipalFirstName);
        contactEmailParamCodes.add(EventEmailParamTypeCode.PrimaryPrincipalLastName);
        contactEmailParamCodes.add(EventEmailParamTypeCode.PrimaryPrincipalEmail);
    }

    // Events that need to keep the contact Info from the original event
    private static Set<EventTypeCode> keepContactInfoEmails;

    static {
        keepContactInfoEmails = new TreeSet<EventTypeCode>();
        keepContactInfoEmails.add(EventTypeCode.BillPaymentOffloaded);
        keepContactInfoEmails.add(EventTypeCode.DeletedPaycheckAlreadyOffloadedToTOK);
        keepContactInfoEmails.add(EventTypeCode.VoidedPaycheckAlreadyOffloadedToTOK);
    }

    public ResendEmail(SourceSystemCode sourceSystemCd, String sourceCompanyId, String emailId, boolean resendRelatedEvents, String sessionUserEmailAddress) {
        this.emailId = emailId;
        this.sourceSystemCd = sourceSystemCd;
        this.sourceCompanyId = sourceCompanyId;
        this.resendRelatedEvents = resendRelatedEvents;
        this.sessionUserEmailAddress = sessionUserEmailAddress;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult<CompanyEventEmail> validationResult = new ProcessResult<CompanyEventEmail>();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        company = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                    this.sourceSystemCd.toString(), sourceCompanyId);
            return validationResult;
        }

        if (emailId == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Company, sourceCompanyId, "emailId");
            return validationResult;
        }

        oldEmail = PayrollServices.entityFinder.findById(CompanyEventEmail.class, SpcfUniqueId.createInstance(emailId));

        if (oldEmail == null) {
            validationResult.getMessages().GenericError(EntityName.Company, sourceCompanyId, "CompanyEventEmail does not exist");
            return validationResult;
        }

        if (!oldEmail.getStatusCd().equals(EventEmailStatus.Sent) && !oldEmail.getStatusCd().equals(EventEmailStatus.SendFailed)) {
            validationResult.getMessages().GenericError(EntityName.Company, sourceCompanyId, "CompanyEventEmail must be in state Sent or SendFailed");
        }

        //to whom should we send it?
        targetRole = getTargetContactRole(validationResult);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }
        //QBOE companies do not have PrimaryPrincipal, though the templates are expecting it to be the PP
        //In this case we will still create the parameters as PrimaryPrincipal, but we will pull them from the PayrollAdmin
        if (sourceSystemCd == SourceSystemCode.QBOE && targetRole == ContactRole.PrimaryPrincipal) {
            contact = company.getContactByRoleCode(ContactRole.PayrollAdmin);
        } else {
            contact = company.getContactByRoleCode(targetRole);
        }
        if (contact == null) {
            validationResult.getMessages().GenericError(EntityName.Contact, sourceCompanyId, "Contact does not exist");
            return validationResult;
        }

        //make sure the contact has name and address
        if ((contact.getFirstName() == null) || (contact.getLastName() == null)) {
            validationResult.getMessages().GenericError(EntityName.Contact, sourceCompanyId, "Unable to retrieve valid first/last name for specified contact role");
            return validationResult;
        } else if (contact.getEmail() == null) {
            validationResult.getMessages().GenericError(EntityName.Contact, sourceCompanyId, "Unable to retrieve valid email address for specified contact role");
            return validationResult;
        }

        if (resendRelatedEvents) {
            validateResendRelatedEvents(validationResult);
        }

        return validationResult;
    }

    private void validateResendRelatedEvents(ProcessResult validationResult) {
        //related events are ones that were generated by the same payroll run and were sent to the same contact.

        //find the event detail that says which payroll generated the event
        //assumption is that all events of the same type will have the payroll information in the same detail

        CompanyEventDetail eventDetail = getDetailFromEvent(oldEmail.getCompanyEvent());

        if (eventDetail == null) {
            //nothing can be related
            return;
        }

        String[] values = getRelatedValues(eventDetail);

        //now find the destination of the email
        String emailAddress = getTargetEmail(oldEmail);

        DomainEntitySet<CompanyEventEmail> relatedEmails = CompanyEventEmail.findCompanyEventEmailsByEventDetailAndEmailTarget(
                company,
                oldEmail.getCompanyEvent().getEventTypeCd(),
                eventDetail.getEventDetailTypeCd(),
                values,
                emailAddress);

        relatedEmailProcesses = new ArrayList<ResendEmail>();

        //we only want one per event and we're sorting by event
        //also want to exclude this email
        CompanyEvent lastCompanyEvent = null;
        for (CompanyEventEmail relatedEmail : relatedEmails) {
            if (relatedEmail.getCompanyEvent() != lastCompanyEvent && this.oldEmail.getCompanyEvent() != relatedEmail.getCompanyEvent()) {
                lastCompanyEvent = relatedEmail.getCompanyEvent();
                ResendEmail relatedResendEmail = new ResendEmail(this.sourceSystemCd,
                        this.sourceCompanyId,
                        relatedEmail.getId().toString(),
                        false,
                        this.sessionUserEmailAddress);

                validationResult.merge(relatedResendEmail.validate());
                relatedEmailProcesses.add(relatedResendEmail);
            }
        }
    }

    private String[] getRelatedValues(CompanyEventDetail eventDetail) {
        switch (eventDetail.getEventDetailTypeCd()) {
            case PayrollRunId:
            case SourcePayrollRunId:
                return new String[]{eventDetail.getValue()};
            case FinancialTransactionId:
                String finTxnId = eventDetail.getValue();
                FinancialTransaction finTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(finTxnId));
                String[] txns = new String[finTxn.getPayrollRun().getFinancialTransactionCollection().size()];
                int i = 0;
                for (FinancialTransaction payrollTxn : finTxn.getPayrollRun().getFinancialTransactionCollection()) {
                    txns[i] = payrollTxn.getId().toString();
                    i++;
                }
                return txns;
        }
        return null;

    }

    private CompanyEventDetail getDetailFromEvent(CompanyEvent ce) {
        PayrollRun pr = null;

        DomainEntitySet<CompanyEventDetail> payrollRunIds = ce.getCompanyEventDetails(EventDetailTypeCode.PayrollRunId);
        if (payrollRunIds.size() > 0) {
            String payrollRunId = payrollRunIds.get(0).getValue();
            pr = PayrollServices.entityFinder.findById(PayrollRun.class, SpcfUniqueId.createInstance(payrollRunId));
        }
        if (pr != null) {
            return payrollRunIds.get(0);
        }

        DomainEntitySet<CompanyEventDetail> sourcePayrollRunIds = ce.getCompanyEventDetails(EventDetailTypeCode.SourcePayrollRunId);
        if (sourcePayrollRunIds.size() > 0) {
            String sourcePayrollRunId = sourcePayrollRunIds.get(0).getValue();
            pr = PayrollRun.findPayrollRun(company, sourcePayrollRunId);
        }
        if (pr != null) {
            return payrollRunIds.get(0);
        }

        DomainEntitySet<CompanyEventDetail> finTxnIds = ce.getCompanyEventDetails(EventDetailTypeCode.FinancialTransactionId);
        for (CompanyEventDetail finTxnDetail : finTxnIds) {
            String finTxnId = finTxnDetail.getValue();
            FinancialTransaction finTxn = PayrollServices.entityFinder.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(finTxnId));
            if (finTxn != null) {
                pr = finTxn.getPayrollRun();
                if (pr != null) {
                    return finTxnDetail;
                }
            }
        }

        return null;
    }


    private String getTargetEmail(CompanyEventEmail cee) {
        DomainEntitySet<CompanyEventEmailParam> emails = cee.getEmailParamForEmailEvent(
                EventEmailParamTypeCode.PayrollAdminEmail, EventEmailParamTypeCode.PrimaryPrincipalEmail);
        if (emails.size() > 0) {
            return emails.get(0).getValue();
        }

        return null;
    }

    private ContactRole getTargetContactRole(ProcessResult validationResult) {
        int adminEmail = oldEmail.getEmailParamForEmailEvent(EventEmailParamTypeCode.PayrollAdminEmail).size();
        int ppEmail = oldEmail.getEmailParamForEmailEvent(EventEmailParamTypeCode.PrimaryPrincipalEmail).size();
        if (adminEmail > 0 && ppEmail > 0) {
            validationResult.getMessages().GenericError(EntityName.Company, sourceCompanyId, "Both Payroll Admin and primary principle in email parameters");
            return null;
        } else if (adminEmail > 0) {
            return ContactRole.PayrollAdmin;
        } else if (ppEmail > 0) {
            return ContactRole.PrimaryPrincipal;
        } else {
            validationResult.getMessages().GenericError(EntityName.Company, sourceCompanyId, "Neither Payroll Admin nor Primary Principle in email parameters");
            return null;
        }

    }

    @Override
    public ProcessResult process() {
        ProcessResult<CompanyEventEmail> processResult = new ProcessResult<CompanyEventEmail>();

        CompanyEventEmail eventEmail = new CompanyEventEmail();
        eventEmail.setCompanyEvent(oldEmail.getCompanyEvent());
        eventEmail.setCompany(oldEmail.getCompanyEvent().getCompany());
        eventEmail.setStatusCd(EventEmailStatus.PendingResend);
        eventEmail.setStatusEffectiveDate(PSPDate.getPSPTime());
        eventEmail.setEmailTemplateTypeCd(oldEmail.getEmailTemplateTypeCd());
        Application.save(eventEmail);

        //copy all the non-contact params
        for (CompanyEventEmailParam param : oldEmail.getEmailParamsForEmailEvent()) {
            if (!contactEmailParamCodes.contains(param.getParamTypeCd())) {
                CompanyEventEmailParam newParam = new CompanyEventEmailParam();
                newParam.setCompanyEventEmail(eventEmail);
                newParam.setCompany(eventEmail.getCompanyEvent().getCompany());
                newParam.setParamTypeCd(param.getParamTypeCd());
                newParam.setValue(param.getValue());
                Application.save(newParam);
            }
        }

        //add the contact params
        EventEmailParamTypeCode firstNameParamCd = null;
        EventEmailParamTypeCode lastNameParamCd = null;
        EventEmailParamTypeCode emailAddressParamCd = null;
        switch (targetRole) {
            case PayrollAdmin:
                firstNameParamCd = EventEmailParamTypeCode.PayrollAdminFirstName;
                lastNameParamCd = EventEmailParamTypeCode.PayrollAdminLastName;
                emailAddressParamCd = EventEmailParamTypeCode.PayrollAdminEmail;
                break;

            case PrimaryPrincipal:
                firstNameParamCd = EventEmailParamTypeCode.PrimaryPrincipalFirstName;
                lastNameParamCd = EventEmailParamTypeCode.PrimaryPrincipalLastName;
                emailAddressParamCd = EventEmailParamTypeCode.PrimaryPrincipalEmail;
                break;

        }

        CompanyEventEmailParam firstNameParam = new CompanyEventEmailParam();
        CompanyEventEmailParam lastNameParam = new CompanyEventEmailParam();
        CompanyEventEmailParam emailAddressParam = new CompanyEventEmailParam();


        firstNameParam.setCompanyEventEmail(eventEmail);
        firstNameParam.setCompany(eventEmail.getCompanyEvent().getCompany());
        firstNameParam.setParamTypeCd(firstNameParamCd);

        lastNameParam.setCompanyEventEmail(eventEmail);
        lastNameParam.setCompany(eventEmail.getCompanyEvent().getCompany());
        lastNameParam.setParamTypeCd(lastNameParamCd);

        emailAddressParam.setCompanyEventEmail(eventEmail);
        emailAddressParam.setCompany(eventEmail.getCompanyEvent().getCompany());
        emailAddressParam.setParamTypeCd(emailAddressParamCd);


        if (!keepContactInfoEmails.contains(oldEmail.getCompanyEvent().getEventTypeCd())) {
            firstNameParam.setValue(WordUtils.capitalize(contact.getFirstName().toLowerCase()));
            lastNameParam.setValue(WordUtils.capitalize(contact.getLastName().toLowerCase()));
            // If sessionUserEmailAddress is passed, then change the destination email address.
            if(Objects.nonNull(sessionUserEmailAddress)) {
                emailAddressParam.setValue(sessionUserEmailAddress);
            } else {
                emailAddressParam.setValue(contact.getEmail());
            }

        } else {
            CompanyEventEmailParam param;
            param = oldEmail.getEmailParamForEmailEvent(firstNameParamCd).get(0);
            firstNameParam.setValue(param.getValue());
            param = oldEmail.getEmailParamForEmailEvent(lastNameParamCd).get(0);
            lastNameParam.setValue(param.getValue());
            param = oldEmail.getEmailParamForEmailEvent(emailAddressParamCd).get(0);
            if(Objects.nonNull(sessionUserEmailAddress)) {
                emailAddressParam.setValue(sessionUserEmailAddress);
            } else {
                emailAddressParam.setValue(param.getValue());
            }

        }

        Application.save(firstNameParam);
        Application.save(lastNameParam);
        Application.save(emailAddressParam);


        processResult.setResult(eventEmail);

        if (relatedEmailProcesses != null) {
            for (ResendEmail relatedEmailProcess : relatedEmailProcesses) {
                processResult.merge(relatedEmailProcess.execute());
            }
        }

        return processResult;
    }


}
