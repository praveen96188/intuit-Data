package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * DTO for the Direct Deposit Manager settings page;
 * uses SourcePayrollParameter to populate data
 */
public class SAPFraudSettings {
    private String fraudEEPaidMax;
    private String fraudEEPaidMaxXPayrolls;

    private String fraudEEAcctUpdateMax;
    private String fraudEEAcctUpdateXDays;

    private String fraudEERoundPaidXPayrolls;
    private String fraudEERoundPaidXAmount;
    private String fraudBPRoundPaidXPayrolls;
    private String fraudBPRoundPaidXAmount;
    private String fraudPRMax;
    private String fraudPRMaxXPayrolls;
    private String fraudEEPercentIncreaseMax;
    private String fraudEEPercentIncreaseMaxXPayrolls;
    private String fraudPRPercentIncreaseMax;
    private String fraudPRPercentIncreaseMaxXPayrolls;
    private String fraudPRNumberOfDaysForXPayrolls;
    private String fraudPRNumberOfPayrollsInXDays;
    private String fraudPRXPayrollAmount;

    private String fraudEEPaidXTimes;
    private String fraudEENumberOfDaysMultiplePaychecks;
    private String fraudEENewEmployeeAddedXDays;
    private String fraudEEPercentGreaterThanOtherEEs;
    private String fraudEENumberOfPaychecksSpikeInPay;
    private String fraudEEPercentGreaterThanAverage;
    private String fraudEENumberOfDaysBankAcctUpdated;
    private String fraudPRNumberOfPayrollsToCheckSameBank;
    private String fraudPRPercentEmployeesPaidSameBank;
    private String fraudPRTotalEmployeesToCheckSameBank;
    private String fraudPREmployeesSameBankAccountMax;
    private String fraudDDInactivityDays;
    private String fraudDDInactivityPayrollAmount;

    private String fraudPayeePaidMax;
    private String fraudPayeePaidMaxXPayrolls;

    private String fraudBPAcctUpdateMax;
    private String fraudBPAcctUpdateXDays;

    private String fraudBPMax;
    private String fraudBPMaxXPayrolls;
    private String fraudPayeePaidXTimes;
    private String fraudPayeeNumberOfDaysMultiplePayments;
    private String fraudBPInactivityDays;
    private String fraudBPInactivityPayrollAmount;

    private String fraudBPNumberOfDaysForXPayments;
    private String fraudBPNumberOfPaymentsInXDays;
    private String fraudBPXPayrollAmount;

    private String fraudBPNumberOfPaymentsToCheckSameBank;
    private String fraudBPPercentPayeesPaidSameBank;
    private String fraudBPTotalPayeesToCheckSameBank;




    public String getFraudEEPaidMax() {
        return fraudEEPaidMax;
    }

    public void setFraudEEPaidMax(String fraudEEPaidMax) {
        this.fraudEEPaidMax = fraudEEPaidMax;
    }

    public String getFraudEEPaidMaxXPayrolls() {
        return fraudEEPaidMaxXPayrolls;
    }

    public void setFraudEEPaidMaxXPayrolls(String fraudEEPaidMaxXPayrolls) {
        this.fraudEEPaidMaxXPayrolls = fraudEEPaidMaxXPayrolls;
    }

    public String getFraudEEAcctUpdateMax() {
        return fraudEEAcctUpdateMax;
    }

    public void setFraudEEAcctUpdateMax(String fraudEEAcctUpdateMax) {
        this.fraudEEAcctUpdateMax = fraudEEAcctUpdateMax;
    }

    public String getFraudEEAcctUpdateXDays() {
        return fraudEEAcctUpdateXDays;
    }

    public void setFraudEEAcctUpdateXDays(String fraudEEAcctUpdateXDays) {
        this.fraudEEAcctUpdateXDays = fraudEEAcctUpdateXDays;
    }

    public String getFraudEERoundPaidXPayrolls() {
        return fraudEERoundPaidXPayrolls;
    }

    public void setFraudEERoundPaidXPayrolls(String fraudEERoundPaidXPayrolls) {
        this.fraudEERoundPaidXPayrolls = fraudEERoundPaidXPayrolls;
    }

    public String getFraudEERoundPaidXAmount() {
        return fraudEERoundPaidXAmount;
    }

    public void setFraudEERoundPaidXAmount(String fraudEERoundPaidXAmount) {
        this.fraudEERoundPaidXAmount = fraudEERoundPaidXAmount;
    }

