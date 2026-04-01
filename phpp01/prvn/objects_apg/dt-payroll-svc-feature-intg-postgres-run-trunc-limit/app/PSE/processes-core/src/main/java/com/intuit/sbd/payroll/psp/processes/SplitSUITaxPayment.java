package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Marcela Villani
 * Date: Jan 25, 2012
 * Time: 1:58:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class SplitSUITaxPayment extends Process implements IProcess {

    private DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
    private MoneyMovementTransaction mmt;
    private String note;

    public SplitSUITaxPayment(List<FinancialTransaction> pFinancialTransactions, String pNote) {
        if (pFinancialTransactions != null) {
            financialTransactions.addAll(pFinancialTransactions);
        }
        note = pNote;

    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        if (financialTransactions.size() == 0) {
            validationResult.getMessages().InvalidArgument(EntityName.FinancialTransaction, "Financial Transactions", "Financial Transactions");
            return validationResult;
        }
        mmt = financialTransactions.get(0).getMoneyMovementTransaction();

        if (mmt != null) {
            PaymentTemplate paymentTemplate = mmt.getPaymentTemplate();
            if (!(paymentTemplate.getCategory().equals(PaymentTemplateCategory.SUI))) {
                validationResult.getMessages().InvalidArgument(EntityName.PaymentTemplate, paymentTemplate.getPaymentTemplateCd(), paymentTemplate.getPaymentTemplateCd());
            }
            if (!mmt.getTaxPaymentStatus().equals(TaxPaymentStatus.ATFFinalized)) {
                validationResult.getMessages().InvalidTaxPaymentStatus(EntityName.MoneyMovementTransaction, mmt.getId().toString(), mmt.getTaxPaymentStatus().toString(), TaxPaymentStatus.ATFFinalized.toString());
            }
            for (FinancialTransaction ft : financialTransactions) {
                if (!ft.getMoneyMovementTransaction().equals(mmt)) {
                    validationResult.getMessages().AllFinancialTransactionsSameMMT();
                    break;
                }
            }
        }
        return validationResult;
    }

    public ProcessResult<MoneyMovementTransaction> process() {
        ProcessResult processResult = new ProcessResult();

        //Agency Tax Credits need to be removed before Agency Tax Credits
        financialTransactions = financialTransactions.sort(FinancialTransaction.TransactionType().TransactionTypeCd());

        for (FinancialTransaction financialTransaction : financialTransactions) {
            MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(financialTransaction);
        }

        for (FinancialTransaction financialTransaction : financialTransactions) {
            MoneyMovementTransaction.addFinancialTransactionToTaxPaymentMMT(financialTransaction);
        }
        // If Splitting the whole payment the new mmt needs to be on hold
        MoneyMovementTransaction mmt = financialTransactions.get(0).getMoneyMovementTransaction();
        if (note == null) {
            note = "Payment amount adjustment - payment split.";
        }

        // remove back date on hold reason if a new mmt was created
        if(mmt.isCreatedInCurrentSession() && mmt.getActiveOnHoldReason(PaymentOnHoldReason.BackDate) != null) {
            MoneyMovementTransaction.removeTaxPaymentOnHoldReason(mmt, PaymentOnHoldReason.BackDate);
        }

        TaxPaymentOnHoldReason onHoldReason = mmt.getActiveOnHoldReason(PaymentOnHoldReason.Agent);
        if (onHoldReason == null) {
            mmt.addTaxPaymentOnHoldReason(PaymentOnHoldReason.Agent, note);
        } else {
            onHoldReason.setNote(note);
        }
        mmt = Application.save(mmt);
        processResult.setResult(mmt);
        return processResult;
    }
}