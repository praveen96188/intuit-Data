package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.DDProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 4/11/12
 * Time: 10:13 AM
 */
public class AddCompanyTaxPenaltyAndInterestRefunds extends Process {

    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private SpcfMoney mPenaltiesRefundAmount;
    private SpcfMoney mInterestRefundAmount;
    private String mNoteText;
    private SettlementTypeDTO mSettlementType;

    private Company mCompany;
    private BankAccount mBankAccount = null;

    public AddCompanyTaxPenaltyAndInterestRefunds(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                                  SpcfMoney pPenaltiesRefundAmount, SpcfMoney pInterestRefundAmount,
                                                  String pNoteText, SettlementTypeDTO pSettlementType) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mPenaltiesRefundAmount = pPenaltiesRefundAmount;
        mInterestRefundAmount = pInterestRefundAmount;
        mNoteText = pNoteText;
        mSettlementType = pSettlementType;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        CompanyBankAccount companyBankAccount;

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCode, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        //Check if company exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCode);
        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCode.toString(), mSourceCompanyId);
            return validationResult;
        }

        if (mPenaltiesRefundAmount == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.FinancialTransaction, mSourceCompanyId, "PenaltiesRefundAmount");
            return validationResult;
        }

        if (mInterestRefundAmount == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.FinancialTransaction, mSourceCompanyId, "InterestRefundAmount");
            return validationResult;
        }

        if (StringUtils.isEmpty(mNoteText)) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "NoteText");
            return validationResult;
        }

        if (mPenaltiesRefundAmount.isLessThan(SpcfMoney.ZERO)) {
            validationResult.getMessages().AmountNotPositive(EntityName.FinancialTransaction, mPenaltiesRefundAmount.toString());
            return validationResult;
        }

        if (mInterestRefundAmount.isLessThan(SpcfMoney.ZERO)) {
            validationResult.getMessages().AmountNotPositive(EntityName.FinancialTransaction, mInterestRefundAmount.toString());
            return validationResult;
        }

        if (mPenaltiesRefundAmount.isZero() && mInterestRefundAmount.isZero()) {
            validationResult.getMessages().TotalRefundAmountNotPositive(EntityName.MoneyMovementTransaction, mPenaltiesRefundAmount.toString());
        }

        if (mSettlementType == null || !(mSettlementType == SettlementTypeDTO.ACH || mSettlementType == SettlementTypeDTO.CheckType || mSettlementType == SettlementTypeDTO.Wire)) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "SettlementType");
            return validationResult;
        }

        if (mSettlementType == SettlementTypeDTO.ACH) {
            //Find active company bank account
            companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

            if (companyBankAccount == null) {
                validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                        mSourceCompanyId, mSourceSystemCode.toString(), mSourceCompanyId);
                return validationResult;
            } else {
                mBankAccount = companyBankAccount.getBankAccount();
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult<DomainEntitySet<FinancialTransaction>> process() {
        ProcessResult<DomainEntitySet<FinancialTransaction>> processResult = new ProcessResult<DomainEntitySet<FinancialTransaction>>();
        IntuitBankAccount debitBankAccount = IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_FEE);
        BankAccountOwnerType creditBankAccountOwnerType = BankAccountOwnerType.Company;

        if (mSettlementType != SettlementTypeDTO.ACH) {
            creditBankAccountOwnerType = null;
        }

        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        com.intuit.sbd.payroll.psp.domain.SettlementType settlementType = DDProcessesToDTO.getDomainSettlementType(mSettlementType);

        // create the FT - Penalties if mPenaltiesRefundAmount is > zero
        if (mPenaltiesRefundAmount.isGreaterThan(SpcfMoney.ZERO)) {
            FinancialTransaction penaltiesFinancialTransaction = FinancialTransaction.createFinancialTransaction(mCompany, null, null, mBankAccount, debitBankAccount.getBankAccount(),
                    creditBankAccountOwnerType, BankAccountOwnerType.Intuit,
                    TransactionTypeCode.EmployerPenaltiesRefundCredit, mPenaltiesRefundAmount, settlementType,
                    FinancialTransaction.getSettlementDate(mCompany.getOffloadGroup()));  //Defaulting it to next ACH offload date for all settlement types
            financialTransactions.add(penaltiesFinancialTransaction);
        }

        // create the FT - Interest if mInterestRefundAmount is > zero
        if (mInterestRefundAmount.isGreaterThan(SpcfMoney.ZERO)) {
            FinancialTransaction interestFinancialTransaction = FinancialTransaction.createFinancialTransaction(mCompany, null, null, mBankAccount, debitBankAccount.getBankAccount(),
                    creditBankAccountOwnerType, BankAccountOwnerType.Intuit,
                    TransactionTypeCode.EmployerInterestRefundCredit, mInterestRefundAmount, settlementType,
                    FinancialTransaction.getSettlementDate(mCompany.getOffloadGroup())); //Defaulting it to next ACH offload date for all settlement types
            financialTransactions.add(interestFinancialTransaction);
        }

        if (mSettlementType != SettlementTypeDTO.ACH) {
            for (FinancialTransaction transaction : financialTransactions) {
                // Update transaction to executed, then completed
                transaction.updateFinancialTransactionState(TransactionStateCode.Executed);
                transaction.updateFinancialTransactionState(TransactionStateCode.Completed);
            }
        }

        processResult.setResult(financialTransactions);

        SpcfMoney totalAmount = (SpcfMoney) mPenaltiesRefundAmount.add(mInterestRefundAmount);

        //Create Company Event -
        CompanyEvent.createERPenaltiesAndInterestRefundCreatedEvent(mCompany, financialTransactions, mPenaltiesRefundAmount, mInterestRefundAmount, totalAmount, mNoteText);

        return processResult;
    }
}
