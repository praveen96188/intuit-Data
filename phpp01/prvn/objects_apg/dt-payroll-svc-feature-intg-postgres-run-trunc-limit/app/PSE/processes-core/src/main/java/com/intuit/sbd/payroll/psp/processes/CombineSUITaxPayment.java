package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Marcela Villani
 * Date: Jan 25, 2012
 * Time: 1:58:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CombineSUITaxPayment extends Process implements IProcess {

    private DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
    private MoneyMovementTransaction mmt;
    private String note;

    public CombineSUITaxPayment(List<FinancialTransaction> pFinancialTransactions, String pNote) {
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
        
        if (mmt != null ) {
            PaymentTemplate paymentTemplate = mmt.getPaymentTemplate();
            if (!(paymentTemplate.getCategory().equals(PaymentTemplateCategory.SUI))) {
                validationResult.getMessages().InvalidArgument(EntityName.PaymentTemplate, paymentTemplate.getPaymentTemplateCd(), paymentTemplate.getPaymentTemplateCd());
            }
            if (!mmt.getTaxPaymentStatus().equals(TaxPaymentStatus.OnHold)) {
                validationResult.getMessages().InvalidTaxPaymentStatus(EntityName.MoneyMovementTransaction, mmt.getId().toString(), mmt.getTaxPaymentStatus().toString(), TaxPaymentStatus.OnHold.toString());
            }
            for (FinancialTransaction ft:financialTransactions) {
                if (!ft.getMoneyMovementTransaction().equals(mmt)){
                    validationResult.getMessages().AllFinancialTransactionsSameMMT();
                    break;
                }
            }
        }
        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        // Create a list of fts that need to be on hold
        ArrayList<FinancialTransaction>  onHoldFts = new ArrayList<FinancialTransaction>();
        MoneyMovementTransaction onHoldMMT = financialTransactions.getFirst().getMoneyMovementTransaction();
        for (FinancialTransaction ft:onHoldMMT.getFinancialTransactionCollection()) {
            if (!financialTransactions.contains(ft)) {
                onHoldFts.add(ft);
            }
        }
        for (FinancialTransaction financialTransaction : financialTransactions) {
              MoneyMovementTransaction.removeFinancialTransactionFromTaxPaymentMMT(financialTransaction);
        }
        // Create new MMT for the fts that will be combined
        for (FinancialTransaction financialTransaction : financialTransactions) {
            MoneyMovementTransaction.addFinancialTransactionToTaxPaymentMMT(financialTransaction);
        }
        // Call the finalize process to either create or combine this new mmt with an existing finalized one
        ArrayList<MoneyMovementTransaction> mmts = new ArrayList<MoneyMovementTransaction>();
        MoneyMovementTransaction createdMMT = financialTransactions.getFirst().getMoneyMovementTransaction();
        createdMMT.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
        mmts.add(createdMMT);
        processResult.merge(PayrollServices.paymentManager.finalizeSUIPayments(mmts,null, mmt.getPaymentPeriodBegin().getYear(),CalendarUtils.getQuarterAsInt(mmt.getPaymentPeriodBegin())));

        // Split the payment again for ONHold Fts
        if (onHoldFts.size() > 0) {
            processResult.merge(PayrollServices.paymentManager.splitSUIPayments(onHoldFts, null));
            // Add note to onHold MMT
            MoneyMovementTransaction mmt = onHoldFts.get(0).getMoneyMovementTransaction();
            TaxPaymentOnHoldReason onHoldReason = mmt.getActiveOnHoldReason(PaymentOnHoldReason.Agent);
            if (onHoldReason != null) {
                if (note == null) {
                    note = "Payment amount adjustment - payment split.";
                }
                onHoldReason.setNote(note);
            }
        }

        return processResult;
    }
}