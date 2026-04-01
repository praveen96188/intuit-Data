package com.intuit.sbd.payroll.psp.batchjobs.AssistedUsage;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portabilitySpecific.SpcfUniqueIdImpl;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AssistedUsagePayrollRunHelper {
    private static SpcfLogger logger = Application.getLogger(com.intuit.sbd.payroll.psp.batchjobs.AssistedUsage.AssistedUsagePayrollRunHelper.class);
    public static long findNextSetPayrollRunsToProcess(HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> pPayrollRunsToProcess) throws Exception {
        long oldToken = -1;
        long newToken = -1;
        pPayrollRunsToProcess.clear();

        try {
            Application.beginUnitOfWork();

            oldToken = SystemParameter.findLongValue(SystemParameter.Code.ASSISTED_USAGE_BILLING_TOKEN);

            logger.info(String.format("Current ASSISTED_USAGE_BILLING_TOKEN: %d", oldToken));

            List<Object[]> companyAndPayrollRunIds = Application.executeNamedQuery("findPayrollRunsForAssistedBillingProcess", new String[]{"lastToken"}, new Object[]{oldToken});
            pPayrollRunsToProcess.putAll(groupPayrollRunIdsByCompany(companyAndPayrollRunIds));

            if (!companyAndPayrollRunIds.isEmpty()) {
                Object[] lastRow = companyAndPayrollRunIds.get(companyAndPayrollRunIds.size() - 1);
                newToken = Long.parseLong(lastRow[2].toString());
            }

            logger.info(String.format("fetched %d payroll runs. The new token is %d", companyAndPayrollRunIds.size(), newToken));
            return newToken;
        } catch (Throwable t) {
            logger.fatal("Exception in SyncPSPToAssistedUsage.findNextSetPayrollRunsToProcess(). Started from old token " + oldToken, t);
            throw new RuntimeException(t);
        } finally {
            Application.rollbackUnitOfWork();
        }
    }

    protected static HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> groupPayrollRunIdsByCompany(List<Object[]> pIds) {
        HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>> companyAndPayRunIds = new HashMap<SpcfUniqueId, ArrayList<SpcfUniqueId>>();
        for (Object[] row : pIds) {
            SpcfUniqueId companyId = new SpcfUniqueIdImpl(row[0].toString());
            SpcfUniqueId payrunId = new SpcfUniqueIdImpl(row[1].toString());

            ArrayList<SpcfUniqueId> payrollRunIds = companyAndPayRunIds.get(companyId);
            if (payrollRunIds == null) {
                payrollRunIds = new ArrayList<SpcfUniqueId>();
                companyAndPayRunIds.put(companyId, payrollRunIds);
            }
            payrollRunIds.add(payrunId);
        }
        return companyAndPayRunIds;
    }
}
