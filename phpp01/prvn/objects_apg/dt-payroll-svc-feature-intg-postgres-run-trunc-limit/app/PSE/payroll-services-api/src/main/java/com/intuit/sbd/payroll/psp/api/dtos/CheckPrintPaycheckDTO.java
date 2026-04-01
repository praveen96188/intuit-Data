package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Jan 18, 2010
 * Time: 11:43:32 AM
 * To change this template use File | Settings | File Templates.
 */
public class CheckPrintPaycheckDTO {
    private static SpcfLogger logger = Application.getLogger(CheckPrintPaycheckDTO.class);

    private Boolean isTestCheck = false;

    /* Employee information */
    private String employeeId;
    private String employeePrintName;
    private String employeePaySchedule;
    private CheckPrintAddressDTO employeeAddress;
    private String employeeFedFilingStatus;
    private int employeeFedAllowances;
    private String employeeStateFilingStatus;
    private int employeeStateAllowances;
    private String employeeWorkState;

    /* Paycheck information */
    private PaycheckType checkType;
    private String checkNumber;
    private DateDTO checkDate;
    private DateDTO periodStartDate;
    private DateDTO periodEndDate;

    /* Totals */
    private BigDecimal checkNetPay = ZERO_BIGDECIMAL;
    private BigDecimal ytdNetPay;
    private BigDecimal earningsCheckTotal;
    private BigDecimal earningsYtdTotal;
    private BigDecimal preTaxDeductionsCheckTotal;
    private BigDecimal preTaxDeductionsYtdTotal;
    private BigDecimal adjustedEarningsCheckTotal;
    private BigDecimal adjustedEarningsYtdTotal;
    private BigDecimal taxesCheckTotal;
    private BigDecimal taxesYtdTotal;
    private BigDecimal deductionsCheckTotal;
    private BigDecimal deductionsYtdTotal;
    private BigDecimal companyContributionsCheckTotal;
    private BigDecimal companyContributionsYtdTotal;
    private BigDecimal companyTaxableContributionsCheckTotal;
    private BigDecimal companyTaxableContributionsYtdTotal;


    /* Earnings, Deductions, PreTax Deductions and Taxes */
    private Set<CheckPrintPaycheckEarningLineDTO> earnings = new TreeSet<CheckPrintPaycheckEarningLineDTO>();
    private Set<CheckPrintPaycheckLineDTO> Deductions = new TreeSet<CheckPrintPaycheckLineDTO>();
    private Set<CheckPrintPaycheckLineDTO> preTaxDeductions = new TreeSet<CheckPrintPaycheckLineDTO>();
    private Set<CheckPrintPaycheckLineDTO> Taxes = new TreeSet<CheckPrintPaycheckLineDTO>();
    private Set<CheckPrintPaycheckLineDTO> CompanyTaxes = new TreeSet<CheckPrintPaycheckLineDTO>();
    private Set<CheckPrintPaycheckLineDTO> CompanyContributions = new TreeSet<CheckPrintPaycheckLineDTO>();
    private Set<CheckPrintPaycheckLineDTO> CompanyTaxableContributions = new TreeSet<CheckPrintPaycheckLineDTO>();

    /* Direct deposit information (if PaycheckType is DirectDeposit) */
    private List<CheckPrintPaycheckDDINfo> directDeposits = new ArrayList<CheckPrintPaycheckDDINfo>();
    private boolean isVoided;
    private long paycheckId;
    private static final BigDecimal ZERO_BIGDECIMAL = new BigDecimal(0.0);

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeePrintName() {
        return employeePrintName;
    }

    public void setEmployeePrintName(String employeePrintName) {
        this.employeePrintName = employeePrintName;
    }

    public String getEmployeePaySchedule() {
        return employeePaySchedule;
    }

    public void setEmployeePaySchedule(String employeePaySchedule) {
        this.employeePaySchedule = employeePaySchedule;
    }

    public CheckPrintAddressDTO getEmployeeAddress() {
        return employeeAddress;
    }

    public void setEmployeeAddress(CheckPrintAddressDTO employeeAddress) {
        this.employeeAddress = employeeAddress;
    }

    public String getEmployeeFedFilingStatus() {
        return employeeFedFilingStatus;
    }

    public void setEmployeeFedFilingStatus(String employeeFedFilingStatus) {
        this.employeeFedFilingStatus = employeeFedFilingStatus;
    }

    public int getEmployeeFedAllowances() {
        return employeeFedAllowances;
    }

    public void setEmployeeFedAllowances(int employeeFedAllowances) {
        this.employeeFedAllowances = employeeFedAllowances;
    }

    public String getEmployeeStateFilingStatus() {
        return employeeStateFilingStatus;
    }

    public void setEmployeeStateFilingStatus(String employeeStateFilingStatus) {
        this.employeeStateFilingStatus = employeeStateFilingStatus;
    }

    public int getEmployeeStateAllowances() {
        return employeeStateAllowances;
    }

    public void setEmployeeStateAllowances(int employeeStateAllowances) {
        this.employeeStateAllowances = employeeStateAllowances;
    }