    public String getFraudBPRoundPaidXAmount() {
        return fraudBPRoundPaidXAmount;
    }

    public void setFraudBPRoundPaidXAmount(String fraudBPRoundPaidXAmount) {
        this.fraudBPRoundPaidXAmount = fraudBPRoundPaidXAmount;
    }
    public String getFraudPRMax() {
        return fraudPRMax;
    }

    public void setFraudPRMax(String fraudPRMax) {
        this.fraudPRMax = fraudPRMax;
    }

    public String getFraudPRMaxXPayrolls() {
        return fraudPRMaxXPayrolls;
    }

    public void setFraudPRMaxXPayrolls(String fraudPRMaxXPayrolls) {
        this.fraudPRMaxXPayrolls = fraudPRMaxXPayrolls;
    }

    public String getFraudEEPercentIncreaseMax() {
        return fraudEEPercentIncreaseMax;
    }

    public void setFraudEEPercentIncreaseMax(String fraudEEPercentIncreaseMax) {
        this.fraudEEPercentIncreaseMax = fraudEEPercentIncreaseMax;
    }

    public String getFraudEEPercentIncreaseMaxXPayrolls() {
        return fraudEEPercentIncreaseMaxXPayrolls;
    }

    public void setFraudEEPercentIncreaseMaxXPayrolls(String fraudEEPercentIncreaseMaxXPayrolls) {
        this.fraudEEPercentIncreaseMaxXPayrolls = fraudEEPercentIncreaseMaxXPayrolls;
    }
    public String getFraudPRXPayrollAmount() {
        return fraudPRXPayrollAmount;
    }

    public void setFraudPRXPayrollAmount(String fraudPRXPayrollAmount) {
        this.fraudPRXPayrollAmount = fraudPRXPayrollAmount;
    }

    public String getFraudBPXPayrollAmount() {
        return fraudBPXPayrollAmount;
    }

    public void setFraudBPXPayrollAmount(String fraudBPXPayrollAmount) {
        this.fraudBPXPayrollAmount = fraudBPXPayrollAmount;
    }

    public String getFraudPRPercentIncreaseMax() {
        return fraudPRPercentIncreaseMax;
    }

    public void setFraudPRPercentIncreaseMax(String fraudPRPercentIncreaseMax) {
        this.fraudPRPercentIncreaseMax = fraudPRPercentIncreaseMax;
    }

    public String getFraudPRPercentIncreaseMaxXPayrolls() {
        return fraudPRPercentIncreaseMaxXPayrolls;
    }

    public void setFraudPRPercentIncreaseMaxXPayrolls(String fraudPRPercentIncreaseMaxXPayrolls) {
        this.fraudPRPercentIncreaseMaxXPayrolls = fraudPRPercentIncreaseMaxXPayrolls;
    }

    public String getFraudPRNumberOfDaysForXPayrolls() {
        return fraudPRNumberOfDaysForXPayrolls;
    }

    public void setFraudPRNumberOfDaysForXPayrolls(String fraudPRNumberOfDaysForXPayrolls) {
        this.fraudPRNumberOfDaysForXPayrolls = fraudPRNumberOfDaysForXPayrolls;
    }

    public String getFraudPRNumberOfPayrollsInXDays() {
        return fraudPRNumberOfPayrollsInXDays;
    }

    public void setFraudPRNumberOfPayrollsInXDays(String fraudPRNumberOfPayrollsInXDays) {
        this.fraudPRNumberOfPayrollsInXDays = fraudPRNumberOfPayrollsInXDays;
    }

    public String getFraudEEPaidXTimes() {
        return fraudEEPaidXTimes;
    }

    public void setFraudEEPaidXTimes(String fraudEEPaidXTimes) {
        this.fraudEEPaidXTimes = fraudEEPaidXTimes;
    }

    public String getFraudEENumberOfDaysMultiplePaychecks() {
        return fraudEENumberOfDaysMultiplePaychecks;
    }

    public void setFraudEENumberOfDaysMultiplePaychecks(String fraudEENumberOfDaysMultiplePaychecks) {
        this.fraudEENumberOfDaysMultiplePaychecks = fraudEENumberOfDaysMultiplePaychecks;
    }

    public String getFraudEENewEmployeeAddedXDays() {
        return fraudEENewEmployeeAddedXDays;
    }

    public void setFraudEENewEmployeeAddedXDays(String fraudEENewEmployeeAddedXDays) {
        this.fraudEENewEmployeeAddedXDays = fraudEENewEmployeeAddedXDays;
    }

