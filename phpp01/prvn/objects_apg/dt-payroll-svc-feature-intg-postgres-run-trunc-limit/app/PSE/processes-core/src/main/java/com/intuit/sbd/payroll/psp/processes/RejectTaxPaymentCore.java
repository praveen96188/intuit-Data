package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jul 20, 2011
 * Time: 11:21:57 AM
 */
public class RejectTaxPaymentCore extends Process implements IProcess {
    private MoneyMovementTransaction moneyMovementTransaction;
    private String mmtId;
    private String rejectionReason;

    public RejectTaxPaymentCore(String pId, String pReason) {
        mmtId = pId;
        rejectionReason = pReason;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();
        if (mmtId == null || mmtId.length() == 0) {
            result.getMessages().RequiredInputMissingOrBlank(EntityName.MoneyMovementTransaction, "Money Movement Transaction Id", "Money Movement Transaction ID");
        } else {
            moneyMovementTransaction = Application.findById(MoneyMovementTransaction.class, SpcfUniqueId.createInstance(mmtId));
            if (moneyMovementTransaction == null) {
                result.getMessages().NoEntityWithGivenId("MoneyMovementTransaction", mmtId);
            }
        }
        if (!result.isSuccess()) {
            return result;
        }
        if (!(moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.SentToAgency) ||
                moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.AcknowledgedByAgency))) {
            result.getMessages().PaymentStatusDoesNotMatch(EntityName.MoneyMovementTransaction, "Payment Status", "AcknowledgedByAgency or SentToAgency");
        }
        if (!moneyMovementTransaction.getStatus().equals(PaymentStatus.Executed)) {
            result.getMessages().StatusDoesNotMatch(EntityName.MoneyMovementTransaction, "Status", "Executed");
        }
        
        if (!(moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.CheckPayment || moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit
                || moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.SuperCheck || moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.ACHDebit)){
            result.getMessages().PaymentMethodDoesNotMatch(EntityName.MoneyMovementTransaction, "Payment Method", "CheckPayment, SuperCheck, ACHDebit or ACHCredit");
        }
        return result;
    }

    @Override
    public ProcessResult<MoneyMovementTransaction> process() {
        ProcessResult<MoneyMovementTransaction> processResult = new ProcessResult<MoneyMovementTransaction>();

        if (moneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.SuperCheck) {
            for (PaymentBatchAssoc paymentBatchAssoc : PaymentBatchAssoc.findPaymentBatchAssocsByBatch(moneyMovementTransaction.getAgencyCheckBatch(), true)) {
                reject(paymentBatchAssoc.getMoneyMovementTransaction());
            }

            VoidedCheck voidedCheck = new VoidedCheck();
            voidedCheck.setCompany(moneyMovementTransaction.getCompany());
            voidedCheck.setAgencyCheckBatch(moneyMovementTransaction.getAgencyCheckBatch());
            voidedCheck.setReason(rejectionReason);
            Application.save(voidedCheck);
        } else {
            reject(moneyMovementTransaction);
        }

        return processResult;
    }

    private void reject(MoneyMovementTransaction pMoneyMovementTransaction)  {
        TaxPaymentStatus oldTaxPaymentStatus = pMoneyMovementTransaction.getTaxPaymentStatus();
        pMoneyMovementTransaction.setTaxPaymentStatus(TaxPaymentStatus.RejectedByAgency);

        //Update All FTs status to Returned
        for (FinancialTransaction financialTransaction : pMoneyMovementTransaction.getFinancialTransactionCollection()) {
            financialTransaction.addTransactionState(TransactionState.findTransactionState(TransactionStateCode.Returned));
        }
        Application.save(pMoneyMovementTransaction);

        //Create company event for Tax payment status update
        CompanyEvent.createTaxPaymentStatusChangeEvent(pMoneyMovementTransaction, oldTaxPaymentStatus, rejectionReason);

        if(pMoneyMovementTransaction.getMoneyMovementPaymentMethod() == PaymentMethod.CheckPayment) {
            VoidedCheck voidedCheck = new VoidedCheck();
            voidedCheck.setMoneyMovementTransaction(pMoneyMovementTransaction);
            voidedCheck.setCompany(pMoneyMovementTransaction.getCompany());
            voidedCheck.setReason(rejectionReason);
            Application.save(voidedCheck);
        }
    }
}
