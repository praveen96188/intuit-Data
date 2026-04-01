package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class LiabilityBalances {

    public static HashMap<Law, SpcfDecimal> getLiabilityBalances(PayrollRun payrollRun, Boolean pIsBalanceFile) {
        return getLiabilityBalances(payrollRun, pIsBalanceFile, false);
    }

    public static HashMap<Law, SpcfDecimal> getLiabilityBalances(PayrollRun payrollRun, Boolean pIsBalanceFile, Boolean pAlwaysRecordFTs) {
        return getLiabilityBalances(payrollRun, pIsBalanceFile, null, pAlwaysRecordFTs);
    }

    /**
     * Liability balance calculation per law will calculate the balance per law as:
     * -  Any AgencyHPDETaxPayment FinancialTransaction amounts associated with the company and law MINUS
     * -  Any liability amounts for the law associated with any other existing payroll associated with the same source system transmission MINUS
     * -  The liability amount for the law associated with the current payroll PLUS
     * -  Any AgencyTaxCredits for the law associated with any other existing payroll associated with the same source system transmission
     *
     * @param pIsBalanceFile - is the calculation for a balance file payroll
     * @return Map - liabilities per law
     */
    public static HashMap<Law, SpcfDecimal> getLiabilityBalances(PayrollRun payrollRun, Boolean pIsBalanceFile, String pSourceCompanyAdjustmentId, Boolean pAlwaysRecordFTs) {
        boolean debitForPayroll = payrollRun.getCompany().getCompanyService(ServiceCode.Tax).isIntuitResponsibleForLiabilities(payrollRun.getPaycheckDate());

        if (pIsBalanceFile && !debitForPayroll) {
            return new HashMap<Law, SpcfDecimal>();
        }

        //Get liability for current payrolls
        HashMap<Law, SpcfDecimal> currentLiabilities = payrollRun.getLiabilityAmountsByLaw(false, pSourceCompanyAdjustmentId, pAlwaysRecordFTs);

        if (pIsBalanceFile && debitForPayroll) {
            //Get amount we have left to apply per law: prior payments - prior refunds - amount we've already applied
            Map<Law, SpcfDecimal> availableHPDEAmounts = new HashMap<Law, SpcfDecimal>();
            for (Law law : currentLiabilities.keySet()) {
                SpcfDecimal priorPaymentAmount = getTotalHPDETaxAmountForCurrentQuarter(payrollRun.getCompany(),law, TransactionTypeCode.AgencyHPDETaxPayment, null);
                SpcfDecimal refundAmount = getTotalHPDETaxAmountForCurrentQuarter(payrollRun.getCompany(),law, TransactionTypeCode.AgencyHPDETaxRefund, null);
                SpcfDecimal appliedAmount = getTotalHPDETaxAmountForCurrentQuarter(payrollRun.getCompany(),law, TransactionTypeCode.AgencyHPDEPriorPaymentApplied, payrollRun);

                availableHPDEAmounts.put(law, priorPaymentAmount.subtract(refundAmount).subtract(appliedAmount));
            }

            // apply payment amounts to the current liabilities
            for (Iterator<Law> iterator = currentLiabilities.keySet().iterator(); iterator.hasNext();) {
                Law law = iterator.next();

                SpcfDecimal currentLiabilityAmount = currentLiabilities.get(law);
                SpcfDecimal availableAmount = availableHPDEAmounts.get(law);
                SpcfDecimal paymentApplied = SpcfMoney.ZERO;

                if (law.isCOBRA()) {
                    SpcfDecimal positiveLiabilityAmount = currentLiabilityAmount.negate();
                    SpcfDecimal positiveAvailableAmount = availableAmount.negate();

                    if (positiveLiabilityAmount.compareTo(SpcfMoney.ZERO)>=0 && positiveAvailableAmount.compareTo(SpcfMoney.ZERO)>=0) {
                        if (positiveLiabilityAmount.compareTo(positiveAvailableAmount)>=0) {
                            paymentApplied = availableAmount;
                            currentLiabilities.put(law, currentLiabilityAmount.subtract(paymentApplied).negate());
                        } else {
                            paymentApplied = currentLiabilityAmount;
                            iterator.remove();
                        }

                        // calculate payment applied.. should really be a different txn type, like AgencyHPDEPriorRefundApplied
                        if (paymentApplied.compareTo(SpcfMoney.ZERO) < 0) {
                            SpcfCalendar settlementDate = PSPDate.getPSPTime();
                            CalendarUtils.clearTime(settlementDate);

                            FinancialTransaction.createHPDETransaction(
                                    payrollRun.getCompany(), payrollRun, TransactionTypeCode.AgencyHPDEPriorPaymentApplied,
                                    new SpcfMoney(paymentApplied.negate()), PSPDate.getPSPTime(), null, law, null, null);
                        }
                    }
                } else {
                    if (currentLiabilityAmount.compareTo(SpcfMoney.ZERO)>=0 && availableAmount.compareTo(SpcfMoney.ZERO)>=0) {
                        if (currentLiabilityAmount.compareTo(availableAmount)>0)  {
                            paymentApplied = availableAmount;
                            currentLiabilities.put(law, currentLiabilityAmount.subtract(paymentApplied));
                        } else {
                            paymentApplied = currentLiabilityAmount;
                            iterator.remove();
                        }

                        // calculate payment applied
                        if (paymentApplied.compareTo(SpcfMoney.ZERO) > 0) {
                            SpcfCalendar settlementDate = PSPDate.getPSPTime();
                            CalendarUtils.clearTime(settlementDate);
                            FinancialTransaction.createHPDETransaction(
                                    payrollRun.getCompany(), payrollRun, TransactionTypeCode.AgencyHPDEPriorPaymentApplied,
                                    new SpcfMoney(paymentApplied), settlementDate, null, law, null, null);
                        }
                    }
                }
            }
        }
        return currentLiabilities;
    }

    //If a payroll is specified, we must look in in-memory payrolls as well
    public static SpcfDecimal getTotalHPDETaxAmountForCurrentQuarter(Company company, Law pLaw, TransactionTypeCode pTransactionTypeCode, PayrollRun pPayrollRun) {
        //We only want to include HPDE financial transactions for the current quarter
        SpcfCalendar firstDayofQuarter = CalendarUtils.getFirstDayOfQuarter(PSPDate.getPSPTime());
        SpcfCalendar lastDayofQuarter = CalendarUtils.getLastDayOfQuarter(PSPDate.getPSPTime());
        Criterion<FinancialTransaction> criterion =
                FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(pTransactionTypeCode)
                        .And(FinancialTransaction.Law().equalTo(pLaw))
                        .And(FinancialTransaction.Company().equalTo(company));
        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();

        DomainEntitySet<MoneyMovementTransaction> inMemoryMoneyMovementTransactions = MoneyMovementTransaction.findAchDirectDepositCreatedMoneyMovementTransactions(company);
        for (MoneyMovementTransaction inMemoryMoneyMovementTransaction : inMemoryMoneyMovementTransactions) {
            if (inMemoryMoneyMovementTransaction.getPaymentPeriodEnd() != null &&
                    inMemoryMoneyMovementTransaction.getPaymentPeriodEnd().between(firstDayofQuarter, lastDayofQuarter)) {
                financialTransactions.addAll(inMemoryMoneyMovementTransaction.getFinancialTransactionCollection().find(criterion));
            }
        }

        if (pPayrollRun != null) {
                for (TransmissionPayrollRun transmissionPayrollRun : pPayrollRun.getTransmissionPayrollRunCollection()) {
                    SourceSystemTransmission sourceSystemTransmission = SourceSystemTransmission.getSourceSystemTransmissionById(transmissionPayrollRun.getSourceSystemTransmissionId());
                    for (TransmissionPayrollRun transmissionPayroll : sourceSystemTransmission.getTransmissionPayrollRunCollection()) {
                        if (transmissionPayroll.getPayrollRun().getPaycheckDate().between(firstDayofQuarter, lastDayofQuarter)) {
                            financialTransactions.addAll(transmissionPayroll.getPayrollRun().getFinancialTransactionCollection().find(criterion));
                        }
                    }
                }
        }

        SpcfDecimal hpdeTaxAmount = new SpcfMoney("0.0");
        for (FinancialTransaction financialTransaction : financialTransactions) {
            hpdeTaxAmount = hpdeTaxAmount.add(financialTransaction.getFinancialTransactionAmount());
        }
        return hpdeTaxAmount;
    }
}
