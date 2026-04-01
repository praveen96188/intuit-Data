package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.common.DDProcessesToDTO;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.apache.commons.lang.StringUtils;

/**
 * User: ihannur
 * Date: 7/19/12
 * Time: 1:54 PM
 */
public class AddCourtesyFeeRefund extends Process {

    private SourceSystemCode mSourceSystemCode;
    private String mSourceCompanyId;
    private SpcfMoney mRefundAmount;
    private String mNoteText;
    private SettlementTypeDTO mSettlementType;

    private Company mCompany;
    private BankAccount mBankAccount = null;

    public AddCourtesyFeeRefund(SourceSystemCode pSourceSystemCode, String pSourceCompanyId,
                                SpcfMoney pRefundAmount, String pNoteText, SettlementTypeDTO pSettlementType) {
        mSourceSystemCode = pSourceSystemCode;
        mSourceCompanyId = pSourceCompanyId;
        mRefundAmount = pRefundAmount;
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

        if (mRefundAmount == null) {
            validationResult.getMessages().RequiredInputMissingOrBlank(EntityName.FinancialTransaction, mSourceCompanyId, "RefundAmount");
            return validationResult;
        }

        if (StringUtils.isEmpty(mNoteText)) {
            validationResult.getMessages().InvalidValue(EntityName.FinancialTransaction, null, "NoteText");
            return validationResult;
        }

        if (mRefundAmount.isLessThanEqualTo(SpcfMoney.ZERO)) {
            validationResult.getMessages().AmountNotPositive(EntityName.FinancialTransaction, mRefundAmount.toString());
            return validationResult;
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
    public ProcessResult<FinancialTransaction> process() {
        ProcessResult<FinancialTransaction> processResult = new ProcessResult<FinancialTransaction>();
        IntuitBankAccount debitBankAccount = IntuitBankAccount.findIntuitBankAccountByName(IntuitBankAccount.Name.INTUIT_FEE);
        BankAccountOwnerType creditBankAccountOwnerType = BankAccountOwnerType.Company;

        if (mSettlementType != SettlementTypeDTO.ACH) {
            creditBankAccountOwnerType = null;
        }

        com.intuit.sbd.payroll.psp.domain.SettlementType settlementType = DDProcessesToDTO.getDomainSettlementType(mSettlementType);

        FinancialTransaction financialTransaction = FinancialTransaction.createFinancialTransaction(mCompany, null, null, mBankAccount, debitBankAccount.getBankAccount(),
                creditBankAccountOwnerType, BankAccountOwnerType.Intuit,
                TransactionTypeCode.ERCourtesyRefundCredit, mRefundAmount, settlementType,
                FinancialTransaction.getSettlementDate(mCompany.getOffloadGroup()));  //Defaulting it to next ACH offload date for all settlement types

        // Try to determine a SKU
        OfferingServiceCharge osc = OfferingServiceChargeGroup.findFirstOfferingServiceCharge(mCompany, OfferingServiceChargeType.CourtesyRefund);
        if (osc != null) {
            financialTransaction.setSku(osc.getSKU());
            financialTransaction.setSkuQuantity(1);
        }

        if (mSettlementType != SettlementTypeDTO.ACH) {
            // Update transaction to executed, then completed
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Executed);
            financialTransaction.updateFinancialTransactionState(TransactionStateCode.Completed);
        }

        processResult.setResult(financialTransaction);

        //Create Company Event for sending email for Courtesy Refund
        CompanyEvent.createFeeRefundedEventForCourtesyRefund(financialTransaction, mNoteText);

        return processResult;
    }
}
