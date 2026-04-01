package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Hand-written business logic
 */
public class ACHEnrollment extends BaseACHEnrollment {

    private static class StateMachine {
        private static Map<ACHEnrollmentStatus, ACHEnrollmentStatus[]> stateMachine;

        static Map<ACHEnrollmentStatus, ACHEnrollmentStatus[]> getMachine() {
            if (stateMachine == null) {
                HashMap<ACHEnrollmentStatus, ACHEnrollmentStatus[]> machine = new HashMap<ACHEnrollmentStatus, ACHEnrollmentStatus[]>();

                machine.put(ACHEnrollmentStatus.PendingEnrollment, new ACHEnrollmentStatus[]{
                        ACHEnrollmentStatus.PendingEnrollmentResponse, ACHEnrollmentStatus.Enrolled, ACHEnrollmentStatus.Cancelled});
                machine.put(ACHEnrollmentStatus.PendingEnrollmentResponse, new ACHEnrollmentStatus[]{
                        ACHEnrollmentStatus.Enrolled, ACHEnrollmentStatus.EnrollmentRejected, ACHEnrollmentStatus.PendingEnrollment});
                machine.put(ACHEnrollmentStatus.EnrollmentRejected, new ACHEnrollmentStatus[]{
                        ACHEnrollmentStatus.Cancelled, ACHEnrollmentStatus.Enrolled, ACHEnrollmentStatus.PendingEnrollment});

                machine.put(ACHEnrollmentStatus.PendingDelete, new ACHEnrollmentStatus[]{
                        ACHEnrollmentStatus.Deleted, ACHEnrollmentStatus.Cancelled});

                machine.put(ACHEnrollmentStatus.Cancelled, new ACHEnrollmentStatus[]{
                        ACHEnrollmentStatus.PendingEnrollment, ACHEnrollmentStatus.PendingDelete});

                stateMachine = Collections.unmodifiableMap(machine);
            }
            return stateMachine;
        }
    }

    public boolean isTransitionAllowed(ACHEnrollmentStatus pACHEnrollmentStatus) {
        if (getStatus() == pACHEnrollmentStatus) return true;
        ACHEnrollmentStatus[] allowedTransitions = getAllowedTransitions();
        if (allowedTransitions != null && allowedTransitions.length > 0) {
            for (ACHEnrollmentStatus allowedTransition : allowedTransitions) {
                if (allowedTransition == pACHEnrollmentStatus) {
                    return true;
                }
            }
        }
        //
        return false;
    }

    public ACHEnrollmentStatus[] getAllowedTransitions() {
        Map<ACHEnrollmentStatus, ACHEnrollmentStatus[]> machine = StateMachine.getMachine();
        return machine.get(getStatus());
    }

    public void updateStatus(ACHEnrollmentStatus pACHEnrollmentStatus) {
        updateStatus(pACHEnrollmentStatus, true);
    }

    public void updateStatus(ACHEnrollmentStatus pACHEnrollmentStatus, boolean pCapturePresentStatus) {
        ACHEnrollmentStatus oldACHEnrollmentStatus = null;

        if(pCapturePresentStatus) {
            oldACHEnrollmentStatus = getStatus();
        }

        if (!isTransitionAllowed(pACHEnrollmentStatus)) {
            throw new RuntimeException("ACHEnrollmentStatus transition is not allowed From:" + getStatus().toString() + " To:" + pACHEnrollmentStatus.toString());
        }
        setStatus(pACHEnrollmentStatus);
        setStatusEffectiveDate(PSPDate.getPSPTime());

        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(getCompanyAgency().getCompany(), paymentTemplate);
        if(companyAgencyPaymentTemplate != null) {
            companyAgencyPaymentTemplate.recalculatePaymentMethods();
        }

        //Create event for status change
        CompanyEvent.createACHEnrollmentStatusChangedEvent(getCompanyAgency().getCompany(), this, oldACHEnrollmentStatus);

    }

