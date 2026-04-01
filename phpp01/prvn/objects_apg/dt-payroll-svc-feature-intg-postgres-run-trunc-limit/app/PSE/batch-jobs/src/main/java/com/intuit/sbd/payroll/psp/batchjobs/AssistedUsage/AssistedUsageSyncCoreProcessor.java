package com.intuit.sbd.payroll.psp.batchjobs.AssistedUsage;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.security.SystemPrincipal;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;

public class AssistedUsageSyncCoreProcessor implements Callable<HashSet<SpcfUniqueId>> {
    private static SpcfLogger logger = Application.getLogger(com.intuit.sbd.payroll.psp.batchjobs.AssistedUsage.AssistedUsageSyncCoreProcessor.class);
    private ArrayList<SpcfUniqueId> mPayrollRunIds;

    AssistedUsageSyncCoreProcessor(ArrayList<SpcfUniqueId> pPayrollRunIds) {
        mPayrollRunIds = pPayrollRunIds;
    }

    private boolean processSinglePayrollRun(SpcfUniqueId pPayrollRunId) {
        try {
            Application.beginUnitOfWork();
            PayrollRun payrollRun = PayrollRun.findPayrollRun(pPayrollRunId);
            Company company = payrollRun.getCompany();
            Entitlement entitlement = company.getActivePrimaryEntitlementUnit().getEntitlement();
            if (!entitlement.getEntitlementCode().isDiamondAssisted()) {
                logger.error(String.format("Failed to process Non Diamond Payroll Run %s", pPayrollRunId));
                return false;
            }

            AsstBundleCompUsage asstBundleCompUsage = AsstBundleCompUsage.findOrCreateAssistedBundleCompanyUsage(company.getSourceCompanyId(), company.getSourceSystemCd(), entitlement.getEntitlementOfferingCode(), entitlement.getLicenseNumber());
            AssistedBundleBill.createOrUpdateAssistedBundleBill(asstBundleCompUsage, payrollRun);
            Application.commitUnitOfWork();
            return true;
        } catch (Throwable t) {
            logger.error(String.format("Could not create assisted bundle bill for payroll ID: %s" + pPayrollRunId), t);
            return false;
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    // return all the processed payrollruns, including those moved to the failed table
    // these payrollruns may need a status update in the original source table such as PayrollRun and FailedPayrollRun
    public HashSet<SpcfUniqueId> call() throws Exception {
        PayrollServices.setCurrentPrincipal(new PspPrincipal(SystemPrincipal.AssistedUsageDataSyncProcess));

        HashSet<SpcfUniqueId> results = new HashSet<SpcfUniqueId>();
        for (SpcfUniqueId payrollRunId : mPayrollRunIds) {
            boolean resultSuccess = processSinglePayrollRun(payrollRunId);
            if (resultSuccess) {
                results.add(payrollRunId);
            }
        }
        return results;
    }
}