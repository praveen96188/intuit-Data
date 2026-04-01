package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.managers.util.Validator;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.accountservice.UpdateAccountServiceBankAccount;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 * Created by IntelliJ IDEA.
 * User: rsakhamuri
 * Date: May 12, 2008
 * Time: 2:26:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChangeCompanyBankAccountCore  extends Process implements IProcess {

    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private CompanyBankAccountDTO companyBankAccountDTO;
    private CompanyBankAccount cbaNew;
    private CompanyBankAccount cbaOld;
    private boolean shouldAddRandomDebits;
    private boolean shouldAllowPendingTransactions;
    private boolean shouldMovePendingTransactionsToAccount;
    private boolean mUpdateAccountService;
    private boolean isPSPRandomDollarVerificationRequired;


    private AddCompanyBankAccountCore addCompanyBankAccountProcess = null;
    private DeactivateCompanyBankAccountCore deactivateCompanyBankAccountProcess = null;
    private UpdateAccountServiceBankAccount updateAccountServiceBankAccount = null;
    private MovePendingTransactionToBankAccount moveTransactionsToBAProcess = null;

    
    public ChangeCompanyBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                        CompanyBankAccountDTO pCompanyBankAccountDTO, boolean pShouldAddRandomDebits,
                                        boolean pShouldAllowPendingTransactions,
                                        boolean pShouldMovePendingTransactionsToAccount) {

        this(pSourceSystemCd,
                pSourceCompanyId,
                pCompanyBankAccountDTO,
                pShouldAddRandomDebits,
                pShouldAllowPendingTransactions,
                pShouldMovePendingTransactionsToAccount,
                false,true);
    }

    public ChangeCompanyBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                        CompanyBankAccountDTO pCompanyBankAccountDTO, boolean pShouldAddRandomDebits,
                                        boolean pShouldAllowPendingTransactions,
                                        boolean pShouldMovePendingTransactionsToAccount,
                                        boolean pAccountServiceUpdate,boolean isPSPRandomDollarVerificationRequired) {
        sourceSystemCd = pSourceSystemCd;
        sourceCompanyId = pSourceCompanyId;
        companyBankAccountDTO = pCompanyBankAccountDTO;
        shouldAddRandomDebits = pShouldAddRandomDebits;
        shouldAllowPendingTransactions = pShouldAllowPendingTransactions ;
        shouldMovePendingTransactionsToAccount = pShouldMovePendingTransactionsToAccount;
        this.mUpdateAccountService = pAccountServiceUpdate;
        this.isPSPRandomDollarVerificationRequired = isPSPRandomDollarVerificationRequired;

    }

    public CompanyBankAccount getCompanyBankAccount() {
        return cbaNew;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Check if Company parameters are valid
        validationResult.merge(Validator.validCompanyParameters(sourceSystemCd, sourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists
        Company foundCompany = Company.findCompany(sourceCompanyId, sourceSystemCd);
        if (foundCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, sourceCompanyId,
                    sourceSystemCd.toString(), sourceCompanyId);
        }
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // make sure this capability is allowed
        if (! foundCompany.isAllowedCapability(SystemCapabilityCode.ChangeEmployerBankAccount)) {
            validationResult.getMessages().CompanyOperationNotAllowed(foundCompany.getSourceSystemCd().toString(),
                    foundCompany.getSourceCompanyId(), SystemCapabilityCode.ChangeEmployerBankAccount.toString());
            return validationResult;
        }

        // make sure the DTO is present and identifies the CBA
        if (companyBankAccountDTO == null || companyBankAccountDTO.getCompanyBankAccountID() == null) {
            validationResult.getMessages().CompanyBankAccountNotSpecified(EntityName.CompanyBankAccount, sourceCompanyId);
            return validationResult;

        }

        // make sure the CBA exists
        cbaOld = CompanyBankAccount.findCompanyBankAccount(foundCompany,
                                        companyBankAccountDTO.getCompanyBankAccountID());
        if (cbaOld == null) {
            if (foundCompany.deactivatedCBAExistsForSourceBankAccountId(
                    companyBankAccountDTO.getCompanyBankAccountID())) {
               validationResult.getMessages().CompanyBankAccountNotActive(EntityName.CompanyBankAccount,
                       companyBankAccountDTO.getCompanyBankAccountID(), companyBankAccountDTO.getCompanyBankAccountID(),
                       sourceSystemCd.toString(), sourceCompanyId);
            } else {
                validationResult.getMessages().CompanyBankAccountDoesNotExist(EntityName.CompanyBankAccount,
                        companyBankAccountDTO.getCompanyBankAccountID(), companyBankAccountDTO.getCompanyBankAccountID(),
                        sourceSystemCd.toString(), sourceCompanyId);
            }

            return validationResult;
        }

        // MAYBE make sure there are no CREATED transactions - excluding EmployerVerificationDebits
        if (! shouldAllowPendingTransactions) {
            DomainEntitySet<FinancialTransaction> createdFTs =
                    FinancialTransaction.findFinancialTransactionsExcludedType(
                            sourceSystemCd, sourceCompanyId, cbaOld.getBankAccount(),
                            TransactionTypeCode.EmployerVerificationDebit, TransactionStateCode.Created);
            if (createdFTs.size() > 0) {
                validationResult.getMessages().CompanyBankAccountHasPendingFinancialTransactions(
                        EntityName.CompanyBankAccount, cbaOld.getSourceBankAccountId(),
                        cbaOld.getSourceBankAccountId(), sourceSystemCd.toString(), sourceCompanyId);
                return validationResult;
            }
        }

        // make sure the new CBA passes "add" validation
        addCompanyBankAccountProcess = new AddCompanyBankAccountCore(sourceSystemCd, sourceCompanyId,
                                                                companyBankAccountDTO, shouldAddRandomDebits, false,isPSPRandomDollarVerificationRequired);
        validationResult.merge(addCompanyBankAccountProcess.validate());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // MAYBE perform "move" validations
        if (shouldMovePendingTransactionsToAccount) {
            moveTransactionsToBAProcess = new MovePendingTransactionToBankAccount(sourceSystemCd, sourceCompanyId,
                                                            companyBankAccountDTO.getCompanyBankAccountID(), true);
            validationResult.merge(moveTransactionsToBAProcess.validate());

            if (!validationResult.isSuccess()) {
                return validationResult;
            }

            if (shouldAddRandomDebits) {
                validationResult.getMessages().DestinationBankAccountNotActive(EntityName.CompanyBankAccount,
                                                        companyBankAccountDTO.getCompanyBankAccountID());
                return validationResult;
            }

        }

        if(StringUtils.isNotEmpty(foundCompany.getIAMRealmId())
                && foundCompany.isMoneyMovementOnboardingEnabled()
                && mUpdateAccountService){
            updateAccountServiceBankAccount = new UpdateAccountServiceBankAccount(foundCompany,companyBankAccountDTO);
            validationResult.merge(updateAccountServiceBankAccount.validate());
        }

        return validationResult;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        // deactivate the source bank account
        cbaOld = cbaOld.deactivate();

        // add the destination bank account
        addCompanyBankAccountProcess.process();
        cbaNew = addCompanyBankAccountProcess.getCompanyBankAccount();
        if (shouldMovePendingTransactionsToAccount) {
            moveTransactionsToBAProcess.process();    
        }

        if (Objects.nonNull(updateAccountServiceBankAccount)){
            processResult.merge(updateAccountServiceBankAccount.process());
        }

        // Raising CBA change event at addCompanyBankAccountProcess

        return processResult;
    }
}
