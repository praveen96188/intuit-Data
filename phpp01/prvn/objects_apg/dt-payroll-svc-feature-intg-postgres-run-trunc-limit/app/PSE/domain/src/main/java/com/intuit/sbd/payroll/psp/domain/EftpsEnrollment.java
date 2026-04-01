package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class EftpsEnrollment extends BaseEftpsEnrollment {
    public enum StateTransitionAction {
        UPDATE,
        ENROLL,
        REENROLL,
        NOTALLOWED
    }

    //
    // Static methods
    //

    public static EftpsEnrollment createNewEnrollment(CompanyAgency pCompanyAgency) {
        return createNewEnrollment(pCompanyAgency, false, null, null, null);
    }


    public static EftpsEnrollment createNewEnrollment(CompanyAgency pCompanyAgency, boolean isSecondary, String pFedTaxId, String pLegalName, String pLegalZip) {
        EftpsEnrollment newEnrollment = new EftpsEnrollment();

        // status of this entity
        newEnrollment.setStatusCd(EftpsEnrollmentStatus.PendingEnrollment);
        newEnrollment.setStatusEffectiveDate(PSPDate.getPSPTime());

        EftpsEnrollment currentEnrollment = pCompanyAgency.getEftpsEnrollmentCollection().sort(EftpsEnrollment.StatusEffectiveDate().Descending()).getFirst();
        if (currentEnrollment != null && currentEnrollment.getStatusEffectiveDate().equals(newEnrollment.getStatusEffectiveDate())) {
            //CI:PSRV003200 New EFTPS enrollments can be out of order -- cheat a bit so that EFTPS enrollments are in a strict total order
            SpcfCalendar newStatusEffectiveDate = PSPDate.getPSPTime();
            newStatusEffectiveDate.addMilliseconds(1);
            newEnrollment.setStatusEffectiveDate(newStatusEffectiveDate);
        }

        // relate it to the Company by way of the CompanyAgency
        newEnrollment.setCompanyAgency(pCompanyAgency);

        newEnrollment = Application.save(newEnrollment);
        pCompanyAgency.addEftpsEnrollment(newEnrollment);

        EftpsEnrollmentDetail eftpsEnrollmentDetail = null;
        if (isSecondary) {
            eftpsEnrollmentDetail = EftpsEnrollmentDetail.createEnrollmentDetail(newEnrollment, pFedTaxId, pLegalName, pLegalZip);
            newEnrollment.setSecondary(true);
        }

        CompanyEvent.createEftpsEnrollmentStatusChangeEvent(newEnrollment, eftpsEnrollmentDetail, null);

        if (!isSecondary) {
            pCompanyAgency.recalculatePaymentMethods();
        }

        return newEnrollment;
    }

    public static DomainEntitySet<EftpsEnrollment> getPendingEftpsEnrollments(int pMaxRows) {
        Expression<EftpsEnrollment> query = new Query<EftpsEnrollment>()
                .Where(StatusCd().equalTo(EftpsEnrollmentStatus.PendingEnrollment))
                .OrderBy(CreatedDate())
                .LimitResults(0, pMaxRows);

        return Application.find(EftpsEnrollment.class, query);
    }

    public static StateTransitionAction resolveTransitionAction(EftpsEnrollment pEftpsEnrollment,
                                                                EftpsEnrollmentStatus pNewStatus) {
        StateTransitionAction action;

        if (pNewStatus == null) {
            //
            // new status of null is never allowed
            //
            action = StateTransitionAction.NOTALLOWED;
        } else if (pEftpsEnrollment == null || pEftpsEnrollment.getStatusCd() == null) {
            //
            // If the company *does not* have an existing eftps enrollment record (or its StatusCd is null),
            // pNewStatus must be PendingEnrollment to allow the transition (other statuses are not allowed)
            //
            switch (pNewStatus) {
                case PendingEnrollment:
                    action = StateTransitionAction.ENROLL;
                    break;

                default:
                    action = StateTransitionAction.NOTALLOWED;
                    break;
            }
        } else {
            //
            // The company *does* have an existing eftps enrollment record, so determine if the transition is allowed
            //
            switch (pEftpsEnrollment.getStatusCd()) {
                case PendingEnrollment:
                    switch (pNewStatus) {
                        case PendingAcceptance:
                        case Invalid:
                        case Cancelled:
                            action = StateTransitionAction.UPDATE;
                            break;
                        default:
                            action = StateTransitionAction.NOTALLOWED;
                            break;
                    }
                    break;

                case PendingAcceptance:
                    switch (pNewStatus) {
                        case PendingEnrollment:
                            action = StateTransitionAction.REENROLL;
                            break;
                        case Enrolled:
                        case Rejected:
                        case AgedOut:
                        case Cancelled:
                            action = StateTransitionAction.UPDATE;
                            break;
                        default:
                            action = StateTransitionAction.NOTALLOWED;
                            break;
                    }
                    break;

                case Invalid:
                case AgedOut:
                case Enrolled:
                case Rejected:
                    switch (pNewStatus) {
                        case PendingEnrollment:
                            action = StateTransitionAction.REENROLL;
                            break;
                        case Cancelled:
                            action = StateTransitionAction.UPDATE;
                            break;
                        default:
                            action = StateTransitionAction.NOTALLOWED;
                            break;
                    }
                    break;

                case Cancelled:
                    switch (pNewStatus) {
                        case PendingEnrollment:
                            action = StateTransitionAction.ENROLL;
                            break;
                        default:
                            action = StateTransitionAction.NOTALLOWED;
                            break;
                    }
                    break;

                default:
                    action = StateTransitionAction.NOTALLOWED;
                    break;
            }
        }

        return action;
    }

    public static boolean isAllowedTransition(EftpsEnrollment pEftpsEnrollment, EftpsEnrollmentStatus pNewStatus) {
        return !StateTransitionAction.NOTALLOWED.equals(resolveTransitionAction(pEftpsEnrollment, pNewStatus));
    }

    public static int ageOutEligibleEftpsEnrollments(int pAgeOutPeriodDays) {
        //Get current Date
        SpcfCalendar date = PSPDate.getPSPTime();
        CalendarUtils.addBusinessDays(date, pAgeOutPeriodDays * -1);

        Criterion<EftpsEnrollment> where = StatusCd().equalTo(EftpsEnrollmentStatus.PendingAcceptance)
                .And(StatusEffectiveDate().lessThan(date));

        DomainEntitySet<EftpsEnrollment> enrollmentSet = Application.find(EftpsEnrollment.class, where);

        for (EftpsEnrollment enrollment : enrollmentSet) {
            enrollment.updateEnrollmentStatus(EftpsEnrollmentStatus.AgedOut);
        }

        return enrollmentSet.size();
    }

    //
    // Instance methods
    //

    /**
     * Default constructor.
     */
    public EftpsEnrollment() {
        super();
    }

    public EftpsEnrollmentDetail findEnrollmentDetail() {
        Expression<EftpsEnrollmentDetail> query = new Query<EftpsEnrollmentDetail>()
                .Where(EftpsEnrollmentDetail.EftpsEnrollment().equalTo(this))
                .OrderBy(EftpsEnrollmentDetail.CreatedDate().Descending());

        DomainEntitySet<EftpsEnrollmentDetail> enrollmentDetails = Application.find(EftpsEnrollmentDetail.class, query);

        return enrollmentDetails.isEmpty() ? null : enrollmentDetails.get(0);
    }

    public boolean isAllowedTransition(EftpsEnrollmentStatus pNewStatus) {
        return isAllowedTransition(this, pNewStatus);
    }

    public EftpsEnrollment updateEnrollmentStatus(EftpsEnrollmentStatus pNewStatus) {
        EftpsEnrollmentStatus oldStatus = getStatusCd();

        if (pNewStatus == null || pNewStatus == oldStatus) {
            return this;
        }

        setStatusCd(pNewStatus);
        setStatusEffectiveDate(PSPDate.getPSPTime());

        EftpsEnrollment enrollment = Application.save(this);

        if (!getSecondary()) {
            getCompanyAgency().recalculatePaymentMethods();
        }

        CompanyEvent.createEftpsEnrollmentStatusChangeEvent(enrollment, enrollment.findEnrollmentDetail(), oldStatus);

        return enrollment;
    }
}
