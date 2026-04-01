package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Hand-written business logic
 */
public class RAFEnrollment extends BaseRAFEnrollment {
    private static class StateMachine {
        private static Map<RAFEnrollmentStatus, RAFEnrollmentStatus[]> stateMachine;

        static Map<RAFEnrollmentStatus, RAFEnrollmentStatus[]> getMachine() {
            if (stateMachine == null) {
                HashMap<RAFEnrollmentStatus, RAFEnrollmentStatus[]> machine = new HashMap<RAFEnrollmentStatus, RAFEnrollmentStatus[]>();
                machine.put(RAFEnrollmentStatus.PendingEnrollment, new RAFEnrollmentStatus[]{
                        RAFEnrollmentStatus.PendingEnrollmentTape, RAFEnrollmentStatus.Cancelled});
                machine.put(RAFEnrollmentStatus.PendingEnrollmentTape, new RAFEnrollmentStatus[]{
                        RAFEnrollmentStatus.PendingEnrollment, RAFEnrollmentStatus.Cancelled, RAFEnrollmentStatus.PendingEnrollmentResponse});
                machine.put(RAFEnrollmentStatus.PendingEnrollmentResponse, new RAFEnrollmentStatus[]{
                        RAFEnrollmentStatus.Enrolled, RAFEnrollmentStatus.Rejected});
                machine.put(RAFEnrollmentStatus.Enrolled, new RAFEnrollmentStatus[]{
                        RAFEnrollmentStatus.Cancelled, RAFEnrollmentStatus.PendingDeleteTape});
                machine.put(RAFEnrollmentStatus.Rejected, new RAFEnrollmentStatus[]{
                        RAFEnrollmentStatus.Cancelled, RAFEnrollmentStatus.Enrolled});
                machine.put(RAFEnrollmentStatus.Cancelled, new RAFEnrollmentStatus[]{
                        RAFEnrollmentStatus.PendingDeleteTape});
                machine.put(RAFEnrollmentStatus.PendingDeleteTape, new RAFEnrollmentStatus[]{
                        RAFEnrollmentStatus.Enrolled, RAFEnrollmentStatus.Deleted});
                machine.put(RAFEnrollmentStatus.Deleted, new RAFEnrollmentStatus[]{RAFEnrollmentStatus.Cancelled});
                stateMachine = Collections.unmodifiableMap(machine);
            }
            return stateMachine;
        }
    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean isTransitionAllowed(RAFEnrollmentStatus status) {
        if (getStatus() == status) return true;
        RAFEnrollmentStatus[] allowedTransitions = getAllowedTransitions();
        if (allowedTransitions!=null && allowedTransitions.length>0) {
            for (RAFEnrollmentStatus allowedTransition : allowedTransitions) {
                if (allowedTransition == status) {
                    return true;
                }
            }
        }
        //
        return false;
    }

    public static RAFEnrollment createNewEnrollment(CompanyAgency pCompanyAgency) {
        RAFEnrollment newEnrollment = new RAFEnrollment();

        // status of this entity
        newEnrollment.setStatus(RAFEnrollmentStatus.PendingEnrollment);
        newEnrollment.setStatusEffectiveDate(PSPDate.getPSPTime());

        // relate it to the Company by way of the CompanyAgency
        newEnrollment.setCompanyAgency(pCompanyAgency);

        newEnrollment = Application.save(newEnrollment);
        pCompanyAgency.addRAFEnrollment(newEnrollment);

        CompanyEvent.createRAFEnrollmentStatusChangeEvent(newEnrollment, null);

        return newEnrollment;
    }

    public RAFEnrollment updateEnrollmentStatus(RAFEnrollmentStatus pNewStatus) {
        RAFEnrollmentStatus oldStatus = getStatus();

        if ((pNewStatus == null) || pNewStatus.equals(oldStatus)) {
            return this;
        }

        setStatus(pNewStatus);
        setStatusEffectiveDate(PSPDate.getPSPTime());

        RAFEnrollment enrollment = Application.save(this);

        CompanyEvent.createRAFEnrollmentStatusChangeEvent(enrollment, oldStatus);

        return enrollment;
    }

    public static DomainEntitySet<RAFEnrollment> getPendingRAFEnrollmentsOrderedByFEIN(int pMaxRows, RAFActionCode pRAFActionCode) {
        RAFEnrollmentStatus status = getRAFEnrollmentStatusForActionCode(pRAFActionCode);
        Expression<RAFEnrollment> query = null;
        Criterion criterion = RAFEnrollment.Status().equalTo(status);

        if (Company.isDGDeleteFeatureEnabled()) {
            criterion = criterion.And(RAFEnrollment.CompanyAgency().Company().IsDgDisassociated().equalTo(Boolean.FALSE));
        }

        query = new Query<RAFEnrollment>()
                .Where(criterion)
                .LimitResults(0, pMaxRows);
        return Application.find(RAFEnrollment.class, query);
    }

    public static DomainEntitySet<RAFEnrollment> getPendingDeleteRAFEnrollmentsOrderedByFEIN(int pMaxRows, RAFActionCode pRAFActionCode) {
        RAFEnrollmentStatus status = getRAFEnrollmentStatusForActionCode(pRAFActionCode);
        Expression<RAFEnrollment> query = null;
        Criterion criterion = RAFEnrollment.Status().equalTo(status)
                .And(RAFEnrollment.RAFEnrollmentDetail().Id().isNotNull());

        if (Company.isDGDeleteFeatureEnabled()) {
            criterion = criterion.And(RAFEnrollment.CompanyAgency().Company().IsDgDisassociated().equalTo(Boolean.FALSE));
        }
        query = new Query<RAFEnrollment>()
                .Where(criterion)
                .LimitResults(0, pMaxRows);
        return Application.find(RAFEnrollment.class, query);
    }