    public String getFraudEEPercentGreaterThanOtherEEs() {
        return fraudEEPercentGreaterThanOtherEEs;
    }

    public void setFraudEEPercentGreaterThanOtherEEs(String fraudEEPercentGreaterThanOtherEEs) {
        this.fraudEEPercentGreaterThanOtherEEs = fraudEEPercentGreaterThanOtherEEs;
    }

    public String getFraudEENumberOfPaychecksSpikeInPay() {
        return fraudEENumberOfPaychecksSpikeInPay;
    }

    public void setFraudEENumberOfPaychecksSpikeInPay(String fraudEENumberOfPaychecksSpikeInPay) {
        this.fraudEENumberOfPaychecksSpikeInPay = fraudEENumberOfPaychecksSpikeInPay;
    }

    public String getFraudEEPercentGreaterThanAverage() {
        return fraudEEPercentGreaterThanAverage;
    }

    public void setFraudEEPercentGreaterThanAverage(String fraudEEPercentGreaterThanAverage) {
        this.fraudEEPercentGreaterThanAverage = fraudEEPercentGreaterThanAverage;
    }

    public String getFraudEENumberOfDaysBankAcctUpdated() {
        return fraudEENumberOfDaysBankAcctUpdated;
    }

    public void setFraudEENumberOfDaysBankAcctUpdated(String fraudEENumberOfDaysBankAcctUpdated) {
        this.fraudEENumberOfDaysBankAcctUpdated = fraudEENumberOfDaysBankAcctUpdated;
    }

    public String getFraudPRNumberOfPayrollsToCheckSameBank() {
        return fraudPRNumberOfPayrollsToCheckSameBank;
    }

    public void setFraudPRNumberOfPayrollsToCheckSameBank(String fraudPRNumberOfPayrollsToCheckSameBank) {
        this.fraudPRNumberOfPayrollsToCheckSameBank = fraudPRNumberOfPayrollsToCheckSameBank;
    }

    public String getFraudPRPercentEmployeesPaidSameBank() {
        return fraudPRPercentEmployeesPaidSameBank;
    }

    public void setFraudPRPercentEmployeesPaidSameBank(String fraudPRPercentEmployeesPaidSameBank) {
        this.fraudPRPercentEmployeesPaidSameBank = fraudPRPercentEmployeesPaidSameBank;
    }

    public String getFraudPRTotalEmployeesToCheckSameBank() {
        return fraudPRTotalEmployeesToCheckSameBank;
    }

    public void setFraudPRTotalEmployeesToCheckSameBank(String fraudPRTotalEmployeesToCheckSameBank) {
        this.fraudPRTotalEmployeesToCheckSameBank = fraudPRTotalEmployeesToCheckSameBank;
    }

    public String getFraudPREmployeesSameBankAccountMax() {
        return fraudPREmployeesSameBankAccountMax;
    }

    public void setFraudPREmployeesSameBankAccountMax(String fraudPREmployeesSameBankAccountMax) {
        this.fraudPREmployeesSameBankAccountMax = fraudPREmployeesSameBankAccountMax;
    }

    public String getFraudDDInactivityDays() {
        return fraudDDInactivityDays;
    }

    public void setFraudDDInactivityDays(String pFraudDDInactivityDays) {
        fraudDDInactivityDays = pFraudDDInactivityDays;
    }

    public String getFraudDDInactivityPayrollAmount() {
        return fraudDDInactivityPayrollAmount;
    }

    public void setFraudDDInactivityPayrollAmount(String pFraudDDInactivityPayrollAmount) {
        fraudDDInactivityPayrollAmount = pFraudDDInactivityPayrollAmount;
    }

    public String getFraudPayeePaidMax() {
        return fraudPayeePaidMax;
    }

    public void setFraudPayeePaidMax(String pFraudPayeePaidMax) {
        fraudPayeePaidMax = pFraudPayeePaidMax;
    }

    public String getFraudPayeePaidMaxXPayrolls() {
        return fraudPayeePaidMaxXPayrolls;
    }

    public void setFraudPayeePaidMaxXPayrolls(String pFraudPayeePaidMaxXPayrolls) {
        fraudPayeePaidMaxXPayrolls = pFraudPayeePaidMaxXPayrolls;
    }

    public String getFraudBPAcctUpdateMax() {
        return fraudBPAcctUpdateMax;
    }

