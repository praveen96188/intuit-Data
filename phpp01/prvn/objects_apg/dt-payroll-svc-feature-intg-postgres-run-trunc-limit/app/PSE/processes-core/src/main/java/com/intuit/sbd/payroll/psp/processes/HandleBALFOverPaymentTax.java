package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jan 29, 2011
 * Time: 10:15:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class HandleBALFOverPaymentTax implements IProcess {

    private String mTransmissionIdentifier;
    private SpcfCalendar mQuarterToStartDate;

    public HandleBALFOverPaymentTax(String mTransmissionIdentifier, SpcfCalendar pQuarterToStartDate) {
        this.mTransmissionIdentifier = mTransmissionIdentifier;
        this.mQuarterToStartDate = pQuarterToStartDate;
    }

    public ProcessResult execute() {
        // process all 4 quarters
        for (int i = 1; i < 5; i++) {
            findUnderOrOverPaymentForQuarter(CalendarUtils.getFirstDayOfQuarter(mQuarterToStartDate.getYear(), i), CalendarUtils.getLastDayOfQuarter(mQuarterToStartDate.getYear(), i));
        }

        return new ProcessResult<PayrollRun>();
    }

    private void findUnderOrOverPaymentForQuarter(SpcfCalendar pQuarterBeginDate, SpcfCalendar pQuarterEndDate) {
        Company company = null;
        DomainEntitySet<TransmissionPayrollRun> transmissionPayrollRuns = null;
        SourceSystemTransmission sourceSystemTransmission = SourceSystemTransmission.findSourceSystemTransmissionByIdentifier(mTransmissionIdentifier);
        company = sourceSystemTransmission.getCompany();
        transmissionPayrollRuns = sourceSystemTransmission.getTransmissionPayrollRunCollection()
                .find(TransmissionPayrollRun.PayrollRun().PaycheckDate().greaterOrEqualThan(pQuarterBeginDate).And(TransmissionPayrollRun.PayrollRun().PaycheckDate().lessOrEqualThan(pQuarterEndDate)));
        Map<CompanyLaw, SpcfDecimal> priorPayments = new HashMap<CompanyLaw, SpcfDecimal>();
        List<Law> laws = new ArrayList<Law>();
        boolean isCurrentQuarter = CalendarUtils.isCurrentQuarter(pQuarterBeginDate);
        DomainEntitySet<FinancialTransaction> hpdeTaxPaymentFinancialTransactions = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<FinancialTransaction> hpdeTaxRefundFinancialTransactions = new DomainEntitySet<FinancialTransaction>();
        DomainEntitySet<MoneyMovementTransaction> inMemoryMoneyMovementTransactions = MoneyMovementTransaction.findAchDirectDepositCreatedMoneyMovementTransactions(company)
                .find(MoneyMovementTransaction.PaymentPeriodBegin().greaterOrEqualThan(pQuarterBeginDate).And(MoneyMovementTransaction.PaymentPeriodEnd().lessOrEqualThan(pQuarterEndDate)));
        DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = Application.find(MoneyMovementTransaction.class, MoneyMovementTransaction.Company().equalTo(company)
                .And(MoneyMovementTransaction.PaymentPeriodBegin().greaterOrEqualThan(pQuarterBeginDate).And(MoneyMovementTransaction.PaymentPeriodEnd().lessOrEqualThan(pQuarterEndDate))));
        moneyMovementTransactions.addAll(inMemoryMoneyMovementTransactions);
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            hpdeTaxPaymentFinancialTransactions.addAll(moneyMovementTransaction.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyHPDETaxPayment)
                    .And(FinancialTransaction.Law().PaymentTemplate().SupportStartDate().lessOrEqualThan(pQuarterBeginDate))));
            hpdeTaxRefundFinancialTransactions.addAll(moneyMovementTransaction.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.AgencyHPDETaxRefund)
                    .And(FinancialTransaction.Law().PaymentTemplate().SupportStartDate().lessOrEqualThan(pQuarterBeginDate))));
        }
        for (FinancialTransaction financialTransaction : hpdeTaxPaymentFinancialTransactions) {
            SpcfDecimal amount = priorPayments.get(financialTransaction.getCompanyLaw());
            if (amount == null) {
                priorPayments.put(financialTransaction.getCompanyLaw(), financialTransaction.getFinancialTransactionAmount());
                laws.add(financialTransaction.getCompanyLaw().getLaw());
            } else {
                priorPayments.put(financialTransaction.getCompanyLaw(), financialTransaction.getFinancialTransactionAmount().add(amount));
            }
        }
        for (FinancialTransaction financialTransaction : hpdeTaxRefundFinancialTransactions) {
            SpcfDecimal amount = priorPayments.get(financialTransaction.getCompanyLaw());
            if (amount == null) {
                priorPayments.put(financialTransaction.getCompanyLaw(), financialTransaction.getFinancialTransactionAmount().multiply(new SpcfMoney("-1")));
                laws.add(financialTransaction.getCompanyLaw().getLaw());
            } else {
                priorPayments.put(financialTransaction.getCompanyLaw(), amount.subtract(financialTransaction.getFinancialTransactionAmount()));
            }
        }

        Map<Law, SpcfDecimal> liabilitiesForAllPayrolls = new HashMap<Law, SpcfDecimal>();
        for (TransmissionPayrollRun transmissionPayrollRun : transmissionPayrollRuns) {
            Map<Law, SpcfDecimal> liabilitiesForOnePayroll = transmissionPayrollRun.getPayrollRun().getLiabilityAmountsByLaw(!isCurrentQuarter, null); // only for paycheck dates < quarter to start date
            for (Law law : liabilitiesForOnePayroll.keySet()) {
                if (laws.contains(law)) {
                    if (liabilitiesForAllPayrolls.get(law) == null) {
                        liabilitiesForAllPayrolls.put(law, liabilitiesForOnePayroll.get(law));
                    } else {
                        liabilitiesForAllPayrolls.put(law, liabilitiesForAllPayrolls.get(law).add(liabilitiesForOnePayroll.get(law)));
                    }
                }
            }
        }
        for (CompanyLaw companyLaw : priorPayments.keySet()) {
            SpcfDecimal amount = priorPayments.get(companyLaw);
            if (liabilitiesForAllPayrolls.get(companyLaw.getLaw()) != null) {
                priorPayments.put(companyLaw, amount.subtract(liabilitiesForAllPayrolls.get(companyLaw.getLaw())));
            }
        }
        PayrollRun payrollRun = null;
        for (CompanyLaw companyLaw : priorPayments.keySet()) {
            if (priorPayments.get(companyLaw).isGreaterThan(SpcfMoney.ZERO)) {
                //Over payment
                if (payrollRun == null) {
                    SpcfCalendar settlementDate = PSPDate.getPSPTime();
                    CalendarUtils.clearTime(settlementDate);
                    payrollRun = PayrollRun.createAdjustmentPayrollRun(company, isCurrentQuarter ? settlementDate : pQuarterEndDate);
                    payrollRun.updatePayrollRunStatus(PayrollStatus.Complete);
                }
                createLiabilityAdjustment(companyLaw, priorPayments.get(companyLaw), payrollRun);
            } else if (priorPayments.get(companyLaw).isLessThan(SpcfMoney.ZERO) && !isCurrentQuarter) {
                //under payment
                if (payrollRun == null) {
                    payrollRun = PayrollRun.createAdjustmentPayrollRun(company, pQuarterEndDate);
                    payrollRun.updatePayrollRunStatus(PayrollStatus.Complete);
                }
                createLiabilityAdjustment(companyLaw, priorPayments.get(companyLaw), payrollRun);
            }
        }
        if (payrollRun != null && payrollRun.updateEETotalsCalculationRequired()) {
            EmpTotalsPayrollRun.insertEmpTotalsPayrollRun(payrollRun);
        }
    }

    private void createLiabilityAdjustment(CompanyLaw pCompanyLaw, SpcfDecimal pLawAmount, PayrollRun pPayrollRun) {
        LiabilityAdjustment liabilityAdjustment = new LiabilityAdjustment();
        liabilityAdjustment.setCompany(pPayrollRun.getCompany());
        liabilityAdjustment.setIsReconcilingAdjustment(true);
        liabilityAdjustment.setAmount((SpcfMoney) pLawAmount);
        liabilityAdjustment.setLaw(pCompanyLaw.getLaw());
        liabilityAdjustment.setPayrollRun(pPayrollRun);
        liabilityAdjustment.setCompanyLaw(pCompanyLaw);
        liabilityAdjustment.setEffectiveDate(pPayrollRun.getPaycheckDate());
        Application.save(liabilityAdjustment);
    }
}
