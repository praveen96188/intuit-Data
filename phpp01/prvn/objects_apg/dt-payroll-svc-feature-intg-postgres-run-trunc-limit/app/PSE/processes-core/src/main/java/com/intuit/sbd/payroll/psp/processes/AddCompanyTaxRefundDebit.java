package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.DDProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.apache.commons.lang.StringUtils;

/**
 * User: ihannur
 * Date: 8/3/12
 * Time: 3:10 PM
 */
public class AddCompanyTaxRefundDebit extends Process {

    private String mFinancialTransactionId;
    private String mNoteText;
    private SettlementTypeDTO mSettlementType;
    private FinancialTransaction mFinancialTransaction;
    private Company mCompany;
    private BankAccount mBankAccount = null;

    public AddCompanyTaxRefundDebit(String pId, String pNoteText, SettlementTypeDTO pSettlementType) {
        mFinancialTransactionId = pId;
        mNoteText = pNoteText;
        mSettlementType = pSettlementType;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        CompanyBankAccount companyBankAccount;

        if (mFinancialTransactionId == null || mFinancialTransactionId.length() == 0) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.FinancialTransaction, "Financial Transaction Id", "Financial Transaction ID");
            return validationResult;
        } else {
            mFinancialTransaction = Application.findById(FinancialTransaction.class, SpcfUniqueId.createInstance(mFinancialTransactionId));
            if (mFinancialTransaction == null) {
                validationResult.getMessages().NoEntityWithGivenId("FinancialTransaction", mFinancialTransactionId);
                return validationResult;
            }
            mCompany = mFinancialTransaction.getCompany();
        }

        if (StringUtils.isEmpty(mNoteText)) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "NoteText");
            return validationResult;
        }

        if (mSettlementType == null || !(mSettlementType == SettlementTypeDTO.ACH || mSettlementType == SettlementTypeDTO.CheckType || mSettlementType == SettlementTypeDTO.Wire)) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "SettlementType");
            return validationResult;
        }

        if (mSettlementType == SettlementTypeDTO.ACH) {
            //Find active company bank account
            companyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mFinancialTransaction.getCompany());

            if (companyBankAccount == null) {
                validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                                               mCompany.getSourceCompanyId(), mCompany.getSourceSystemCd().toString(), mCompany.getSourceCompanyId());
                return validationResult;
            } else {
                mBankAccount = companyBankAccount.getBankAccount();
            }
        }

        return validationResult;
    }

    @Override
    public ProcessResult<FinancialTransaction> process() {
        ProcessResult<FinancialTransaction> processResult = new ProcessResult<FinancialTransaction>();
        IntuitBankAccount creditBankAccount = IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_FEE);
        BankAccountOwnerType creditBankAccountOwnerType = BankAccountOwnerType.Company;

        if (mSettlementType != SettlementTypeDTO.ACH) {
            creditBankAccountOwnerType = null;
        }

        com.intuit.sbd.payroll.psp.domain.SettlementType settlementType = DDProcessesToDTO.getDomainSettlementType(mSettlementType);

        TransactionTypeCode transactionTypeCode;
        switch (mFinancialTransaction.getTransactionType().getTransactionTypeCd()) {
            case EmployerPenaltiesRefundCredit:
                transactionTypeCode = TransactionTypeCode.EmployerPenaltiesRefundDebit;
                break;
            case EmployerInterestRefundCredit:
                transactionTypeCode = TransactionTypeCode.EmployerInterestRefundDebit;
                break;
            default:
                return processResult;
        }

        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(mCompany, null, null, creditBankAccount.getBankAccount(), mBankAccount,
                                                                         BankAccountOwnerType.Intuit, creditBankAccountOwnerType,
                                                                         transactionTypeCode, mFinancialTransaction.getFinancialTransactionAmount(), settlementType,
                                                                         FinancialTransaction.getSettlementDate(mCompany.getOffloadGroup()));  //Defaulting it to next ACH offload date for all settlement types

        financialTransaction.setOriginalTransaction(mFinancialTransaction);
        mFinancialTransaction.addAssociatedTransactions(financialTransaction);

        if (mSettlementType != SettlementTypeDTO.ACH) {
                // Update transaction to executed, then completed
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Completed);

        }

        processResult.setResult(financialTransaction);

        //Create Company Event -
        CompanyEvent.createERPenaltiesAndInterestRefundDebitCreatedEvent(mCompany, financialTransaction, mNoteText);

        return processResult;
    }
}
