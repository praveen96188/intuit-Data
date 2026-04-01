package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ERRefundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.ProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: wnichols
 * Date: Sep 12, 2008
 * Time: 1:00:22 PM
 */
public class AddEmployerFraudOrEscalationRefundCore extends Process implements IProcess {
    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private Collection<ERRefundDTO> mInputDTOs;

    private Company mCompany;
    private PayrollRun mPayrollRun;
    private HashMap<ERRefundDTO, FinancialTransaction> mOrigFTs = new HashMap<ERRefundDTO, FinancialTransaction>();
    private CompanyBankAccount mCBA;


    public AddEmployerFraudOrEscalationRefundCore(SourceSystemCode pSrcSystemCd, String pSrcCompanyId, Collection<ERRefundDTO> pDTOs) {
        mSourceSystemCode = pSrcSystemCd;
        mSourceCompanyId = pSrcCompanyId;
        mInputDTOs = pDTOs;
    }

    public ProcessResult validate() {
        ProcessResult result = new ProcessResult();

        // make sure company parameters are present
        result.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!result.isSuccess()) {
            return result;
        }

        // make sure the company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);
        if (mCompany == null) {
            result.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId, mSourceSystemCode.toString(),
                                                     mSourceCompanyId);
            return result;
        }

        // if there are no FTs to refund, then we're done validating
        if (mInputDTOs.isEmpty()) {
            result.getMessages().RequiredInputMissingOrBlank(EntityName.FinancialTransaction, mSourceCompanyId,
                                                             "ERRefundDTO") ;
            return result;
        }

        // for each refund to be created, validate the individual DTOs
        for (ERRefundDTO dto : mInputDTOs) {

            // make sure the debit FT exists
            if (dto.getFinancialTxId()==null || dto.getFinancialTxId().trim().length()==0) {
                result.getMessages().RequiredInputMissingOrBlank(EntityName.FinancialTransaction, mSourceCompanyId,
                                                                 "FinancialTxId") ;
                return result;
            }
            FinancialTransaction debitFT = Application.findById(FinancialTransaction.class,
                                                                SpcfUniqueId.createInstance(dto.getFinancialTxId()));
            if (debitFT == null) {
                result.getMessages().NoEntityWithGivenId("FinancialTransaction", dto.getFinancialTxId());
                return result;
            }

            // make sure all input FTs are related to the same PayrollRun
            if (mPayrollRun == null) {
                mPayrollRun = debitFT.getPayrollRun(); // first debit FT validated

                // make sure the FT is related (via the PayrollRun) to the right company
                if (! mPayrollRun.getCompany().getId().equals(mCompany.getId())) {
                    result.getMessages().InvalidArgument(EntityName.FinancialTransaction, debitFT.getId().toString(),
                                                         "FinancialTransaction.PayrollRun.Company");
                }
            } else if (! mPayrollRun.getId().equals(debitFT.getPayrollRun().getId())) {
                result.getMessages().InvalidArgument(EntityName.FinancialTransaction, debitFT.getId().toString(),
                                                     "FinancialTransaction.PayrollRun");
                return result;
            }

            // make sure the amount is in bounds
            if (dto.getFinancialTxAmt().compareTo(new SpcfMoney("0")) <= 0) {
                result.getMessages().AmountNotPositive(EntityName.Company, mSourceCompanyId);
                return result;
            } else if (dto.getFinancialTxAmt().compareTo(getMaxRefundAmount(debitFT)) > 0) {
                result.getMessages().InvalidValue(EntityName.FinancialTransaction, dto.getFinancialTxId(), "Refund Amount");
                return result;
            }

            // other validations depend on the settlement type
            if (dto.getSettlementType() == SettlementTypeDTO.ACH) {

                // make sure there's an active CompanyBankAccount
                if (mCBA == null) {
                    mCBA = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);
                    if (mCBA == null) {
                        result.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                                mSourceCompanyId, mSourceSystemCode.toString(), mSourceCompanyId);
                        return result;
                    }
                }

                if (!CompanyService.isCompanyOnDirectDepositOrTaxService(mCompany)) {
                    result.getMessages().CompanyNotAssociatedWithService(EntityName.Company, mSourceCompanyId,
                            mSourceSystemCode.toString(), mSourceCompanyId,
                            ServiceCode.DirectDeposit.toString() + " or " + ServiceCode.Tax.toString());
                    return result;
                }

            } else { // non-ACH settlement

                // make sure the transaction date is in bounds
                SpcfCalendar present = PSPDate.getPSPTime();
                CalendarUtils.clearTime(present);

                SpcfCalendar past = present.copy();
                past.addDays(-45);

                SpcfCalendar txDate = DateDTO.convertToSpcfCalendar(dto.getTxDate());
                CalendarUtils.clearTime(txDate);

                if (txDate.before(past)) {
                    result.getMessages().SettlementDateTooFarInPast(EntityName.Date, txDate.toString(), txDate.toString(),
                                                                    dto.getSettlementType().toString());
                    return result;
                } else if (txDate.after(present)) {
                    result.getMessages().SettlementDateTooFarInFuture(EntityName.Date, txDate.toString(), txDate.toString(),
                                                                      dto.getSettlementType().toString());
                    return result;
                }
            }

            mOrigFTs.put(dto, debitFT);
        }

        // make sure this action is valid
        ActionEvent action = PayrollServices.entityFinder.findById(ActionEvent.class, ActionEventCode.ERFraudOrEscalationRefund);
        if (! mPayrollRun.validateAction(action)) {
            result.getMessages().ActionNotValidForPayrollRun(EntityName.PayrollRun,
                                                             mPayrollRun.getSourcePayRunId(),
                                                             ActionEventCode.ERFraudOrEscalationRefund.toString(),
                                                             mPayrollRun.getSourcePayRunId(),
                                                             mPayrollRun.getPayrollRunStatus().toString());
            return result;
        }

        // make sure there are no pending refunds against this payroll
        DomainEntitySet<FinancialTransaction> refundFTs = mPayrollRun.getFinancialTransactions(TransactionType.getRefundTypes(),
                                                                   new TransactionStateCode[]{TransactionStateCode.Created});
        if (refundFTs.size() > 0) {
            result.getMessages().ActionNotValidForPayrollRunLedgerAccount(EntityName.PayrollRun,
                                                                          mPayrollRun.getSourcePayRunId(),
                                                                          "ER Fraud/Escalation Refund",
                                                                          mPayrollRun.getSourcePayRunId());
            return result;
        }

        return result;
    }

    public ProcessResult process() {
        ProcessResult<FinancialTransaction> result = new ProcessResult<FinancialTransaction>();

        for (ERRefundDTO dto : mInputDTOs) {
            FinancialTransaction debitFT = mOrigFTs.get(dto);

            //tax only if the txn is ONLY a tax transaction
            TransactionTypeCode ftTypeCd;
            if (debitFT.getTransactionType().getServiceCollection().size() == 1 && debitFT.getTransactionType().getServiceCollection().getFirst().getServiceCd() == ServiceCode.Tax) {
                ftTypeCd = TransactionTypeCode.EmployerTaxFraudOrEscalationRefundCredit;
            } else {
                ftTypeCd = TransactionTypeCode.EmployerFraudOrEscalationRefundCredit;
            }
            IntuitBankAccount iba = IntuitBankAccount.findIntuitBankAccount(ftTypeCd, CreditDebitCode.Debit);

            SettlementType sType = ProcessesToDTO.getDomainSettlementType(dto.getSettlementType());
            SpcfCalendar sDate;
            BankAccount creditBA;
            BankAccountOwnerType creditOwnerType;
            if (sType == SettlementType.ACH) {

                sDate = FinancialTransaction.getSettlementDate(mCompany.getOffloadGroup());
                creditBA = mCBA.getBankAccount();
                creditOwnerType = BankAccountOwnerType.Company;

            } else { // non-ACH settlement

                sDate = DateDTO.convertToSpcfCalendar(dto.getTxDate());
                creditBA = null;
                creditOwnerType = null;

            }

            // create the transaction
            FinancialTransaction refundFT = FinancialTransaction.createFinancialTransaction(mCompany,
                                                                                           mPayrollRun,
                                                                                           null,
                                                                                           creditBA,
                                                                                           iba.getBankAccount(),
                                                                                           creditOwnerType,
                                                                                           BankAccountOwnerType.Intuit, // debit BA owner type
                                                                                           ftTypeCd,
                                                                                           dto.getFinancialTxAmt(),
                                                                                           sType,
                                                                                           sDate,
                                                                                           debitFT.getSku(),
                                                                                           debitFT, // orig FT
                                                                                           0); // sku quantity

            // if non-ACH, advance the state through Executed to Completed
            if (sType != SettlementType.ACH) {
                refundFT = refundFT.updateFinancialTransactionState(TransactionStateCode.Executed);
                refundFT = refundFT.updateFinancialTransactionState(TransactionStateCode.Completed);
            }
        }

        // all done
        return result;
    }


    private SpcfMoney getMaxRefundAmount(FinancialTransaction pDebitFT) {
        TransactionSummary summary = pDebitFT.summarizeRelatedTransactions();
        return new SpcfMoney(summary.amtCollected);
    }
}
