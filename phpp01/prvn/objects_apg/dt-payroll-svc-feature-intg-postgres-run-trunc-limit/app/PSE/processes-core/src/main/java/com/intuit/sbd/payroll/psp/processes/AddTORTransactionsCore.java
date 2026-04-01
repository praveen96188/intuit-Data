package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * User: dweinberg
 * Date: 12/5/12
 * Time: 2:49 PM
 * If there is any ATR balance for the quarter/template/company, create AgencyRefundTOR transactions and return the created payroll run.
 * Otherwise, do nothing and return null
 */
public class AddTORTransactionsCore extends Process {

    private SourceSystemCode sourceSystemCode;
    private String sourceCompanyId;
    private String paymentTemplateCd;
    private SpcfCalendar quarterEndDate;

    private Company company;
    private Map<Law,SpcfDecimal> mBalancesToApply;

    public AddTORTransactionsCore(SourceSystemCode pSourceSystemCode, String pSourceCompanyId, String pPaymentTemplateCd, SpcfCalendar pQuarterEndDate) {
        sourceSystemCode = pSourceSystemCode;
        sourceCompanyId = pSourceCompanyId;
        paymentTemplateCd = pPaymentTemplateCd;
        quarterEndDate = pQuarterEndDate;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(sourceSystemCode, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        company = Company.findCompany(sourceCompanyId, sourceSystemCode);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, sourceCompanyId,
                                                               sourceSystemCode.toString(), sourceCompanyId);
            return validationResult;
        }

