package com.intuit.sbd.payroll.psp.util;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.FinancialTransaction;
import com.intuit.sbd.payroll.psp.domain.SettlementType;
import com.intuit.sbd.payroll.psp.exceptions.MoneyMovementControlException;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlagLazyLoader;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class MoneyMovementControlUtil {

    private static SpcfLogger logger = SpcfLogManager.getLogger(MoneyMovementControlUtil.class);
    public static long MAX_FINANCIAL_TRANSACTION_LIMIT = 15_000_000L;
    public static final long DEFAULT_FINANCIAL_TRANSACTION_LIMIT = 10_000_000L;
    public static final Set<SettlementType> TRACKED_SETTLEMENT_TYPE_CODE_LIST = new HashSet<>(Arrays.asList(new SettlementType[]{SettlementType.ACH, SettlementType.EFTPS, SettlementType.EFTPSDirectDebit}));

    // This is introduced only for certain integration tests only
    private static boolean skipValidation = false;

    public static void validateFinancialTransaction(FinancialTransaction ft) {
        PspPrincipal principal = Application.getCurrentPrincipal();
        boolean isFTLimitEnabled = isFTLimitEnabled();
        long ftLimit = ((Double) FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.FINANCIAL_TRANSACTION_LIMIT)).longValue();
        long ftAmount = ft.getFinancialTransactionAmount().getIntegerPart();

        logAgentInititatedFT(ftLimit, ftAmount, ft, principal);
        checkFT(isFTLimitEnabled, ftLimit, ftAmount, ft, principal);
    }

    private static boolean isFTLimitEnabled() {
        return skipValidation ? !skipValidation
                : (Boolean) FeatureFlagLazyLoader.getInstance().getFeatureFlagValue(FeatureFlags.Key.ENABLE_FINANCIAL_TRANSACTION_LIMIT);
    }

    private static void logAgentInititatedFT(long ftLimit, long ftAmount, FinancialTransaction ft, PspPrincipal principal) {
        if(principal.isAgent()) {
            logFTDetails(ftLimit, ftAmount, ft, principal, "AgentInitiatedFT", "Agent Initiated Financial Transaction");
        }
    }

    private static void checkFT(boolean isFTLimitEnabled, long ftLimit, long ftAmount, FinancialTransaction ft, PspPrincipal principal) {
        if(isFTLimitEnabled
                && TRACKED_SETTLEMENT_TYPE_CODE_LIST.contains(ft.getSettlementTypeCd())
                && (ftAmount > ftLimit || ftAmount > MAX_FINANCIAL_TRANSACTION_LIMIT)) {
            logFTDetails( ftLimit, ftAmount, ft, principal, "FTAmountBreached", "Financial Transaction Amount Breached the Threshold Amount");
            throw new MoneyMovementControlException(String.format("Financial Transaction Amount %s Breached The Allowed Threshold Amount. psid=%s", ftAmount, ft.getCompany().getSourceCompanyId()));
        }
    }

    private static void logFTDetails(long ftLimit, long ftAmount, FinancialTransaction ft, PspPrincipal principal, String action, String message){
        String originalFtId = Objects.isNull(ft.getOriginalTransaction()) ? "NONE" : ft.getOriginalTransaction().getId().toString();
        logger.info(String.format("Action=%s, Msg=%s," +
                        " psid=%s, ftAmount=%s, ftLimit=%s, maxFTLimit=%s, settlementTypeCd=%s, transactionTypeCd=%s, ftId=%s, isAgent=%s, userId=%s, userName=%s, ftStatus=%s, original_ftId=%s",
                action, message, ft.getCompany().getSourceCompanyId(), ftAmount, ftLimit, MAX_FINANCIAL_TRANSACTION_LIMIT, ft.getSettlementTypeCd()
                , ft.getTransactionType().getTransactionTypeCd(), ft.getId(), principal.isAgent(), principal.getId(), principal.getName(), ft.getCurrentTransactionState().getTransactionStateCd(),
                originalFtId));
    }

    public static void setSkipValidation(boolean skipValidation) {
        MoneyMovementControlUtil.skipValidation = skipValidation;
    }

}
