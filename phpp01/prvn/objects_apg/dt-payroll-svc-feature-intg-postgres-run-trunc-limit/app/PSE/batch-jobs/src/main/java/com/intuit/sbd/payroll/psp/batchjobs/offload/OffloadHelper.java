package com.intuit.sbd.payroll.psp.batchjobs.offload;

import java.util.ArrayList;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

public class OffloadHelper {

    private static final String COUNT_EMPTY_EDR_SETTLEMENT_DATE_QUERY = "findEmptySettlementDateEDRCount";
    private static final String PARAM_INITIATION_DATE = "initiationDate";
    private static final SpcfLogger LOGGER = Application.getLogger(OffloadACHTransactions.class);

    public boolean isPayrollRunLevelFundingModel(SpcfCalendar initiationDate) {
        boolean enablePayrollRunLevelFundingModel = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_PAYROLL_RUN_LEVEL_FUNDING_MODEL, false);
        if (!enablePayrollRunLevelFundingModel) {
            LOGGER.info("ENABLE_PAYROLL_RUN_LEVEL_FUNDING_MODEL flag is off");
            return false;
        }

        return allEDRsHaveSettlementDate(initiationDate);
    }

    private boolean allEDRsHaveSettlementDate(SpcfCalendar initiationDate) {
        String[] paramName = new String[1];
        paramName[0] = PARAM_INITIATION_DATE;
        Object[] paramValue = new Object[1];
        paramValue[0] = initiationDate;

        ArrayList<Object> result = Application.executeNamedQuery(COUNT_EMPTY_EDR_SETTLEMENT_DATE_QUERY, paramName, paramValue);
        long emptySettlementDateEDRCount = (Long) result.iterator().next();
        LOGGER.info(emptySettlementDateEDRCount + " CCD/PPD EDRs have empty settlement date. Initiation Date=" + initiationDate);
        return emptySettlementDateEDRCount == 0 ? true : false;
    }
}
