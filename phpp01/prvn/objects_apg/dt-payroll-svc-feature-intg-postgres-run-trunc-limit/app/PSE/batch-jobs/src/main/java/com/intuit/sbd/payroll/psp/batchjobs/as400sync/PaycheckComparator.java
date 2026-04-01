package com.intuit.sbd.payroll.psp.batchjobs.as400sync;

import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class PaycheckComparator {
    private static final SpcfDecimal ZERO = SpcfMoney.createInstance(0.00);
    private List<String> errorMessages = new ArrayList<String>();
    private List<String> infoMessages = new ArrayList<String>();
    private SpcfUniqueId paycheckId;

    public PaycheckComparator(SpcfUniqueId paycheckId) {
        this.paycheckId = paycheckId;
    }

    /**
     * ***************************************************************************************************************
     * comparePaycheck
     * ****************************************************************************************************************
     */
    public void comparePaycheck(Paycheck pspPayCheck, CheckPrintPaycheckDTO as400Paycheck) {
        String checkIdentifier = "Company=" + pspPayCheck.getPayrollRun().getCompany().getSourceCompanyId() + " Paycheck=" + pspPayCheck.getSourcePaycheckId();

        compareAmount("Net Pay", checkIdentifier, pspPayCheck.getNetAmount(), as400Paycheck.getCheckNetPay().multiply(new BigDecimal(-1)));
        compareDate("Paycheck Date", checkIdentifier, pspPayCheck.getPayrollRun().getPaycheckDate(), as400Paycheck.getCheckDate());
        compareDate("Period Start Date", checkIdentifier, pspPayCheck.getPayPeriodBeginDate(), as400Paycheck.getPeriodStartDate());
        compareDate("Period End Date", checkIdentifier, pspPayCheck.getPayPeriodEndDate(), as400Paycheck.getPeriodEndDate());
        compareString("Employee Id", checkIdentifier, pspPayCheck.getSourceEmployee() == null ? pspPayCheck.getDDEmployee().getSourceEmployeeId() : pspPayCheck.getSourceEmployee().getSourceEmployeeId(), as400Paycheck.getEmployeeId());
        compareCompensations(checkIdentifier, pspPayCheck.getCompensationCollection(), as400Paycheck.getEarnings());
        compareDeductions(checkIdentifier, pspPayCheck.getDeductionCollection(), as400Paycheck.getDeductions(), as400Paycheck.getPreTaxDeductions());
        compareCompanyContributions(checkIdentifier, pspPayCheck.getEmployerContributionCollection(), as400Paycheck.getCompanyContributions(), as400Paycheck.getCompanyTaxableContributions());
        compareTaxes(checkIdentifier, pspPayCheck.getTaxCollection(), as400Paycheck.getTaxes(), as400Paycheck.getCompanyTaxes());
        //compareDirectDepositSplits(checkIdentifier, pspPayCheck.getPaycheckSplits(), as400Paycheck.getDirectDeposits());

        infoMessages.add("Paycheck " + checkIdentifier + " " + pspPayCheck.getPayrollRun().getPaycheckDate().toISO8601() +
                (errorMessages.size() == 0 ? " matches " : " did not match") +
                " #Comps:" + pspPayCheck.getCompensationCollection().size() + "/" + as400Paycheck.getEarnings().size() +
                " #Deds:" + pspPayCheck.getDeductionCollection().size() + "/" + (as400Paycheck.getDeductions().size() + as400Paycheck.getPreTaxDeductions().size()) +
                " #Taxes:" + pspPayCheck.getTaxCollection().size() + "/" + (as400Paycheck.getTaxes().size() + as400Paycheck.getCompanyTaxes().size()) +
                " #ER Contributions:" + pspPayCheck.getEmployerContributionCollection().size() + "/" + (as400Paycheck.getCompanyContributions().size() + as400Paycheck.getCompanyTaxableContributions().size()) +
                " #DD Splits:" + pspPayCheck.getPaycheckSplits().size() + "/" + as400Paycheck.getDirectDeposits().size());
    }


    /**
     * ***************************************************************************************************************
     * compareTaxes, compareDeductions, etc.
     * ****************************************************************************************************************
     */
    private void compareDirectDepositSplits(String checkIdentifier, DomainEntitySet<PaycheckSplit> pspPaycheckSplits, List<CheckPrintPaycheckDDINfo> as400PaycheckSplits) {
        DomainEntitySet<PaycheckSplit> pspPaycheckSplitsLocalSet = new DomainEntitySet<PaycheckSplit>();   // make a copy so we can modify the collection locally
        pspPaycheckSplitsLocalSet.addAll(pspPaycheckSplits);

        for (CheckPrintPaycheckDDINfo as400PaycheckSplit : as400PaycheckSplits) {
            comparePaycheckSplit(checkIdentifier, pspPaycheckSplitsLocalSet, as400PaycheckSplit);
        }

        // The remaining entries in pspPaycheckSplitsLocalSet did not have a match in the AS/400
        for (PaycheckSplit pspPaycheckSplit : pspPaycheckSplitsLocalSet) {
            errorMessages.add("Paycheck Split [" + pspPaycheckSplit.getEmployeeBankAccount().getBankAccount().getAccountNumber() + "," + pspPaycheckSplit.getPaycheckSplitAmount().toString() + "] in PSP for " + checkIdentifier + " does not exist in AS/400");
        }
    }

    private void compareCompanyContributions(String checkIdentifier, DomainEntitySet<EmployerContribution> pspEmployerContributions, Set<CheckPrintPaycheckLineDTO> as400CompanyContributions, Set<CheckPrintPaycheckLineDTO> as400CompanyTaxableContributions) {
        DomainEntitySet<EmployerContribution> pspContributionsLocalSet = new DomainEntitySet<EmployerContribution>();   // make a copy so we can modify the collection locally
        pspContributionsLocalSet.addAll(pspEmployerContributions);

        for (CheckPrintPaycheckLineDTO as400Deduction : as400CompanyContributions) {
            compareEmployerContribution(checkIdentifier, pspContributionsLocalSet, as400Deduction);
        }
        for (CheckPrintPaycheckLineDTO as400Deduction : as400CompanyTaxableContributions) {
            compareEmployerContribution(checkIdentifier, pspContributionsLocalSet, as400Deduction);
        }

        // The remaining entries in pspContributionsLocalSet did not have a match in the AS/400
        for (EmployerContribution pspEmployerContribution : pspContributionsLocalSet) {
            if (pspEmployerContribution.getContributionAmount().compareTo(ZERO) == 0 && pspEmployerContribution.getContributionYTDAmount().compareTo(ZERO) == 0) {
                continue;
            }
            errorMessages.add("Employer Contribution [" + pspEmployerContribution.getCompanyPayrollItem().getSourceDescription() + "," + pspEmployerContribution.getContributionAmount().toString() + "] in PSP for " + checkIdentifier + " does not exist in AS/400");
        }
    }

    private void compareDeductions(String checkIdentifier, DomainEntitySet<Deduction> pspDeductions, Set<CheckPrintPaycheckLineDTO> as400Deductions, Set<CheckPrintPaycheckLineDTO> as400PreTaxDeductions) {
        DomainEntitySet<Deduction> pspDeductionsLocalSet = new DomainEntitySet<Deduction>();   // make a copy so we can modify the collection locally
        pspDeductionsLocalSet.addAll(pspDeductions);

        for (CheckPrintPaycheckLineDTO as400Deduction : as400Deductions) {
            compareDeduction(checkIdentifier, pspDeductionsLocalSet, as400Deduction);
        }

        for (CheckPrintPaycheckLineDTO as400Deduction : as400PreTaxDeductions) {
            compareDeduction(checkIdentifier, pspDeductionsLocalSet, as400Deduction);
        }

        // The remaining entries in pspDeductionsLocalSet did not have a match in the AS/400
        for (Deduction pspDeduction : pspDeductionsLocalSet) {
            if (pspDeduction.getCompanyPayrollItem().getSourceDescription().equals("Direct Deposit") ||
                    pspDeduction.getCompanyPayrollItem().getSourceDescription().equals("*Direct Deposit") ||
                    pspDeduction.getDeductionAmount().compareTo(ZERO) == 0 && pspDeduction.getDeductionYTDAmount().compareTo(ZERO) == 0) {
                continue;
            }

            errorMessages.add("Deduction [" + pspDeduction.getCompanyPayrollItem().getSourceDescription() + ", type=" + pspDeduction.getCompanyPayrollItem().getPayrollItem().getPayrollItemCode() + "," + pspDeduction.getDeductionAmount().toString() + "] in PSP for " + checkIdentifier + " does not exist in AS/400");
        }
    }

    private void compareTaxes(String checkIdentifier, DomainEntitySet<Tax> pspTaxes, Set<CheckPrintPaycheckLineDTO> as400Taxes, Set<CheckPrintPaycheckLineDTO> as400CompanyTaxes) {
        DomainEntitySet<Tax> pspTaxesLocalSet = new DomainEntitySet<Tax>();   // make a copy so we can modify the collection locally
        pspTaxesLocalSet.addAll(pspTaxes);

        for (CheckPrintPaycheckLineDTO as400Tax : as400Taxes) {
            compareTax(checkIdentifier, pspTaxesLocalSet, as400Tax, false);
        }

        for (CheckPrintPaycheckLineDTO as400Tax : as400CompanyTaxes) {
            compareTax(checkIdentifier, pspTaxesLocalSet, as400Tax, true);
        }

        // The remaining entries in pspTaxesLocalSet did not have a match in the AS/400
        for (Tax pspTax : pspTaxesLocalSet) {
            errorMessages.add("Tax [" + pspTax.getCompanyLaw().getSourceDescription() + "," + pspTax.getTaxLiabilityAmount().toString() + "] in PSP for " + checkIdentifier + " does not exist in AS/400");
        }
    }


    private void compareCompensations(String checkIdentifier, DomainEntitySet<Compensation> pspCompensations, Set<CheckPrintPaycheckEarningLineDTO> as400Compensations) {
        DomainEntitySet<Compensation> pspCompensationsLocalSet = new DomainEntitySet<Compensation>();   // make a copy so we can modify the collection locally
        pspCompensationsLocalSet.addAll(pspCompensations);

        for (CheckPrintPaycheckEarningLineDTO as400Comp : as400Compensations) {
            compareCompensation(checkIdentifier, pspCompensationsLocalSet, as400Comp);
        }

        // The remaining entries in pspCompensationsLocalSet did not have a match in the AS/400
        for (Compensation pspComp : pspCompensationsLocalSet) {
            if (pspComp.getCompensationAmount().compareTo(ZERO) == 0 && pspComp.getCompensationYTDAmount().compareTo(ZERO) == 0)
                continue;

            errorMessages.add("Compensation [" + pspComp.getCompanyPayrollItem().getSourceDescription() + "," + pspComp.getCompensationAmount().toString() + "] in PSP for " + checkIdentifier + " does not exist in AS/400");
        }
    }

    /**
     * ***************************************************************************************************************
     * compareTax, compareCompensation, etc.
     * ****************************************************************************************************************
     */
    private void compareTax(String checkIdentifier, DomainEntitySet<Tax> pspTaxesLocalSet, CheckPrintPaycheckLineDTO as400Tax, Boolean isCompanyTax) {
        SpcfMoney as400Amount;
        BigDecimal as400YtdAmount;
        BigDecimal as400TotalWagesAmount;
        BigDecimal as400TaxableWagesAmount;
        BigDecimal as400TipTaxableWagesAmount;

        if (isCompanyTax) {
            as400Amount = SpcfUtils.convertToSpcfMoney(as400Tax.getPaylineAmount());
            as400YtdAmount = as400Tax.getYtdAmount();
        } else {
            as400Amount = SpcfUtils.convertToSpcfMoney(as400Tax.getPaylineAmount().multiply(new BigDecimal(-1)));
            as400YtdAmount = as400Tax.getYtdAmount().multiply(new BigDecimal(-1));
        }

        as400TipTaxableWagesAmount = as400Tax.getTipTaxableWages();
        if (as400Tax.getTaxableWages().compareTo(as400Tax.getTotalWages()) > 0) {
            as400TotalWagesAmount = as400Tax.getTaxableWages();
            as400TaxableWagesAmount = as400Tax.getTotalWages();
        } else {
            as400TotalWagesAmount = as400Tax.getTotalWages();
            as400TaxableWagesAmount = as400Tax.getTaxableWages();
        }

        Tax pspTax = findFirst(pspTaxesLocalSet,
                (Tax.CompanyLaw().SourceDescription().equalTo(as400Tax.getPaylineDescription()))
                        .And(Tax.TaxLiabilityAmount().equalTo(as400Amount)));

        if (pspTax == null) {
            // in the as400 but not in PSP
            errorMessages.add(checkIdentifier + " Tax [" + as400Tax.getPaylineDescription() + "," + as400Tax.getPaylineAmount().toString() + "] in AS/400" + " does not exist in PSP");
        } else {
            compareAmount("Tax YTD Amount", checkIdentifier + " Tax [" + as400Tax.getPaylineDescription() + "]", pspTax.getTaxLiabilityYTDAmount(), as400YtdAmount);
            compareAmount("Tax Total Wages", checkIdentifier + " Tax [" + as400Tax.getPaylineDescription() + ", EmployerTax=" + isCompanyTax + "/" + pspTax.getCompanyLaw().getLaw().getIsEmployerTax() + "]", pspTax.getTotalWagesAmount(), as400TotalWagesAmount);
            compareAmount("Tax Taxable Wages", checkIdentifier + " Tax [" + as400Tax.getPaylineDescription() + "]", pspTax.getTaxableWagesAmount(), as400TaxableWagesAmount);
            compareAmount("Tax Tip Wages", checkIdentifier + " Tax [" + as400Tax.getPaylineDescription() + "]", pspTax.getTipsTaxableWageAmount(), as400TipTaxableWagesAmount);

            // remove from psp collection
            pspTaxesLocalSet.remove(pspTax);
        }
    }

    private void compareCompensation(String checkIdentifier, DomainEntitySet<Compensation> pspCompensationsLocalSet, CheckPrintPaycheckEarningLineDTO as400Comp) {
        Compensation comp = findFirst(pspCompensationsLocalSet,
                (Compensation.CompanyPayrollItem().SourceDescription().equalTo(as400Comp.getPaylineDescription()))
                        .And(Compensation.CompensationAmount().equalTo(SpcfUtils.convertToSpcfMoney(as400Comp.getPaylineAmount()))));

        if (comp == null) {
            // Try to match with a sum of psp compensations
            SpcfDecimal accumulatedAmount = new SpcfMoney("0.00");
            double accumulatedHours = 0.0;
            DomainEntitySet<Compensation> pspCompensations = pspCompensationsLocalSet.find(Deduction.CompanyPayrollItem().SourceDescription().equalTo(as400Comp.getPaylineDescription()));
            for (Compensation pspComp : pspCompensations) {
                accumulatedAmount = accumulatedAmount.add(pspComp.getCompensationAmount());
                accumulatedHours = accumulatedHours + pspComp.getHoursWorked();
            }

            if (accumulatedAmount.compareTo(SpcfUtils.convertToSpcfMoney(as400Comp.getPaylineAmount())) != 0 ||
                    as400Comp.getHours().compareTo(new BigDecimal(accumulatedHours).setScale(as400Comp.getHours().scale(), RoundingMode.HALF_UP)) != 0) {
                // in the as400 but not in PSP
                errorMessages.add(checkIdentifier + " Compensation [" + as400Comp.getPaylineDescription() + ", " + as400Comp.getPaylineAmount().toString() + "] in AS/400" + " does not exist in PSP");
            } else {
                // remove from psp collection
                for (Compensation pspComp : pspCompensations) {
                    pspCompensationsLocalSet.remove(pspComp);
                }
            }

        } else {
            compareAmount("Compensation [" + as400Comp.getPaylineDescription() + "]", checkIdentifier, comp.getHoursWorked(), as400Comp.getHours());

            // remove from psp collection
            pspCompensationsLocalSet.remove(comp);
        }
    }

    private void compareDeduction(String checkIdentifier, DomainEntitySet<Deduction> pspDeductionsLocalSet, CheckPrintPaycheckLineDTO as400Deduction) {
        SpcfMoney as400Amount = SpcfUtils.convertToSpcfMoney(as400Deduction.getPaylineAmount());
        Deduction pspDeduction = findFirst(pspDeductionsLocalSet,
                (Deduction.CompanyPayrollItem().SourceDescription().equalTo(as400Deduction.getPaylineDescription()))
                        .And(Deduction.DeductionAmount().equalTo(as400Amount)));

        if (pspDeduction == null) {
            // Try to match with a sum of psp deductions
            SpcfDecimal accumulatedAmount = new SpcfMoney("0.00");
            DomainEntitySet<Deduction> pspDeductions = pspDeductionsLocalSet.find(Deduction.CompanyPayrollItem().SourceDescription().equalTo(as400Deduction.getPaylineDescription()));
            for (Deduction pspDed : pspDeductions) {
                accumulatedAmount = accumulatedAmount.add(pspDed.getDeductionAmount());
            }

            if (accumulatedAmount.compareTo(as400Amount) != 0) {
                // in the as400 but not in PSP
                errorMessages.add(checkIdentifier + " Deduction [" + as400Deduction.getPaylineDescription() + "," + as400Deduction.getPaylineAmount().toString() + "] in AS/400" + " does not exist in PSP");
            } else {
                // remove from psp collection
                for (Deduction pspDed : pspDeductions) {
                    compareAmount("Deduction YTD Amount", checkIdentifier, pspDed.getDeductionYTDAmount(), as400Deduction.getYtdAmount());
                    pspDeductionsLocalSet.remove(pspDed);
                }
            }
        } else {
            compareAmount("Deduction YTD Amount", checkIdentifier, pspDeduction.getDeductionYTDAmount(), as400Deduction.getYtdAmount());

            // remove from psp collection
            pspDeductionsLocalSet.remove(pspDeduction);
        }
    }

    private void compareEmployerContribution(String checkIdentifier, DomainEntitySet<EmployerContribution> pspContributionsLocalSet, CheckPrintPaycheckLineDTO as400Deduction) {
        SpcfMoney as400Amount = SpcfUtils.convertToSpcfMoney(as400Deduction.getPaylineAmount());
        EmployerContribution pspEmployerContribution = findFirst(pspContributionsLocalSet,
                (EmployerContribution.CompanyPayrollItem().SourceDescription().equalTo(as400Deduction.getPaylineDescription()))
                        .And(EmployerContribution.ContributionAmount().equalTo(as400Amount)));

        if (pspEmployerContribution == null) {
            // in the as400 but not in PSP
            errorMessages.add(checkIdentifier + " Contribution [" + as400Deduction.getPaylineDescription() + "," + as400Deduction.getPaylineAmount().toString() + "] in AS/400" + " does not exist in PSP");
        } else {
            compareAmount("Contribution YTD Amount", checkIdentifier, pspEmployerContribution.getContributionYTDAmount(), as400Deduction.getYtdAmount());

            // remove from psp collection
            pspContributionsLocalSet.remove(pspEmployerContribution);
        }
    }

    private void comparePaycheckSplit(String checkIdentifier, DomainEntitySet<PaycheckSplit> pspPaycheckSplitsLocalSet, CheckPrintPaycheckDDINfo as400PaycheckSplit) {
        SpcfMoney as400Amount = SpcfUtils.convertToSpcfMoney(as400PaycheckSplit.getDDAmount());
        PaycheckSplit pspPaycheckSplit = null;
        Criterion<PaycheckSplit> criterion = null;
        if(as400PaycheckSplit.getAccountId() == null){
            criterion = PaycheckSplit.EmployeeBankAccount().BankAccount().AccountNumberEnc().isNull();
        }else{
            List<String> accountIdEncList = EncryptionUtils.deterministicEncryptWithAllKeys(BankAccount.AccountNumberKeyName, as400PaycheckSplit.getAccountId());
            criterion = PaycheckSplit.EmployeeBankAccount().BankAccount().AccountNumberEnc().in(accountIdEncList);
        }
        criterion = criterion.And(PaycheckSplit.PaycheckSplitAmount().equalTo(as400Amount));
        pspPaycheckSplit = findFirst(pspPaycheckSplitsLocalSet,criterion);
        if (pspPaycheckSplit == null) {
            // in the as400 but not in PSP
            errorMessages.add(checkIdentifier + " Paycheck Split [" + as400PaycheckSplit.getAccountId() + "," + as400PaycheckSplit.getDDAmount().toString() + "] in AS/400" + " does not exist in PSP");
        } else {
            compareBankAccountType("Paycheck Split Account Type", checkIdentifier, pspPaycheckSplit.getEmployeeBankAccount().getBankAccount().getAccountTypeCd(), as400PaycheckSplit.getAccountType());

            // remove from psp collection
            pspPaycheckSplitsLocalSet.remove(pspPaycheckSplit);
        }
    }

    /**
     * ***************************************************************************************************************
     * compareAmount, compareHours, etc.
     * ****************************************************************************************************************
     */
    private void compareAmount(String message, String checkIdentifier, double hoursWorked, BigDecimal as400Hours) {
        BigDecimal pspHours = new BigDecimal(hoursWorked);
        if (as400Hours.compareTo(pspHours.setScale(as400Hours.scale(), RoundingMode.HALF_UP)) != 0) {
            errorMessages.add(message + " does not match for " + checkIdentifier + " PSP: " + pspHours.toString() + " AS400: " + as400Hours.toString());
        }
    }

    private void compareAmount(String message, String checkIdentifier, SpcfMoney netAmount, BigDecimal checkNetPay) {
        SpcfDecimal as400NetPay = SpcfUtils.convertToSpcfMoney(checkNetPay);
        if (netAmount.setScale(as400NetPay.getScale(), SpcfDecimal.SpcfRoundingType.HalfUp).compareTo(as400NetPay) != 0) {
            errorMessages.add(message + " does not match for " + checkIdentifier + " PSP: " + netAmount.toString() + " AS400: " + as400NetPay.toString());
        }
    }

    private void compareDate(String message, String checkIdentifier, SpcfCalendar pspDate, DateDTO as400Date) {
        if (pspDate == null) {
            errorMessages.add(message + " does not match for " + checkIdentifier + " PSP: null AS400: " + as400Date.toString());
        } else {
            if (DateDTO.convertToSpcfCalendar(as400Date).compareTo(pspDate) != 0) {
                errorMessages.add(message + " does not match for " + checkIdentifier + " PSP: " + pspDate.toString() + " AS400: " + as400Date.toString());
            }
        }
    }

    private void compareString(String message, String checkIdentifier, String pspSourceEmployeeId, String as400SourceEmployeeId) {
        if (!pspSourceEmployeeId.equals(as400SourceEmployeeId)) {
            errorMessages.add(message + " does not match for " + checkIdentifier + " PSP: " + pspSourceEmployeeId + " AS400: " + as400SourceEmployeeId);
        }
    }

    private void compareBankAccountType(String message, String checkIdentifier, BankAccountType pspAccountType, String as400AccountType) {
        if (pspAccountType.toString().equals(as400AccountType)) {
            errorMessages.add(message + " does not match for " + checkIdentifier + " PSP: " + pspAccountType.toString() + " AS400: " + as400AccountType);
        }
    }

    private static <T extends DataObject> T findFirst(DomainEntitySet<T> set, Criterion criterion) {
        DomainEntitySet<T> entities = set.find(criterion);
        if (entities.size() > 0) {
            return entities.get(0);
        } else {
            return null;
        }
    }

    public String getMessage() {
        final String newLine = System.getProperty("line.separator");
        StringBuilder result = new StringBuilder(newLine);

        for (String currMessage : infoMessages) {
            result.append(currMessage);
            result.append(newLine);
        }

        for (String currMessage : errorMessages) {
            result.append(currMessage);
            result.append(newLine);
        }

        return result.toString();
    }

    public boolean getHasErrors() {
        return errorMessages.size() > 0;
    }
}
