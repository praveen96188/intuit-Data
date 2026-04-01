package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Marcela Villani
 * Date: Jan 25, 2012
 * Time: 1:58:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class FinalizeSUITaxPayment extends Process implements IProcess {

    private DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactions = new DomainEntitySet<MoneyMovementTransaction>();
    private DomainEntitySet<MoneyMovementTransaction> moneyMovementTransactionsOnHoldAndAchDebit = new DomainEntitySet<MoneyMovementTransaction>();
    private PaymentTemplate paymentTemplate;
    private int year;
    private int quarter;

    public FinalizeSUITaxPayment(List<MoneyMovementTransaction> pMoneyMovementTransactions, PaymentTemplate pPaymentTemplate, int pYear, int pQuarter) {
        if (pMoneyMovementTransactions != null) {
            moneyMovementTransactions.addAll(pMoneyMovementTransactions);
        }
        year = pYear;
        quarter = pQuarter;
        paymentTemplate = pPaymentTemplate;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        boolean validate = true;
        SpcfCalendar quarterBeginDate = CalendarUtils.getFirstDayOfQuarter(year, quarter);
        SpcfCalendar quarterEndDate = CalendarUtils.getLastDayOfQuarter(year, quarter);

        if (quarterBeginDate == null || quarterEndDate == null) {
            validationResult.getMessages().InvalidYearQuarter(Integer.toString(year), Integer.toString(quarter));
            return validationResult;
        }
        if (moneyMovementTransactions.size() == 0) {
            if (paymentTemplate == null) {
                validationResult.getMessages().InvalidArgument(EntityName.PaymentTemplate, "Payment Template", "Payment Template");
                return validationResult;
            }
            if (!(paymentTemplate.getCategory().equals(PaymentTemplateCategory.SUI)|| PaymentTemplate.isToBeFinalizedNonSUIPaymentTemplate(paymentTemplate))) {
                validationResult.getMessages().InvalidArgument(EntityName.PaymentTemplate, paymentTemplate.getPaymentTemplateCd(), paymentTemplate.getPaymentTemplateCd());
            }


            moneyMovementTransactions =
                    MoneyMovementTransaction.findTaxPayments()
                            .setPaymentTemplate(paymentTemplate)
                            .setPeriodBeginDate(quarterBeginDate)
                            .setPeriodEndDate(quarterEndDate)
                            .setReadyToSend()
                            .find();

            moneyMovementTransactionsOnHoldAndAchDebit =
                    MoneyMovementTransaction.findTaxPayments()
                                            .setPaymentTemplate(paymentTemplate)
                                            .setPeriodBeginDate(quarterBeginDate)
                                            .setPeriodEndDate(quarterEndDate)
                                            .setOnHoldAndAchDebit()
                                            .find();
            moneyMovementTransactions.addAll(moneyMovementTransactionsOnHoldAndAchDebit);
            validate = false;
        }

        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            if (validate) {
                PaymentTemplate paymentTemplate = moneyMovementTransaction.getPaymentTemplate();
                if (!(paymentTemplate.getCategory().equals(PaymentTemplateCategory.SUI)|| PaymentTemplate.isToBeFinalizedNonSUIPaymentTemplate(paymentTemplate))) {
                    validationResult.getMessages().InvalidPaymentTemplateCategory(EntityName.PaymentTemplate, paymentTemplate.getPaymentTemplateCd(), paymentTemplate.getPaymentTemplateCd(), paymentTemplate.getCategory().toString());
                }
                SpcfCalendar paymentPeriodBegin = moneyMovementTransaction.getPaymentPeriodBegin().copy().toLocal();
                CalendarUtils.clearTime(paymentPeriodBegin);

                if (!(paymentPeriodBegin.equals(quarterBeginDate))) {
                    validationResult.getMessages().InvalidMMTQuarter(EntityName.MoneyMovementTransaction, moneyMovementTransaction.getId().toString(), moneyMovementTransaction.getId().toString(), Integer.toString(year * 100 + quarter));
                }

                SpcfCalendar paymentPeriodEnd = moneyMovementTransaction.getPaymentPeriodEnd().copy().toLocal();
                CalendarUtils.clearTime(paymentPeriodEnd);
                if (!(paymentPeriodEnd.equals(quarterEndDate))) {
                    validationResult.getMessages().InvalidMMTQuarter(EntityName.MoneyMovementTransaction, moneyMovementTransaction.getId().toString(), moneyMovementTransaction.getId().toString(), Integer.toString(year * 100 + quarter));
                }

                //Allow to finalize payments when the payment is onHold and payment method being AchDebit
                if (!(moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.ReadyToSend)) && !(moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.OnHold) && moneyMovementTransaction.getMoneyMovementPaymentMethodString().equals(PaymentMethod.ACHDebit.toString()))) {
                    validationResult.getMessages().PaymentStatusDoesNotMatch(EntityName.MoneyMovementTransaction, moneyMovementTransaction.getId().toString(), TaxPaymentStatus.ReadyToSend.toString());
                }
            }
        }
        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        for (MoneyMovementTransaction moneyMovementTransaction : moneyMovementTransactions) {
            moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);
            moneyMovementTransaction.setTaxPaymentStatusEffectiveDate(PSPDate.getPSPTime());
            // If there is another Finalized mmt for the same payment template, company, payment period and payment method, combine these payments
            MoneyMovementTransaction finalizedMMT = moneyMovementTransaction.findFinalizedMMTMatch(moneyMovementTransaction);
            if ((finalizedMMT != null ) && !finalizedMMT.equals(moneyMovementTransaction)) {
                SpcfCalendar newInitiationDate =  finalizedMMT.getInitiationDate().copy().toLocal();
                CalendarUtils.clearTime(newInitiationDate);
                moneyMovementTransaction.updateTaxInitiationDate(newInitiationDate);
                // Temporarily set the finalized MMTs to ReadyToSend so they can be combined
                finalizedMMT.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
                moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
                finalizedMMT.combinePayment(moneyMovementTransaction);
                finalizedMMT.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);
                moneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.ATFFinalized);
            }
        }
        return processResult;
    }


}