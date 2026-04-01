package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.util.TransactionSummary;
import com.intuit.sbd.payroll.psp.api.dtos.RedebitImpoundDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: Jul 18, 2008
 * Time: 3:55:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateRedebitImpoundTransactionCore extends Process implements IProcess {

    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private RedebitImpoundDTO redebitImpoundDTO;
    private Company mCompany;
    private FinancialTransaction originalFinancialTxn;
    private PayrollRun mPayrollRun;
    private CompanyBankAccount mCompanyBankAccount;
    private TransactionResponse mTrasactionResponse;
    private FinancialTransaction mRedebitTransaction;
    private FinancialTransaction existingRedebitTransaction;
    private TransactionSummary txnSummary;
    private CancelTransactionCore cancelTransactionCoreProcess;

    public UpdateRedebitImpoundTransactionCore(SourceSystemCode pSourceSystemCode,
                          String pSourceCompanyId,
                          RedebitImpoundDTO pRedebitImpoundDTO) {
        this.mSourceCompanyId = pSourceCompanyId;
        this.mSourceSystemCd = pSourceSystemCode;
        this.redebitImpoundDTO = pRedebitImpoundDTO;
    }

    public FinancialTransaction getFinancialTransaction() {
        return mRedebitTransaction;
    }

    public ProcessResult validate() {

        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        if (redebitImpoundDTO == null) {
            validationResult.getMessages().InvalidValue(EntityName.DDTransaction, null,"RedebitImpoundDTO");
            return validationResult;
        }
        // validate the DTO
        validationResult.merge(redebitImpoundDTO.validate());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company Exists
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (mCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Check if company bank account is Active
        mCompanyBankAccount = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);

        if (mCompanyBankAccount == null) {
            validationResult.getMessages().CompanyDoesNotHaveActiveBankAccount(EntityName.CompanyBankAccount,
                    mSourceCompanyId, mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Check original financial transaction exists

        existingRedebitTransaction = PayrollServices.entityFinder.findById(FinancialTransaction.class,
                SpcfUniqueId.createInstance(redebitImpoundDTO.getOriginalFinancialTxId()));

        if (existingRedebitTransaction == null) {
            validationResult.getMessages().FinancialTransactionDoesNotExist(EntityName.FinancialTransaction,
                    redebitImpoundDTO.getOriginalFinancialTxId(),
                    redebitImpoundDTO.getOriginalFinancialTxId(), mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        // Check whether this is a valid action
        mPayrollRun = existingRedebitTransaction.getPayrollRun();
        ActionEvent actionEvent = PayrollServices.entityFinder.findById(ActionEvent.class,  ActionEventCode.DDRedebitEdit);

        if (!TransactionType.isValidTypeToUpdateRedebit(existingRedebitTransaction.getTransactionType().getTransactionTypeCd())) {
            validationResult.getMessages().ActionNotValidForFinancialTransaction(EntityName.FinancialTransaction,
                    existingRedebitTransaction.getId().toString(),
                    actionEvent.getCode().toString(),
                    existingRedebitTransaction.getId().toString(),
                    existingRedebitTransaction.getTransactionType().getTransactionTypeCd().toString(),
                    existingRedebitTransaction.calculateCurrentTransactionState().getTransactionStateCd().toString());
                return validationResult;
        }

        originalFinancialTxn = existingRedebitTransaction.getOriginalTransaction();
        txnSummary = originalFinancialTxn.summarizeRelatedTransactions();
        SpcfDecimal amtUncoleected = txnSummary.amtUncollected;

        String offeringServiceChargeType = null;
        if (TransactionType.isFeeTransactionType(originalFinancialTxn.getTransactionType().getTransactionTypeCd())) {
            offeringServiceChargeType =
                    OfferingServiceCharge.findOfferingServiceChargeTypeBySKU(originalFinancialTxn.getSku()).toString();
        }
        if (amtUncoleected.compareTo(redebitImpoundDTO.getAmount()) < 0) {
            validationResult.getMessages().RedebitAmountExceedsUncollectedAmount(EntityName.FinancialTransaction,
                    existingRedebitTransaction.getId().toString(), redebitImpoundDTO.getAmount().toString(),
                    existingRedebitTransaction.getTransactionType().getTransactionTypeCd().toString(),
                    offeringServiceChargeType);
             return validationResult;
        }

        // verify the Initiation date is a future banking day
        SpcfCalendar initiationDate = DateDTO.convertToSpcfCalendar(redebitImpoundDTO.getInitiationDate());
        SpcfCalendar today = PSPDate.getPSPTime();
        CalendarUtils.clearTime(today);
        boolean isInitiationDateInPast = initiationDate.before(today);
        if ( isInitiationDateInPast || CalendarUtils.isWeekendOrHoliday(initiationDate)) {
            validationResult.getMessages().SettlementDateNotFutureBankingDay(EntityName.Date, null,
                    DateDTO.convertToSpcfCalendar(redebitImpoundDTO.getInitiationDate()).toString(), SettlementType.ACH.toString());
            return validationResult;
        }

        // Perform the validation to cancel the existing Redebit
        cancelTransactionCoreProcess = new CancelTransactionCore(mSourceSystemCd, mSourceCompanyId, existingRedebitTransaction.getId().toString(), true);
        validationResult.merge(cancelTransactionCoreProcess.validate());
        
        return validationResult;

    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        //resolve MMT?  Only if this amount + the pending amount from THIS transaction type PLUS all the other transactions associated with the MMT
        // is equal to the pending collection amount
        MoneyMovementTransaction mmt = originalFinancialTxn.getMoneyMovementTransaction();
        boolean bNewAmountResolvesMMT = mmt.amountResolvesMMT(new SpcfMoney(redebitImpoundDTO.getAmount().subtract(existingRedebitTransaction.getFinancialTransactionAmount())));

        //If the amount resolves the MMT, resolve the original MMT txn return and the txn returns for all the redebits related to the txns related to the original MMT
        //If the amount DOES NOT cover the MMT, unresolve the return
        if (bNewAmountResolvesMMT) {
            mmt.resolveMMTAndRelatedMMTs();
        } else {
            mmt.unresolveMMTAndRelatedTransactionReturns();
        }

        SpcfMoney oldRedebitAmount = existingRedebitTransaction.getFinancialTransactionAmount();
        SpcfCalendar oldRedebitDate = existingRedebitTransaction.getSettlementDate().toLocal();
        
        //Call the process method for the CancelTransactionCore process we created in validate
        processResult.merge(cancelTransactionCoreProcess.process());

        SpcfCalendar settlementDate = DateDTO.convertToSpcfCalendar(redebitImpoundDTO.getInitiationDate()).copy();
        boolean beforeCutoff = mCompany.getOffloadGroup().isBeforeActualCutoffTime();
        if (beforeCutoff) {
            CalendarUtils.addBusinessDays(settlementDate, 1);
        } else {
            CalendarUtils.addBusinessDays(settlementDate, 2);
        }
        if (redebitImpoundDTO.getAmount().compareTo(SpcfDecimal.createInstance("0.00")) > 0) {
            // Add the transaction to the database
            FinancialTransaction financialTx =
                    FinancialTransaction.createFinancialTransaction(existingRedebitTransaction.getCompany(),
                            existingRedebitTransaction.getPayrollRun(),
                            null,
                            existingRedebitTransaction.getCreditBankAccount(),
                            mCompanyBankAccount.getBankAccount(),
                            BankAccountOwnerType.Intuit,
                            BankAccountOwnerType.Company,
                            existingRedebitTransaction.getTransactionType().getTransactionTypeCd(),
                            redebitImpoundDTO.getAmount(),
                            SettlementType.ACH,
                            settlementDate,
                            existingRedebitTransaction.getSku(),
                            originalFinancialTxn,
                            existingRedebitTransaction.getSkuQuantity());

            mRedebitTransaction = financialTx;

            // Create a transaction response for the ER DD REDEBIT
            Collection<FinancialTransaction> transactions = new DomainEntitySet<FinancialTransaction>();
            transactions.add(financialTx);
            mTrasactionResponse = TransactionResponse.createTransactionResponse(mCompany, transactions, null);
        }

        // Create events
        if (oldRedebitAmount.compareTo(redebitImpoundDTO.getAmount()) != 0) {
            CompanyEvent.createRedebitAmountUpdatedEvent(mCompany, mPayrollRun.getId().toString(), oldRedebitAmount.toString(),
                    redebitImpoundDTO.getAmount().toString());
        }

        if (oldRedebitDate.compareTo(settlementDate) != 0) {
            CompanyEvent.createRedebitDateUpdatedEvent(mCompany, mPayrollRun.getId().toString(), oldRedebitDate.toString(),
                    settlementDate.toString());
        }

        return processResult;
    }

}
