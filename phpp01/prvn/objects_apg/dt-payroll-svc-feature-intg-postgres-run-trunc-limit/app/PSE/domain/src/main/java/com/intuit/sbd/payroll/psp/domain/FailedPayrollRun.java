package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

import java.util.HashSet;

/**
 * Hand-written business logic
 */
public class FailedPayrollRun extends BaseFailedPayrollRun {

	/**
	 * Default constructor.
	 */
	public FailedPayrollRun() {
		super();
	}

	public static void createFailures(SpcfUniqueId pPayrollRunId) {
        if (findFailedPayrollRun(pPayrollRunId) != null) {
            return;
        }

		PayrollRun tPayrollRun = Application.findById(PayrollRun.class, pPayrollRunId);

		FailedPayrollRun aFailureRecord = new FailedPayrollRun();

		aFailureRecord.setPayrollRun(tPayrollRun);
		aFailureRecord.setStatusToken(SyncStatus.Error);

		Application.save(aFailureRecord);
	}

	public static DomainEntitySet<PayrollRun> findPayrollRunsWithStatus(SyncStatus pSyncStatus) {
		DomainEntitySet<FailedPayrollRun> tFailedPayrollRuns = Application.find(FailedPayrollRun.class, FailedPayrollRun.StatusToken().equalTo(pSyncStatus));
		DomainEntitySet<PayrollRun> tPayrollRuns = new DomainEntitySet<PayrollRun>();
		for (FailedPayrollRun tFailedPayrollRun : tFailedPayrollRuns)
			tPayrollRuns.add(tFailedPayrollRun.getPayrollRun());
		return tPayrollRuns;
	}

    protected static DomainEntitySet<FailedPayrollRun> findFailedPayrollRuns(SpcfUniqueId pPayrollRunId) {
        return Application.find(FailedPayrollRun.class, FailedPayrollRun.PayrollRun().Id().equalTo(pPayrollRunId));
    }

    public static FailedPayrollRun findFailedPayrollRun(SpcfUniqueId pPayrollRunId) {
        DomainEntitySet<FailedPayrollRun> tFailedPayrollRun = findFailedPayrollRuns(pPayrollRunId);

        if (!tFailedPayrollRun.isEmpty()) {
            return tFailedPayrollRun.get(0);
        } else {
            return null;
        }
    }

	public static void updateStatusCode(HashSet<SpcfUniqueId> pPayrollRunIds, SyncStatus pSyncStatus) {
		for (SpcfUniqueId tPayrollRunId : pPayrollRunIds) {
			FailedPayrollRun tFailedPayrollRun = findFailedPayrollRun(tPayrollRunId);
			tFailedPayrollRun.setStatusToken(pSyncStatus);
			Application.save(tFailedPayrollRun);
		}
	}
}
