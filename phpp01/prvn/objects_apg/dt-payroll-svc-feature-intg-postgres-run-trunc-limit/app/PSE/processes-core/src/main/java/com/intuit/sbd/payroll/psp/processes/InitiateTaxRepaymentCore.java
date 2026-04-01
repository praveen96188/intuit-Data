package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Jan 21, 2011
 * Time: 3:13:17 PM
 */
public class InitiateTaxRepaymentCore extends Process implements IProcess {
    private MoneyMovementTransaction moneyMovementTransaction;
    private String mmtId;
    private SpcfCalendar newInitiationDate;
    private boolean recreate;

    public InitiateTaxRepaymentCore(String id, SpcfCalendar pNewInitiationDate, boolean pRecreate) {
        mmtId = id;
        newInitiationDate = pNewInitiationDate;
        recreate = pRecreate;
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
        if (newInitiationDate == null) {
            result.getMessages().RequiredInputMissingOrBlank(null, null, "New Initiation Date");
        }
        if (!result.isSuccess()) {
            return result;
        }
        if (!(moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.RejectedByAgency) ||
                moneyMovementTransaction.getTaxPaymentStatus().equals(TaxPaymentStatus.ReturnedTaxNotPaid))) {
            result.getMessages().PaymentStatusDoesNotMatch(EntityName.MoneyMovementTransaction, "Payment Status", "ReturnedTaxNotPaid or RejectedByAgency");
        }
        if (!moneyMovementTransaction.getStatus().equals(PaymentStatus.Executed)) {
            result.getMessages().StatusDoesNotMatch(EntityName.MoneyMovementTransaction, "Status", "Executed");
        }