    public void setFraudBPAcctUpdateMax(String fraudBPAcctUpdateMax) {
        this.fraudBPAcctUpdateMax = fraudBPAcctUpdateMax;
    }

    public String getFraudBPAcctUpdateXDays() {
        return fraudBPAcctUpdateXDays;
    }

    public void setFraudBPAcctUpdateXDays(String fraudBPAcctUpdateXDays) {
        this.fraudBPAcctUpdateXDays = fraudBPAcctUpdateXDays;
    }

    public String getFraudBPMax() {
        return fraudBPMax;
    }

    public void setFraudBPMax(String pFraudBPMax) {
        fraudBPMax = pFraudBPMax;
    }

    public String getFraudBPMaxXPayrolls() {
        return fraudBPMaxXPayrolls;
    }

    public void setFraudBPMaxXPayrolls(String pFraudBPMaxXPayrolls) {
        fraudBPMaxXPayrolls = pFraudBPMaxXPayrolls;
    }

    public String getFraudPayeePaidXTimes() {
        return fraudPayeePaidXTimes;
    }

    public void setFraudPayeePaidXTimes(String pFraudPayeePaidXTimes) {
        fraudPayeePaidXTimes = pFraudPayeePaidXTimes;
    }

    public String getFraudPayeeNumberOfDaysMultiplePayments() {
        return fraudPayeeNumberOfDaysMultiplePayments;
    }

    public void setFraudPayeeNumberOfDaysMultiplePayments(String pFraudPayeeNumberOfDaysMultiplePayments) {
        fraudPayeeNumberOfDaysMultiplePayments = pFraudPayeeNumberOfDaysMultiplePayments;
    }

    public String getFraudBPInactivityDays() {
        return fraudBPInactivityDays;
    }

    public void setFraudBPInactivityDays(String pFraudBPInactivityDays) {
        fraudBPInactivityDays = pFraudBPInactivityDays;
    }

    public String getFraudBPInactivityPayrollAmount() {
        return fraudBPInactivityPayrollAmount;
    }

    public void setFraudBPInactivityPayrollAmount(String pFraudBPInactivityPayrollAmount) {
        fraudBPInactivityPayrollAmount = pFraudBPInactivityPayrollAmount;
    }

    public String getFraudBPNumberOfDaysForXPayments() {
        return fraudBPNumberOfDaysForXPayments;
    }

    public void setFraudBPNumberOfDaysForXPayments(String pFraudBPNumberOfDaysForXPayments) {
        fraudBPNumberOfDaysForXPayments = pFraudBPNumberOfDaysForXPayments;
    }

    public String getFraudBPNumberOfPaymentsInXDays() {
        return fraudBPNumberOfPaymentsInXDays;
    }

    public void setFraudBPNumberOfPaymentsInXDays(String pFraudBPNumberOfPaymentsInXDays) {
        fraudBPNumberOfPaymentsInXDays = pFraudBPNumberOfPaymentsInXDays;
    }

    public String getFraudBPRoundPaidXPayrolls() {
        return fraudBPRoundPaidXPayrolls;
    }

    public void setFraudBPRoundPaidXPayrolls(String pFraudBPRoundPaidXPayrolls) {
        fraudBPRoundPaidXPayrolls = pFraudBPRoundPaidXPayrolls;
    }

    public String getFraudBPNumberOfPaymentsToCheckSameBank() {
        return fraudBPNumberOfPaymentsToCheckSameBank;
    }

    public void setFraudBPNumberOfPaymentsToCheckSameBank(String pFraudBPNumberOfPaymentsToCheckSameBank) {
        fraudBPNumberOfPaymentsToCheckSameBank = pFraudBPNumberOfPaymentsToCheckSameBank;
    }

    public String getFraudBPPercentPayeesPaidSameBank() {
        return fraudBPPercentPayeesPaidSameBank;
    }

    public void setFraudBPPercentPayeesPaidSameBank(String pFraudBPPercentPayeesPaidSameBank) {
        fraudBPPercentPayeesPaidSameBank = pFraudBPPercentPayeesPaidSameBank;
    }

    public String getFraudBPTotalPayeesToCheckSameBank() {
        return fraudBPTotalPayeesToCheckSameBank;
    }

    public void setFraudBPTotalPayeesToCheckSameBank(String pFraudBPTotalPayeesToCheckSameBank) {
        fraudBPTotalPayeesToCheckSameBank = pFraudBPTotalPayeesToCheckSameBank;
    }
}