    public String getEmployeeWorkState() {
        return employeeWorkState;
    }

    public void setEmployeeWorkState(String employeeWorkState) {
        this.employeeWorkState = employeeWorkState;
    }

    public PaycheckType getCheckType() {
        return checkType;
    }

    public void setCheckType(PaycheckType checkType) {
        this.checkType = checkType;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public DateDTO getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(DateDTO checkDate) {
        this.checkDate = checkDate;
    }

    public DateDTO getPeriodStartDate() {
        return periodStartDate;
    }

    public void setPeriodStartDate(DateDTO periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public DateDTO getPeriodEndDate() {
        return periodEndDate;
    }

    public void setPeriodEndDate(DateDTO periodEndDate) {
        this.periodEndDate = periodEndDate;
    }

    public BigDecimal getCheckNetPay() {
        return checkNetPay;
    }

    public BigDecimal getYtdNetPay() {
        return ytdNetPay;
    }

    public BigDecimal getEarningsCheckTotal() {
        return earningsCheckTotal;
    }

    public BigDecimal getEarningsYtdTotal() {
        return earningsYtdTotal;
    }

    public BigDecimal getPreTaxDeductionsCheckTotal() {
        return preTaxDeductionsCheckTotal;
    }

    public BigDecimal getPreTaxDeductionsYtdTotal() {
        return preTaxDeductionsYtdTotal;
    }

    public BigDecimal getTaxesCheckTotal() {
        return taxesCheckTotal;
    }

    public BigDecimal getTaxesYtdTotal() {
        return taxesYtdTotal;
    }

    public BigDecimal getDeductionsCheckTotal() {
        return deductionsCheckTotal;
    }

    public BigDecimal getDeductionsYtdTotal() {
        return deductionsYtdTotal;
    }

    public Set<CheckPrintPaycheckEarningLineDTO> getEarnings() {
        return earnings;
    }

    public void setEarnings(Set<CheckPrintPaycheckEarningLineDTO> earnings) {
        this.earnings = earnings;
    }

    public Set<CheckPrintPaycheckLineDTO> getDeductions() {
        return Deductions;
    }

    public void setDeductions(Set<CheckPrintPaycheckLineDTO> deductions) {
        Deductions = deductions;
    }

    public Set<CheckPrintPaycheckLineDTO> getPreTaxDeductions() {
        return preTaxDeductions;
    }

    public void setPreTaxDeductions(Set<CheckPrintPaycheckLineDTO> preTaxDeductions) {
        this.preTaxDeductions = preTaxDeductions;
    }

    public Set<CheckPrintPaycheckLineDTO> getTaxes() {
        return Taxes;
    }

    public void setTaxes(Set<CheckPrintPaycheckLineDTO> taxes) {
        Taxes = taxes;
    }

    public Set<CheckPrintPaycheckLineDTO> getCompanyTaxes() {
        return CompanyTaxes;
    }

    public void setCompanyTaxes(Set<CheckPrintPaycheckLineDTO> companyTaxes) {
        CompanyTaxes = companyTaxes;
    }

    public BigDecimal getAdjustedEarningsCheckTotal() {
        return adjustedEarningsCheckTotal;
    }

    public BigDecimal getAdjustedEarningsYtdTotal() {
        return adjustedEarningsYtdTotal;
    }

    public List<CheckPrintPaycheckDDINfo> getDirectDeposits() {
        return directDeposits;
    }

    public void setDirectDeposits(List<CheckPrintPaycheckDDINfo> directDeposits) {
        this.directDeposits = directDeposits;
    }

    public boolean getIsVoided() {
        return isVoided;
    }

    public void setIsVoided(boolean voided) {
        this.isVoided = voided;
    }

    public long getPaycheckId() {
        return paycheckId;
    }

    public void setPaycheckId(long paycheckId) {
        this.paycheckId = paycheckId;
    }

    public void setCheckNetPay(BigDecimal checkNetPay) {
        this.checkNetPay = checkNetPay;
    }

    public Boolean getIsTestCheck() {
        return isTestCheck;
    }

    public void setIsTestCheck(Boolean testCheck) {
        isTestCheck = testCheck;
    }

    public Set<CheckPrintPaycheckLineDTO> getCompanyContributions() {
        return CompanyContributions;
    }

    public void setCompanyContributions(Set<CheckPrintPaycheckLineDTO> companyContributions) {
        CompanyContributions = companyContributions;
    }

    public Set<CheckPrintPaycheckLineDTO> getCompanyTaxableContributions() {
        return CompanyTaxableContributions;
    }

    public void setCompanyTaxableContributions(Set<CheckPrintPaycheckLineDTO> companyTaxableContributions) {
        CompanyTaxableContributions = companyTaxableContributions;
    }

    public BigDecimal getCompanyContributionsCheckTotal() {
        return companyContributionsCheckTotal;
    }

    public void setCompanyContributionsCheckTotal(BigDecimal companyContributionsCheckTotal) {
        this.companyContributionsCheckTotal = companyContributionsCheckTotal;
    }

    public BigDecimal getCompanyContributionsYtdTotal() {
        return companyContributionsYtdTotal;
    }

    public void setCompanyContributionsYtdTotal(BigDecimal companyContributionsYtdTotal) {
        this.companyContributionsYtdTotal = companyContributionsYtdTotal;
    }

    public BigDecimal getCompanyTaxableContributionsCheckTotal() {
        return companyTaxableContributionsCheckTotal;
    }

    public void setCompanyTaxableContributionsCheckTotal(BigDecimal companyTaxableContributionsCheckTotal) {
        this.companyTaxableContributionsCheckTotal = companyTaxableContributionsCheckTotal;
    }

    public BigDecimal getCompanyTaxableContributionsYtdTotal() {
        return companyTaxableContributionsYtdTotal;
    }

    public void setCompanyTaxableContributionsYtdTotal(BigDecimal companyTaxableContributionsYtdTotal) {
        this.companyTaxableContributionsYtdTotal = companyTaxableContributionsYtdTotal;
    }

    public enum PaycheckType {
        ManualCheck,
        DirectDeposit
    }

    public void calculateTotals() {
        earningsCheckTotal = ZERO_BIGDECIMAL;
        earningsYtdTotal = ZERO_BIGDECIMAL;
        for (CheckPrintPaycheckEarningLineDTO earning : getEarnings()) {
            earningsCheckTotal = earningsCheckTotal.add(earning.getPaylineAmount());
            earningsYtdTotal = earningsYtdTotal.add(earning.getYtdAmount());
        }

        preTaxDeductionsCheckTotal = ZERO_BIGDECIMAL;
        preTaxDeductionsYtdTotal = ZERO_BIGDECIMAL;
        for (CheckPrintPaycheckLineDTO preTaxDeduction : getPreTaxDeductions()) {
            preTaxDeductionsCheckTotal = preTaxDeductionsCheckTotal.add(preTaxDeduction.getPaylineAmount());
            preTaxDeductionsYtdTotal = preTaxDeductionsYtdTotal.add(preTaxDeduction.getYtdAmount());
        }

        deductionsCheckTotal = ZERO_BIGDECIMAL;
        deductionsYtdTotal = ZERO_BIGDECIMAL;
        for (CheckPrintPaycheckLineDTO deduction : getDeductions()) {
            deductionsCheckTotal = deductionsCheckTotal.add(deduction.getPaylineAmount());
            deductionsYtdTotal = deductionsYtdTotal.add(deduction.getYtdAmount());
        }

        taxesCheckTotal = ZERO_BIGDECIMAL;
        taxesYtdTotal = ZERO_BIGDECIMAL;
        for (CheckPrintPaycheckLineDTO tax : getTaxes()) {
            taxesCheckTotal = taxesCheckTotal.add(tax.getPaylineAmount());
            taxesYtdTotal = taxesYtdTotal.add(tax.getYtdAmount());
        }

        if (checkNetPay.compareTo(ZERO_BIGDECIMAL) == 0) {
           for (CheckPrintPaycheckDDINfo ddInfo : directDeposits) {
               checkNetPay = checkNetPay.add(ddInfo.getDDAmount());
           }
        }

        BigDecimal calculatedCheckNetPay = earningsCheckTotal.add(preTaxDeductionsCheckTotal).add(deductionsCheckTotal).add(taxesCheckTotal);
        if (checkNetPay.compareTo(calculatedCheckNetPay) != 0) {
            logger.info("Calculated netpay " + calculatedCheckNetPay + " is different than netpay " + checkNetPay + " for check " + paycheckId);
        }

        ytdNetPay = earningsYtdTotal.add(preTaxDeductionsYtdTotal).add(deductionsYtdTotal).add(taxesYtdTotal);

        adjustedEarningsCheckTotal = earningsCheckTotal.add(preTaxDeductionsCheckTotal);
        adjustedEarningsYtdTotal = earningsYtdTotal.add(preTaxDeductionsYtdTotal);

        // Company contributions do not affect net pay
        setCompanyContributionsCheckTotal(ZERO_BIGDECIMAL);
        setCompanyContributionsYtdTotal(ZERO_BIGDECIMAL);
        for (CheckPrintPaycheckLineDTO companyContribution : getCompanyContributions()) {
            setCompanyContributionsCheckTotal(companyContributionsCheckTotal.add(companyContribution.getPaylineAmount()));
            setCompanyContributionsYtdTotal(getCompanyContributionsYtdTotal().add(companyContribution.getYtdAmount()));
        }

        setCompanyTaxableContributionsCheckTotal(ZERO_BIGDECIMAL);
        setCompanyTaxableContributionsYtdTotal(ZERO_BIGDECIMAL);
        for (CheckPrintPaycheckLineDTO companyTaxableContribution : getCompanyContributions()) {
            setCompanyTaxableContributionsCheckTotal(getCompanyTaxableContributionsCheckTotal().add(companyTaxableContribution.getPaylineAmount()));
            setCompanyTaxableContributionsYtdTotal(getCompanyTaxableContributionsYtdTotal().add(companyTaxableContribution.getYtdAmount()));
        }

    }
}