        CalendarUtils.clearTime(newInitiationDate);
        SpcfCalendar nextOffloadDate = MoneyMovementTransaction.getNextInitiationDate(moneyMovementTransaction.getMoneyMovementPaymentMethod()).copy().toLocal();
        CalendarUtils.clearTime(nextOffloadDate);
        if (newInitiationDate.before(nextOffloadDate)) {
            result.getMessages().NewInitiationDateBeforeOffloadDate(EntityName.MoneyMovementTransaction, "Initiation Date", newInitiationDate, nextOffloadDate);
        }
        return result;
    }

    @Override
    public ProcessResult<MoneyMovementTransaction> process() {
        ProcessResult<MoneyMovementTransaction> processResult = new ProcessResult<MoneyMovementTransaction>();
        //create New money movement transaction if required.  otherwise will rely on normal adding of state to select correct mmt
        MoneyMovementTransaction newMmt = null;
        if (!recreate) {
            newMmt = new MoneyMovementTransaction();
            newMmt.setCompany(moneyMovementTransaction.getCompany());
            Application.save(newMmt);
            newMmt.setMoneyMovementPaymentMethod(null);
            newMmt.setStatus(PaymentStatus.Created);
            newMmt.setDueDate(moneyMovementTransaction.getDueDate());
            newMmt.setMoneyMovementTransactionAmount(moneyMovementTransaction.getMoneyMovementTransactionAmount());
            newMmt.setPaymentFrequency(moneyMovementTransaction.getPaymentFrequency());
            newMmt.setPaymentTemplate(moneyMovementTransaction.getPaymentTemplate());
            newMmt.setPaymentPeriodBegin(moneyMovementTransaction.getPaymentPeriodBegin());
            newMmt.setPaymentPeriodEnd(moneyMovementTransaction.getPaymentPeriodEnd());
            newMmt.setMoneyMovementPaymentMethod(moneyMovementTransaction.getMoneyMovementPaymentMethod());
            newMmt.updateTaxInitiationDate(newInitiationDate);
            newMmt.setOriginalInitiationDate(moneyMovementTransaction.getOriginalInitiationDate());
            newMmt.setTaxPaymentStatus(TaxPaymentStatus.ReadyToSend);
            newMmt.setAgencyTaxpayerId(moneyMovementTransaction.getAgencyTaxpayerId());
			newMmt.setOriginalTransaction(moneyMovementTransaction);
            newMmt.setTaxPaymentStatusEffectiveDate(PSPDate.getPSPTime());
            newMmt.setReferenceNumber(moneyMovementTransaction.getReferenceNumber());
            newMmt.setTransactionNumber(moneyMovementTransaction.getNextTransactionNumber());
            newMmt.setManualPaymentStatus(moneyMovementTransaction.getManualPaymentStatus());
            newMmt.setDepositFrequencyFk(moneyMovementTransaction.getDepositFrequencyFk());
            Application.save(newMmt);
        }

        //calculate settlement date
        SpcfCalendar newSettlementDate = newInitiationDate.copy();
        CalendarUtils.addBusinessDays(newSettlementDate, MoneyMovementTransaction.getPaymentMethodDayOffset(moneyMovementTransaction.getMoneyMovementPaymentMethod(),moneyMovementTransaction.getPaymentTemplate()));
        for (FinancialTransaction financialTransaction : moneyMovementTransaction.getFinancialTransactionCollection()) {
            FinancialTransaction newFt = new FinancialTransaction();
            newFt.setFinancialTransactionAmount(financialTransaction.getFinancialTransactionAmount());
            //setting new settlement date
            newFt.setSettlementDate(newSettlementDate);
            newFt.setSettlementTypeCd(financialTransaction.getSettlementTypeCd());
            newFt.setCreditBankAccountType(financialTransaction.getCreditBankAccountType());
            newFt.setCreditBankAccount(financialTransaction.getCreditBankAccount());
            newFt.setDebitBankAccountType(financialTransaction.getDebitBankAccountType());
            newFt.setDebitBankAccount(financialTransaction.getDebitBankAccount());
            newFt.updateOnHold(financialTransaction.getOnHold());
            newFt.setSku(financialTransaction.getSku());
            newFt.setSkuQuantity(financialTransaction.getSkuQuantity());
            newFt.setOriginalSettlementDate(financialTransaction.getOriginalSettlementDate());
            newFt.setRefundType(financialTransaction.getRefundType());
            newFt.setBillingDetail(financialTransaction.getBillingDetail());
            newFt.setBillPaymentSplit(financialTransaction.getBillPaymentSplit());
            newFt.setCompanyAdjustmentSubmission(financialTransaction.getCompanyAdjustmentSubmission());
            newFt.setCompanyLaw(financialTransaction.getCompanyLaw());
            newFt.setLaw(financialTransaction.getLaw());
            newFt.setQbdtTransactionInfo(financialTransaction.getQbdtTransactionInfo());
            if(financialTransaction.getQbdtTransactionInfo() != null) {
                financialTransaction.getQbdtTransactionInfo().setFinancialTransaction(newFt);
            }
            newFt.setTaxPenaltyInterest(financialTransaction.getTaxPenaltyInterest());
            newFt.setCompany(financialTransaction.getCompany());
            newFt.setRelatableTransaction(financialTransaction.getRelatableTransaction());
            newFt.setPayrollRun(financialTransaction.getPayrollRun());
            if (financialTransaction.getPayrollRun() != null) {
                financialTransaction.getPayrollRun().getFinancialTransactionCollection().add(newFt);
            }
            newFt.setPaycheckSplit(financialTransaction.getPaycheckSplit());
            newFt.setTransactionType(financialTransaction.getTransactionType());

            if (recreate) {
                //it will create the MMT as the highest priority instead of the existing; so need to update FT accordingly
                newFt.updateSettlementType(FinancialTransaction.getDefaultTaxSettlementType(moneyMovementTransaction.getCompany(), newFt.getLaw().getPaymentTemplate()));
            }

            Application.save(newFt);

            if (!recreate) {
                FinancialTransactionState financialTransactionState = new FinancialTransactionState();
                financialTransactionState.setFinancialTransaction(newFt);
                financialTransactionState.setCompany(newFt.getCompany());
                financialTransactionState.setTransactionType(newFt.getTransactionType());
                financialTransactionState.setTransactionState(TransactionState.findTransactionState(TransactionStateCode.Created));
                financialTransactionState.setTransactionStateEffectiveDate(PSPDate.getTimeZoneIndependentDate(PSPDate.getPSPTime()));

                newFt.setCurrentTransactionState(TransactionState.findTransactionState(TransactionStateCode.Created));
                newFt.setMoneyMovementTransaction(newMmt);
                //noinspection ConstantConditions
                newMmt.addFinancialTransaction(newFt);

                Application.save(financialTransactionState);
            } else {
                newFt.addTaxPaymentTransactionState(TransactionState.findTransactionState(TransactionStateCode.Created));
                newMmt = newFt.getMoneyMovementTransaction();
            }

            newFt.setOriginalTransaction(financialTransaction);
            financialTransaction.addAssociatedTransactions(newFt);
        }

        //noinspection ConstantConditions
        newMmt.setOriginalTransaction(moneyMovementTransaction);

        if (recreate) {
            newMmt.recalculatePaymentMethod();
        }

        if (!recreate && newMmt.getMoneyMovementPaymentMethod() == PaymentMethod.ACHCredit) {
            //need to create EDRs if just copied.
            MoneyMovementTransaction.recreateEntryDetailRecords(newMmt);
        }

        processResult.setResult(newMmt);

        //Cancel old money movement transaction
        moneyMovementTransaction.setStatus(PaymentStatus.Canceled);

        // delete existing ones
        while (moneyMovementTransaction.getEntryDetailRecordCollection().size() > 0) {
            EntryDetailRecord entryDetailRec = moneyMovementTransaction.getEntryDetailRecordCollection().get(0);
            moneyMovementTransaction.removeEntryDetailRecord(entryDetailRec);
            Application.delete(entryDetailRec);
        }

        Application.save(moneyMovementTransaction);
        return processResult;
    }
}

