package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.sbd.payroll.psp.domain.BaseSMSSyncFailure;

import java.util.List;
import java.util.Optional;

/**
 * Hand-written business logic
 */
public class SMSSyncFailure extends BaseSMSSyncFailure {

    /**
     * Default constructor.
     */
    public SMSSyncFailure() {
        super();
    }


    private static final SpcfLogger logger = SpcfLogManager.getLogger(SMSSyncFailure.class);

    public static void saveSMSSyncFailureIfAbsent(String realmId, String failureReason) {

        Long realmIdLong = Long.valueOf(realmId);
        DomainEntitySet<SMSSyncFailure> failures = getSmsSyncFailureByRealmId(realmIdLong);

        if (failures.isEmpty()) {
            BaseSMSSyncFailure smsSyncFailure = new SMSSyncFailure();
            smsSyncFailure.setCompanyRealmId(realmIdLong);
            smsSyncFailure.setSyncDirection(com.intuit.sbd.payroll.psp.domain.SMSSyncDirection.ASToPSP);
            smsSyncFailure.setStatus(com.intuit.sbd.payroll.psp.domain.SMSSyncJobStatus.Pending);

            if (failureReason != null && failureReason.length() > 4000) {
                failureReason = failureReason.substring(0, 4000);
            }
            smsSyncFailure.setFailureReason(failureReason);
            smsSyncFailure.setLastRetryTimeStamp(new SpcfCalendarImpl());

            Application.save(smsSyncFailure);
        }

    }

    public static DomainEntitySet<SMSSyncFailure> getSmsSyncFailureByRealmId(Long realmIdLong) {

        Criterion<SMSSyncFailure> query = SMSSyncFailure.CompanyRealmId().equalTo(realmIdLong);
        return Application.find(SMSSyncFailure.class, query);

    }

    public static DomainEntitySet<SMSSyncFailure> getAllPendingRecords() {

        Criterion<SMSSyncFailure> query = SMSSyncFailure.Status().equalTo(com.intuit.sbd.payroll.psp.domain.SMSSyncJobStatus.Pending);
        return Application.find(SMSSyncFailure.class, query);

    }


}