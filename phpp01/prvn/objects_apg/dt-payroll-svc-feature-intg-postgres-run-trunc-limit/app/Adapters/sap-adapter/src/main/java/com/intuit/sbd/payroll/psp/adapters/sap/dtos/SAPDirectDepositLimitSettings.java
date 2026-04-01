package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.List;

/**
 * DTO for the Direct Deposit Manager settings page;
 * uses SourcePayrollParameter to populate data
 */
public class SAPDirectDepositLimitSettings {
    // per company DD limit
    private String defaultDDCompanyLimit;
    // company limit cannot be exceeded in
    private String DDCompanyLimitDuration;
    // per employee DD limit
    private String defaultDDEmployeeLimit;

    private String defaultBPCompanyLimit;
    private String defaultBPPayeeLimit;

    // employee limit cannot be exceeded in
    private String DDEmployeeLimitDuration;
    // max violations before suspension
    private String consecutiveLimitViolationLimit;
    // verify bank account retry limit max attempts
    private String companyBankAccountVerificationAttemptLimit;
    // account must be verified in
    private String companyBankAccountDurationLimitForVerification;
    // minimum payroll amount
    private String minimumNonSuspectPayrollAmount;
    // maximum payroll amount
    private String maxDDCompanyLimitDefault;

    private List<SAPAutoLimitIncreaseTier> autoLimitIncreaseTiers;

    public String getDefaultDDCompanyLimit() {
        return defaultDDCompanyLimit;
    }

    public void setDefaultDDCompanyLimit(String defaultDDCompanyLimit) {
        this.defaultDDCompanyLimit = defaultDDCompanyLimit;
    }

    public String getDDCompanyLimitDuration() {
        return DDCompanyLimitDuration;
    }

    public void setDDCompanyLimitDuration(String DDCompanyLimitDuration) {
        this.DDCompanyLimitDuration = DDCompanyLimitDuration;
    }

    public String getDefaultDDEmployeeLimit() {
        return defaultDDEmployeeLimit;
    }

    public void setDefaultDDEmployeeLimit(String defaultDDEmployeeLimit) {
        this.defaultDDEmployeeLimit = defaultDDEmployeeLimit;
    }

    public String getDefaultBPCompanyLimit() {
        return defaultBPCompanyLimit;
    }

    public void setDefaultBPCompanyLimit(String pDefaultBPCompanyLimit) {
        defaultBPCompanyLimit = pDefaultBPCompanyLimit;
    }

    public String getDefaultBPPayeeLimit() {
        return defaultBPPayeeLimit;
    }

    public void setDefaultBPPayeeLimit(String pDefaultBPPayeeLimit) {
        defaultBPPayeeLimit = pDefaultBPPayeeLimit;
    }

    public String getDDEmployeeLimitDuration() {
        return DDEmployeeLimitDuration;
    }

    public void setDDEmployeeLimitDuration(String DDEmployeeLimitDuration) {
        this.DDEmployeeLimitDuration = DDEmployeeLimitDuration;
    }

    public String getConsecutiveLimitViolationLimit() {
        return consecutiveLimitViolationLimit;
    }

    public void setConsecutiveLimitViolationLimit(String consecutiveLimitViolationLimit) {
        this.consecutiveLimitViolationLimit = consecutiveLimitViolationLimit;
    }

    public String getCompanyBankAccountVerificationAttemptLimit() {
        return companyBankAccountVerificationAttemptLimit;
    }

    public void setCompanyBankAccountVerificationAttemptLimit(String companyBankAccountVerificationAttemptLimit) {
        this.companyBankAccountVerificationAttemptLimit = companyBankAccountVerificationAttemptLimit;
    }

    public String getCompanyBankAccountDurationLimitForVerification() {
        return companyBankAccountDurationLimitForVerification;
    }

    public void setCompanyBankAccountDurationLimitForVerification(String companyBankAccountDurationLimitForVerification) {
        this.companyBankAccountDurationLimitForVerification = companyBankAccountDurationLimitForVerification;
    }

    public String getMinimumNonSuspectPayrollAmount() {
        return minimumNonSuspectPayrollAmount;
    }

    public void setMinimumNonSuspectPayrollAmount(String minimumNonSuspectPayrollAmount) {
        this.minimumNonSuspectPayrollAmount = minimumNonSuspectPayrollAmount;
    }

    public String getMaxDDCompanyLimitDefault() {
        return maxDDCompanyLimitDefault;
    }

    public void setMaxDDCompanyLimitDefault(String maxDDCompanyLimitDefault) {
        this.maxDDCompanyLimitDefault = maxDDCompanyLimitDefault;
    }

    public List<SAPAutoLimitIncreaseTier> getAutoLimitIncreaseTiers() {
        return autoLimitIncreaseTiers;
    }

    public void setAutoLimitIncreaseTiers(List<SAPAutoLimitIncreaseTier> autoLimitIncreaseTiers) {
        this.autoLimitIncreaseTiers = autoLimitIncreaseTiers;
    }
}