    public static ACHEnrollment createACHEnrollment(Company pCompany, boolean pReAdd) {
        ACHEnrollment currentACHEnrollment;
        CompanyAgency companyAgency = pCompany.getCompanyAgencyCollection().findEntity(CompanyAgency.Agency().AgencyId().equalTo(Agency.FL_AGENT_ID));
        if (companyAgency == null) {
            //If there is no FL company Agency, Can not enroll in to FL ACH Enrollment
            return null;
        }
        currentACHEnrollment = pCompany.getCurrentACHEnrollment();

        if (currentACHEnrollment != null &&
                (currentACHEnrollment.getStatus() == ACHEnrollmentStatus.PendingEnrollment ||
                        (!pReAdd && currentACHEnrollment.getStatus().in(ACHEnrollmentStatus.Enrolled, ACHEnrollmentStatus.PendingEnrollmentResponse)))) {
            //Already it is enrolled or pending for acceptance, no need to send again
            return currentACHEnrollment;
        }

        // Check for all the requirements for sending ADD request
        //1. Tax service has to be in Active status
        CompanyService taxService = pCompany.getCompanyService(ServiceCode.Tax);
        if (taxService == null || !taxService.isActive()) {
            return null;
        }

        //2. Company Law Filing status has to be in Active
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(PaymentTemplate.FL_SUI);

        CompanyLaw companyLaw = companyAgency.getCompanyLawCollection().find(CompanyLaw.Law().PaymentTemplate().equalTo(paymentTemplate)).getFirst();

        if (companyLaw == null) {
            return null;
        }

        if (companyLaw.getFilingStatus() != null && companyLaw.getFilingStatus() == PayrollItemStatus.Inactive) {
            return null;
        }

        //3. Agency Id has to be in correct format
        CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(pCompany, paymentTemplate);
        AgencyIdRequirement agencyIdRequirement = null;
        PaymentTemplatePaymentMethod paymentTemplatePaymentMethod = companyAgencyPaymentTemplate.getPaymentTemplate().getPaymentTemplatePaymentMethod(PaymentMethod.ACHCredit);
        CompanyPaymentTemplatePaymentMethod companyPaymentTemplatePaymentMethod = companyAgencyPaymentTemplate.getCompanyPaymentTemplatePaymentMethod(PaymentMethod.ACHCredit);
        for (PaymentMethodRequirement paymentMethodRequirement : paymentTemplatePaymentMethod.getPaymentMethodRequirementCollection()) {
            if (paymentMethodRequirement instanceof AgencyIdRequirement) {
                agencyIdRequirement = (AgencyIdRequirement) paymentMethodRequirement;
                break;
            }
        }
        if (agencyIdRequirement == null || !agencyIdRequirement.isRequirementMet(companyPaymentTemplatePaymentMethod)) {
            return null;
        }

        // All conditions are met to send enrollment details in ADD file
        if (currentACHEnrollment != null && currentACHEnrollment.getStatus() == ACHEnrollmentStatus.PendingDelete) {
            currentACHEnrollment.updateStatus(ACHEnrollmentStatus.Cancelled);
            Application.save(currentACHEnrollment);
        }

        ACHEnrollment achEnrollment = new ACHEnrollment();
        achEnrollment.setCompanyAgency(companyAgency);
        achEnrollment.updateStatus(ACHEnrollmentStatus.PendingEnrollment, false);
        SpcfCalendar effectiveDate = PSPDate.getPSPTime();
        if(taxService.getServiceStartDate() != null && taxService.getServiceStartDate().after(effectiveDate)){
            effectiveDate = taxService.getServiceStartDate();
        }
        achEnrollment.setEffectiveDate(CalendarUtils.getFirstDayOfQuarter(effectiveDate));
        Application.save(achEnrollment);
        companyAgency.addACHEnrollment(achEnrollment);

        return achEnrollment;
    }

    public static ACHEnrollment deleteACHEnrollment(Company pCompany) {
        ACHEnrollment currentACHEnrollment;
        CompanyAgency companyAgency = CompanyAgency.findCompanyAgency(pCompany, Agency.FL_AGENT_ID);
        if (companyAgency == null) {
            //If there is no FL company Agency, Can not delete FL ACH Enrollment
            return null;
        }
        currentACHEnrollment = pCompany.getCurrentACHEnrollment();

        if (currentACHEnrollment != null && currentACHEnrollment.getStatus().in(ACHEnrollmentStatus.PendingDelete, ACHEnrollmentStatus.Deleted)) {
            //Already it is in Delete process, no need to send again in Delete file
            return currentACHEnrollment;
        }

        if (currentACHEnrollment != null && currentACHEnrollment.getStatus() == ACHEnrollmentStatus.PendingEnrollment) {
            currentACHEnrollment.updateStatus(ACHEnrollmentStatus.Cancelled);
            Application.save(currentACHEnrollment);
        }

        currentACHEnrollment = companyAgency.getACHEnrollmentCollection().find(ACHEnrollment.Status().in(ACHEnrollmentStatus.PendingEnrollmentResponse, ACHEnrollmentStatus.Enrolled, ACHEnrollmentStatus.Deleted))
                                            .sort(ACHEnrollment.StatusEffectiveDate().Descending())
                                            .getFirst();
        if (currentACHEnrollment != null && currentACHEnrollment.getStatus().in(ACHEnrollmentStatus.PendingEnrollmentResponse, ACHEnrollmentStatus.Enrolled)) {
            // All conditions are met to send enrollment details in ADD file
            ACHEnrollment achEnrollment = new ACHEnrollment();
            achEnrollment.setCompanyAgency(companyAgency);
            achEnrollment.updateStatus(ACHEnrollmentStatus.PendingDelete, false);
            achEnrollment.setEffectiveDate(CalendarUtils.getLastDayOfPreviousQuarter(PSPDate.getPSPTime()));
            Application.save(achEnrollment);
            companyAgency.addACHEnrollment(achEnrollment);
            currentACHEnrollment = achEnrollment;
        }
        return currentACHEnrollment;
    }


}