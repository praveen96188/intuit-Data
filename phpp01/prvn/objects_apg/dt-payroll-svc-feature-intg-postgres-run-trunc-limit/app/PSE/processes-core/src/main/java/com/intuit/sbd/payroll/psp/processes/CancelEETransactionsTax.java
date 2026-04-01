package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LiabilityAdjustmentDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Feb 23, 2009
 * Time: 10:36:57 AM
 */
public class CancelEETransactionsTax extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCd = null;
    private String mCompanyId = null;
    private PayrollRun mPayrollRun = null;
    private Collection<Paycheck> paychecksToCancel;
    private TransactionCancelEEDTO mTransactionCancelEEDTO = null;
    private Map<Law, SpcfDecimal> cancellationAmountsByLawId = new HashMap<Law, SpcfDecimal>();
    private final Map<Law, SpcfDecimal> positiveLiabilitiesToMoveToNewPayroll = new HashMap<> ();

    private boolean isHistorical = false;
    private Company mCompany;

    public CancelEETransactionsTax(SourceSystemCode pSourceSystemCd, String pCompanyId,
                                   TransactionCancelEEDTO pDto) {
        mSourceSystemCd = pSourceSystemCd;
        mCompanyId = pCompanyId;
        mTransactionCancelEEDTO = pDto;
        paychecksToCancel = new ArrayList<Paycheck>();
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        mCompany = Company.findCompany(mCompanyId, mSourceSystemCd);
        mPayrollRun = PayrollRun.findPayrollRun(mCompany, mTransactionCancelEEDTO.getSourcePayrollRunId());
        SpcfCalendar paycheckDate = mPayrollRun.getPaycheckDate();

        isHistorical = mPayrollRun.isHistoricalPayroll();

        if(mPayrollRun.hasTaxImpoundOffloaded()) {
            validationResult.getMessages().GenericError(
                    EntityName.PayrollRun,
                    mPayrollRun.getSourcePayRunId(),
                    "Impound transaction associated to Payroll:" + mPayrollRun.getSourcePayRunId() + " is no longer pending and can not be cancelled.");
            return validationResult;
        }

        if (mTransactionCancelEEDTO.getSourcePaycheckIdList() != null && mTransactionCancelEEDTO.getSourcePaycheckIdList().size() > 0) {
            // paychecks are eager loaded in calling class
            for (String paycheckId : mTransactionCancelEEDTO.getSourcePaycheckIdList()) {
                Paycheck paycheck = Paycheck.findPaycheck(mCompany, paycheckId);
                if (paycheck.isVoidedOrRecalled()) {
                    validationResult.getMessages().PaycheckAlreadyCanceled(EntityName.PayCheck, paycheck.getSourcePaycheckId(), paycheck.getSourcePaycheckId());
                    continue;
                }

                if (paycheck.getTaxCollection() != null && paycheck.getTaxCollection().size() > 0) {
                    paychecksToCancel.add(paycheck);
                }
            }

            // Get the cancellation Amounts by Law Id for Adjustments. todo remove this it is for Gemini only
            if (mTransactionCancelEEDTO.getPayrollTaxes() != null && mTransactionCancelEEDTO.getPayrollTaxes().size() > 0) {
                for (LiabilityAdjustmentDTO liabilityAdjustmentDTO : mTransactionCancelEEDTO.getPayrollTaxes()) {
                    SpcfDecimal amount = liabilityAdjustmentDTO.getAmount();
                    Law law = Application.findById(Law.class, liabilityAdjustmentDTO.getLawId());
                    if (amount != null) {
                        amount.negate();
                        if (cancellationAmountsByLawId.get(law) != null) {
                            amount = amount.add(cancellationAmountsByLawId.get(law));
                        }
                        cancellationAmountsByLawId.put(law, amount);
                    }

                    LiabilityAdjustment liabilityAdjustment = new LiabilityAdjustment();
                    liabilityAdjustment.setCompany(mCompany);                    
                    liabilityAdjustment.setAmount(liabilityAdjustmentDTO.getAmount());
                    liabilityAdjustment.setEffectiveDate(DateDTO.convertToSpcfCalendar(liabilityAdjustmentDTO.getEffectiveDate()));
                    liabilityAdjustment.setLaw(law);
                    liabilityAdjustment.setPayrollRun(mPayrollRun);
                    liabilityAdjustment = Application.save(liabilityAdjustment);
                    mPayrollRun.addLiabilityAdjustment(liabilityAdjustment);
                }
            }
        } else {
            for (Paycheck paycheck : mPayrollRun.getPaycheckCollection()) {
                if (paycheck.getTaxCollection() != null && paycheck.getTaxCollection().size() > 0) {
                    paychecksToCancel.add(paycheck);
                }
            }
        }

        CompanyLaw nationalPaidLeaveLaw = CompanyLaw.findCompanyLaw(mPayrollRun.getCompany(), Law.NATIONAL_PAID_LEAVE_CREDIT);
        if (nationalPaidLeaveLaw != null && mPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.Law().equalTo(nationalPaidLeaveLaw.getLaw())).isNotEmpty()) {
            // positive amounts will create a LA below
            SpcfDecimal cancelledCreditAmount = PayrollSubmitTax.calculateNationalPaidLeaveCreditAmount(paychecksToCancel).negate();
            positiveLiabilitiesToMoveToNewPayroll.put(nationalPaidLeaveLaw.getLaw(), cancelledCreditAmount.add(positiveLiabilitiesToMoveToNewPayroll.computeIfAbsent(nationalPaidLeaveLaw.getLaw(), law -> SpcfMoney.ZERO)));
        }

        CompanyLaw employeeRetentionLeaveLaw = CompanyLaw.findCompanyLaw(mPayrollRun.getCompany(), Law.EMPLOYEE_RETENTION_CREDIT);
        if (employeeRetentionLeaveLaw != null && mPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.Law().equalTo(employeeRetentionLeaveLaw.getLaw())).isNotEmpty()) {
            // positive amounts will create a LA below
            SpcfDecimal cancelledCreditAmount = PayrollSubmitTax.calculateEmployeeRetentionCreditAmount(paycheckDate, paychecksToCancel).negate();
            positiveLiabilitiesToMoveToNewPayroll.put(employeeRetentionLeaveLaw.getLaw(), cancelledCreditAmount.add(positiveLiabilitiesToMoveToNewPayroll.computeIfAbsent(employeeRetentionLeaveLaw.getLaw(), law -> SpcfMoney.ZERO)));
        }

        CompanyLaw eeFicaDeferralLaw = CompanyLaw.findCompanyLaw(mPayrollRun.getCompany(), Law.FICA_EE_DEFERRAL_CREDIT);
        if (eeFicaDeferralLaw != null && mPayrollRun.getFinancialTransactionCollection().find(FinancialTransaction.Law().equalTo(eeFicaDeferralLaw.getLaw())).isNotEmpty()) {
            // positive amounts will create a LA below
            SpcfDecimal cancelledCreditAmount = PayrollSubmitTax.calculateEeFicaDeferralCreditAmount(paychecksToCancel).negate();
            positiveLiabilitiesToMoveToNewPayroll.put(eeFicaDeferralLaw.getLaw(), cancelledCreditAmount.add(positiveLiabilitiesToMoveToNewPayroll.computeIfAbsent(eeFicaDeferralLaw.getLaw(), key -> SpcfMoney.ZERO)));
        }

        for (Paycheck paycheck : paychecksToCancel) {
            for (Tax tax : paycheck.getTaxCollection()) {
                Law law = tax.getLaw();

                if(law.isCOBRA()) {
                    continue;
                }

                SpcfDecimal amount = tax.getTaxLiabilityAmount().negate();
                if (cancellationAmountsByLawId.get(law) != null) {
                    amount = amount.add(cancellationAmountsByLawId.get(law));
                }
                cancellationAmountsByLawId.put(law, amount);

                DomainEntitySet<FinancialTransaction> executedAgencyTaxCredits =
                        mPayrollRun.findFinancialTransactionsByLawTypeState(law, TransactionTypeCode.AgencyTaxCredit, TransactionStateCode.Returned, TransactionStateCode.Executed, TransactionStateCode.Completed);

                DomainEntitySet<FinancialTransaction> executedAgencyDirectCredits =
                        mPayrollRun.findFinancialTransactionsByLawTypeState(law, TransactionTypeCode.AgencyDirectCredit, TransactionStateCode.Returned, TransactionStateCode.Executed, TransactionStateCode.Completed);

                boolean hasNonPendingTaxCredits = executedAgencyTaxCredits.size() > 0 || executedAgencyDirectCredits.size() > 0;
                if(hasNonPendingTaxCredits && amount.isGreaterThan(SpcfMoney.ZERO) && !isHistorical &&
                        tax.getLaw().getPaymentTemplate().isSupportedAsOfDate(paycheckDate)) {
                    validationResult.getMessages().GenericError(
                            EntityName.Paycheck,
                            paycheck.getSourcePaycheckId(),
                            "Tax transaction associated to Paycheck:" + paycheck.getSourcePaycheckId() + " is no longer pending and can not be cancelled.");
                }
            }
        }

        CompanyLaw ficaErDeferralCreditLaw = CompanyLaw.findCompanyLaw(mPayrollRun.getCompany(), Law.FICA_ER_DEFERRAL_CREDIT);
        if (mPayrollRun.getPaycheckDate().between(PayrollSubmitTax.FICA_DEFERRAL_BEGIN, PayrollSubmitTax.FICA_DEFERRAL_END) && ficaErDeferralCreditLaw != null) {
            Map<Law, SpcfDecimal> financialTransactionAmounts = mPayrollRun.getFinancialTransactionLiabilityBalancesByLaw();
            if (financialTransactionAmounts.containsKey(ficaErDeferralCreditLaw.getLaw())) {
                // only cancel the amount of credit available
                positiveLiabilitiesToMoveToNewPayroll.put(ficaErDeferralCreditLaw.getLaw(),
                                                          // using max because both amounts are negative, so we want the smaller absolute value
                                                          financialTransactionAmounts.get(ficaErDeferralCreditLaw.getLaw()).max(cancellationAmountsByLawId.get(Law.getFicaErLaw())).negate());
            }
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        if (isHistorical) {
            if (mTransactionCancelEEDTO.getSourcePaycheckIdList() != null &&
                    mTransactionCancelEEDTO.getSourcePaycheckIdList().size() > 0 &&
                    mTransactionCancelEEDTO.getSourcePaycheckIdList().size() != mPayrollRun.getPaycheckCollection().size()) {
                // do this only if we are voided some of the paychecks
                for (String sourceId : mTransactionCancelEEDTO.getSourcePaycheckIdList()) {
                    Paycheck paycheck = Paycheck.findPaycheck(mCompany, sourceId);
                    paycheck.setStatus(PaycheckStatusCode.Inactive);
                    Application.save(paycheck);
                }
            } else {
                mPayrollRun.setPayrollRunStatus(PayrollStatus.Canceled);
                mPayrollRun.setStatusEffectiveDate(PSPDate.getPSPTime());
                for (Paycheck paycheck : mPayrollRun.getPaycheckCollection()) {
                    paycheck.setStatus(PaycheckStatusCode.Inactive);
                    Application.save(paycheck);
                }
                Application.save(mPayrollRun);
            }
        }

        // void hpde transactions todo remove this it is for Gemini only
        SpcfDecimal[] hpdeERAmount = new SpcfDecimal[1];
        cancelHPDETransactions(hpdeERAmount);
        
        if(!isHistorical) {
            Map<Law, SpcfDecimal> negativeLiabilityOffsets = new HashMap<>();

            // Move positive liabilities to a new payroll so we can debit for them. Create negative liability adjustments as an offset.
            for (Law law : cancellationAmountsByLawId.keySet()) {
                if (!law.getLawId().equals(Law.COBRA) && cancellationAmountsByLawId.get(law).isGreaterThan(SpcfMoney.ZERO)) {
                    negativeLiabilityOffsets.put(law, cancellationAmountsByLawId.get(law));
                    positiveLiabilitiesToMoveToNewPayroll.put(law, cancellationAmountsByLawId.get(law));
                    cancellationAmountsByLawId.put(law, SpcfMoney.ZERO);
                }
            }

            // record negative liability adjustments for positive liabilities being moved to a new payroll, no effect on FTs
            processResult.merge(PayrollTaxHelper.createNegativeLiabilityAdjustments(mPayrollRun, negativeLiabilityOffsets));

            processResult.merge(PayrollTaxHelper.updateTaxTransactions(mPayrollRun, cancellationAmountsByLawId));

            // create a new payroll for the positive liability, will create FTs
            processResult.merge(PayrollTaxHelper.createPayrollForPositiveLiability(mPayrollRun, positiveLiabilitiesToMoveToNewPayroll));
        }

        //Handle the case where this payroll was originally inserted in a Completed state because there wasn't an impound and is now being recalled.
        // In this case, we need to update ATF, so put it into ATF's processing table (again)
        if (mPayrollRun.getPayrollRunStatus() == PayrollStatus.Complete && ATFPayrollsToProcess.payrollRunExistsForProcessing(mPayrollRun)) {
            ATFPayrollsToProcess newPayrollToProcess = new ATFPayrollsToProcess();
            newPayrollToProcess.setPayrollRun(mPayrollRun);
            Application.save(newPayrollToProcess);
        }

        return processResult;
    }

    private SpcfDecimal getPendingDebitAmount() {
        DomainEntitySet<FinancialTransaction> pendingDebits =
                mPayrollRun.findFinancialTransactionsByLawTypeState(null, TransactionTypeCode.AgencyDirectCredit, TransactionStateCode.Created);
        pendingDebits.addAll(mPayrollRun.findFinancialTransactionsByLawTypeState(null, TransactionTypeCode.EmployerTaxDebit, TransactionStateCode.Created));

        SpcfDecimal debitAmount = SpcfMoney.ZERO;
        for (FinancialTransaction pendingDebit : pendingDebits) {
            debitAmount = debitAmount.add(pendingDebit.getFinancialTransactionAmount());
        }
        return debitAmount;
    }

    private Map<Law, SpcfDecimal> cancelHPDETransactions(SpcfDecimal[] erAmountContainer) {
        Map<Law, SpcfDecimal> lawAmounts = new HashMap<Law, SpcfDecimal>();
        SpcfDecimal erAmount = SpcfMoney.ZERO;
        for (FinancialTransaction transaction : mPayrollRun.getFinancialTransactionCollection()) {
            switch (transaction.getTransactionType().getTransactionTypeCd()) {
                case AgencyHPDETaxPayment:
                    switch (transaction.getCurrentTransactionState().getTransactionStateCd()) {
                        case Completed:
                            transaction.cancelFinancialTransaction();
                            addLawAmount(lawAmounts, transaction.getLaw(), transaction.getFinancialTransactionAmount());
                            break;
                    }
                    break;
                case AgencyCreditBalanceCarryForwardDebit:
                    switch (transaction.getSettlementTypeCd()) {
                        case EFE:
                            lawAmounts.put(transaction.getLaw(), transaction.getFinancialTransactionAmount());
                            transaction.cancelFinancialTransaction();
                            addLawAmount(lawAmounts, transaction.getLaw(), transaction.getFinancialTransactionAmount());
                            break;
                    }
                    break;
                case EmployerCreditBalanceCarryForwardCredit:
                    switch (transaction.getSettlementTypeCd()) {
                        case ACH:
                            erAmount = transaction.getFinancialTransactionAmount();
                            transaction.cancelFinancialTransaction();
                            break;
                    }
                    break;
            }
        }
        erAmountContainer[0] = erAmount;
        Application.save(mPayrollRun);
        return lawAmounts;
    }

    private void addLawAmount(Map<Law, SpcfDecimal> amounts, Law law, SpcfDecimal transactionAmount) {
        SpcfDecimal currentAmount = amounts.get(law);
        if (currentAmount == null) {
            amounts.put(law, transactionAmount);
        } else {
            amounts.put(law, currentAmount.add(transactionAmount));
        }
    }

}