        if (StringUtils.isEmpty(paymentTemplateCd)) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.Company, sourceCompanyId, "paymentTemplateCd");
            return validationResult;
        }
        PaymentTemplate paymentTemplate = PaymentTemplate.findPaymentTemplate(paymentTemplateCd);
        if (paymentTemplate == null) {
            validationResult.getMessages().PaymentTemplateDoesNotExist(EntityName.Company, sourceCompanyId, paymentTemplateCd);
            return validationResult;
        }

        /* In Happy Land, ATR balances are always positive for every quarter.  In real life, however, they are not.
        * Many of the places where they are not are from bugs, but need to prevent compounding the problem.
        * Additionally, FUTA is paid annually so it may be neg one quarter and pos another.
        * Finally, we might have negative for one law on the template and positive for another.  This is allowed, but probably won't happen.
        *
        * Not allowed (this usually indicates that the payment is in the wrong quarter, so manual research needs to be done):
        * Q1 FIT -10
        * Q2 FIT 20
        *
        * Not allowed (year crossing not allowed):
        * 2012 Q4 FUTA -10
        * 2013 Q1 FUTA 20
        *
        * Allowed:
        * Q1 FIT -10
        * Q1 FICA 20
        *
        * Allowed:
        * 2012 Q1 FUTA -10
        * 2012 Q4 FUTA 20
        *
        * */

        //First we must verify we are not in a disallowed scenario.
        Map<SpcfCalendar, Map<Law, SpcfMoney>> ledgerBalancesByQuarter = LedgerAccount.getLedgerAccountBalanceByTemplate(LedgerAccountCode.AgencyTaxRefund, paymentTemplate, company);
        Map<SpcfCalendar, SpcfDecimal> ledgerBalanceByTemplate = new HashMap<SpcfCalendar, SpcfDecimal>();

        for (Map.Entry<SpcfCalendar, Map<Law, SpcfMoney>> quarterEntry : ledgerBalancesByQuarter.entrySet()) {
            for (Map.Entry<Law, SpcfMoney> lawEntry : quarterEntry.getValue().entrySet()) {

                SpcfCalendar newEffectiveDate = quarterEntry.getKey().copy();
                if (paymentTemplate.isRolledUpAnnually()) {
                    newEffectiveDate = CalendarUtils.getFirstDayOfTheYearLocal(newEffectiveDate);
                }

                if (!ledgerBalanceByTemplate.containsKey(newEffectiveDate)) {
                    ledgerBalanceByTemplate.put(newEffectiveDate, SpcfMoney.ZERO);
                }
                ledgerBalanceByTemplate.put(newEffectiveDate, ledgerBalanceByTemplate.get(newEffectiveDate).add(lawEntry.getValue()));
            }
        }


        for (SpcfDecimal amount : ledgerBalanceByTemplate.values()) {
            if (amount.isLessThan(SpcfMoney.ZERO)) {
                validationResult.getMessages().GenericError(EntityName.Company, company.getSourceCompanyId(), "Cannot create TOR because Company has negative ATR on template");
                return validationResult;
            }
        }

        //Second we must figure out how to adjust the created TORs when there are negative balances (only looking at requested quarter/year)
        mBalancesToApply = new HashMap<Law, SpcfDecimal>();
        SpcfDecimal negativeBalanceTotal = SpcfMoney.ZERO;

        for (Map.Entry<SpcfCalendar, Map<Law, SpcfMoney>> quarterEntry : ledgerBalancesByQuarter.entrySet()) {
            //exclude quarters/years not requested
            if (paymentTemplate.isRolledUpAnnually() && !CalendarUtils.getFirstDayOfTheYearLocal(quarterEntry.getKey()).equals(CalendarUtils.getFirstDayOfTheYearLocal(quarterEndDate))) {
                continue;
            } else if (!paymentTemplate.isRolledUpAnnually() && !CalendarUtils.getFirstDayOfQuarter(quarterEntry.getKey()).equals(CalendarUtils.getFirstDayOfQuarter(quarterEndDate))) {
                continue;
            }

            //first try to apply 1:1
            for (Map.Entry<Law, SpcfMoney> lawEntry : quarterEntry.getValue().entrySet()) {
                if (lawEntry.getValue().isLessThan(SpcfMoney.ZERO)){
                    negativeBalanceTotal = negativeBalanceTotal.add(lawEntry.getValue());
                } else {
                    if (!mBalancesToApply.containsKey(lawEntry.getKey())) {
                        mBalancesToApply.put(lawEntry.getKey(), SpcfMoney.ZERO);
                    }
                    mBalancesToApply.put(lawEntry.getKey(), mBalancesToApply.get(lawEntry.getKey()).add(lawEntry.getValue()));
                }
            }
        }

        //if there is any negative amount, reduce the other laws in an arbitrary (but predictable) order
        //this shouldn't happen because the ATOA should be applied with a different law, but it could.
        DomainEntitySet<Law> sortedLaws = new DomainEntitySet<Law>(mBalancesToApply.keySet()).sort(Law.LawId());
        for (Law sortedLaw : sortedLaws) {
            if (negativeBalanceTotal.isZero()) {
                break;
            }
            SpcfDecimal amountToReduce = (SpcfDecimal) ObjectUtils.min(negativeBalanceTotal.abs(), mBalancesToApply.get(sortedLaw));
            negativeBalanceTotal = negativeBalanceTotal.add(amountToReduce);
            mBalancesToApply.put(sortedLaw, mBalancesToApply.get(sortedLaw).subtract(amountToReduce));
        }

        //finally verify that there is anything to do
        boolean anyBalanceToApply = false;
        for (SpcfDecimal spcfDecimal : mBalancesToApply.values()) {
            if (!spcfDecimal.isZero()) {
                anyBalanceToApply = true;
                break;
            }
        }
        if (!anyBalanceToApply) {
            //just a warning--will essentially be a no-op process
            validationResult.getMessages().GenericWarning(EntityName.Company, company.getSourceCompanyId(), "There is no balance to TOR");
        }


        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult<PayrollRun> processResult = new ProcessResult<PayrollRun>();

        PayrollRun payrollRun = null;

        for (Map.Entry<Law, SpcfDecimal> refundAmountEntry : mBalancesToApply.entrySet()) {
            if (refundAmountEntry.getValue().isGreaterThan(SpcfMoney.ZERO)) {
                if (payrollRun == null) {
                    payrollRun = PayrollRun.createAdjustmentPayrollRun(company, quarterEndDate);
                }
                SpcfCalendar settlementDate = PSPDate.getPSPTime();
                CalendarUtils.clearTime(settlementDate);
                FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(company,
                                                                                                            payrollRun,
                                                                                                            null,
                                                                                                            null,
                                                                                                            null,
                                                                                                            null,
                                                                                                            null,
                                                                                                            TransactionTypeCode.AgencyRefundTOR,
                                                                                                            new SpcfMoney(refundAmountEntry.getValue()),
                                                                                                            SettlementType.ApplyForward,
                                                                                                            settlementDate,
                                                                                                            refundAmountEntry.getKey());
                financialTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
                financialTransaction.updateFinancialTransactionState(TransactionStateCode.Completed);
            }
        }

        if (payrollRun != null) {
            PayrollTaxHelper.checkForPayrollCompletion(payrollRun);
        }

        processResult.setResult(payrollRun);
        return processResult;
    }
}