    public RAFEnrollmentStatus[] getAllowedTransitions() {
        Map<RAFEnrollmentStatus, RAFEnrollmentStatus[]> machine = StateMachine.getMachine();
        return machine.get(getStatus());
    }

    public static boolean shouldCancelOnServiceDeactivate(RAFEnrollmentStatus pStatus) {
        return pStatus != null && (pStatus == RAFEnrollmentStatus.PendingEnrollment || pStatus == RAFEnrollmentStatus.PendingEnrollmentTape || pStatus == RAFEnrollmentStatus.Rejected);
    }

    public static Long getRAFEnrollmentsByStatusCount(
            RAFEnrollmentStatus pStatus,
            List<String> pPSID_EINList,
            SpcfCalendar pCreationDateStart, SpcfCalendar pCreationDateEnd,
            SpcfCalendar pLastUpdateStart, SpcfCalendar pLastUpdateEnd) {
        Criterion<RAFEnrollment> expr = getRAFEnrollmentByStatusCriterion(pPSID_EINList, pCreationDateStart, pCreationDateEnd, pLastUpdateStart, pLastUpdateEnd, pStatus);
        return Application.executeScalarAggQuery(RAFEnrollment.class, new Query<RAFEnrollment>().Select(RAFEnrollment.Id().Count()).Where(expr));
    }

    public static DomainEntitySet<RAFEnrollment> getRAFEnrollmentsByStatus(RAFEnrollmentStatus pStatus,
                                                                           List<String> pPSID_EINList,
                                                                           SpcfCalendar pCreationDateStart, SpcfCalendar pCreationDateEnd,
                                                                           SpcfCalendar pLastUpdateStart, SpcfCalendar pLastUpdateEnd,
                                                                           int pFirstResult, int pMaxResults) {
        Criterion<RAFEnrollment> expr = getRAFEnrollmentByStatusCriterion(pPSID_EINList, pCreationDateStart, pCreationDateEnd, pLastUpdateStart, pLastUpdateEnd, pStatus);
        Expression<RAFEnrollment> query = new Query<RAFEnrollment>()
                .Where(expr)
                .OrderBy(RAFEnrollment.CompanyAgency().Company().FedTaxIdEnc()).LimitResults(pFirstResult, pMaxResults);

        return Application.find(RAFEnrollment.class, query);
    }

    static Criterion<RAFEnrollment> getRAFEnrollmentByStatusCriterion(List<String> pPSID_EINList,
                                                                      SpcfCalendar pCreationDateStart, SpcfCalendar pCreationDateEnd,
                                                                      SpcfCalendar pLastUpdateStart, SpcfCalendar pLastUpdateEnd,
                                                                      RAFEnrollmentStatus... pStatus) {
        Criterion<RAFEnrollment> expr = RAFEnrollment.Status().in(pStatus);
        List<String> fedTaxIdEncList = null;
        if (pPSID_EINList.size() == 1) {
            String psidEin = pPSID_EINList.get(0);
            if (StringUtils.isNotEmpty(psidEin)) {
                fedTaxIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,psidEin);
                expr = expr.And(RAFEnrollment.CompanyAgency().Company().SourceCompanyId().like("%" + psidEin + "%").Or(RAFEnrollment.CompanyAgency().Company().FedTaxIdEnc().in(fedTaxIdEncList)));
            }
        } else if (pPSID_EINList.size() > 1) {
            List<String> einList = pPSID_EINList;
            List<String> encEinList = new ArrayList<>();
            for (String ein : einList) {
                fedTaxIdEncList = (EncryptionUtils.deterministicEncryptWithAllKeys(Company.FedTaxIdKeyName,ein));
                encEinList.addAll(fedTaxIdEncList);
            }
            expr = expr.And(RAFEnrollment.CompanyAgency().Company().SourceCompanyId().in(pPSID_EINList).Or(RAFEnrollment.CompanyAgency().Company().FedTaxIdEnc().in(encEinList)));
        }
        if (pCreationDateStart != null) {
            expr = expr.And(RAFEnrollment.CreatedDate().greaterOrEqualThan(pCreationDateStart));
        }
        if (pCreationDateEnd != null) {
            expr = expr.And(RAFEnrollment.CreatedDate().lessOrEqualThan(pCreationDateEnd));
        }
        if (pLastUpdateStart != null) {
            expr = expr.And(RAFEnrollment.ModifiedDate().greaterOrEqualThan(pLastUpdateStart));
        }
        if (pLastUpdateEnd != null) {
            expr = expr.And(RAFEnrollment.ModifiedDate().lessOrEqualThan(pLastUpdateEnd));
        }
        return expr;
    }

    public static Long getCountRAFEnrollmentsForActionCode(RAFActionCode pCode) {
        RAFEnrollmentStatus status = getRAFEnrollmentStatusForActionCode(pCode);

        return Application.executeScalarAggQuery(RAFEnrollment.class, new Query<RAFEnrollment>()
                .Select(RAFEnrollment.Id().Count())
                .Where(RAFEnrollment.Status().equalTo(status)));
    }

    private static RAFEnrollmentStatus getRAFEnrollmentStatusForActionCode(RAFActionCode pCode) {
        if (pCode == RAFActionCode.Add) {
            return RAFEnrollmentStatus.PendingEnrollmentTape;
        } else {
            return RAFEnrollmentStatus.PendingDeleteTape;
        }
    }
}
