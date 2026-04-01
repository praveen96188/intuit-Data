package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.accountservice.UpdateAccountServiceBankAccount;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import org.apache.commons.lang.StringUtils;

import java.util.Objects;

/**
 *
 * User: mvillani
 * Date: Aug 13, 2007
 * Time: 4:50:47 PM

 */
public final class UpdateCompanyBankAccountCore extends Process implements IProcess {

    /**
     * Core process for updating company bank account.
     *
     * @author Marcela Villani
     */

    private CompanyBankAccountDTO mCompanyBankAccountDTO;
    private CompanyBankAccount mCompanyBankAccount = null;
    private CompanyBankAccount mFoundCompanyBankAccount = null;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private boolean mUpdateAccountService;
    private UpdateAccountServiceBankAccount updateAccountServiceBankAccount = null;


    public CompanyBankAccount getCompanyBankAccount() {
        return mCompanyBankAccount;
    }

    public UpdateCompanyBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                        CompanyBankAccountDTO pCompanyBankAccountDTO) {
        this(pSourceSystemCd,pSourceCompanyId,pCompanyBankAccountDTO,false);
    }

    public UpdateCompanyBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                        CompanyBankAccountDTO pCompanyBankAccountDTO, boolean pUpdateAccountService) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mCompanyBankAccountDTO = pCompanyBankAccountDTO;
        this.mUpdateAccountService = pUpdateAccountService;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        if (mFoundCompanyBankAccount != null) {
            mFoundCompanyBankAccount.getBankAccount().setBankName(mCompanyBankAccountDTO.getBankAccountDTO().getBankName());
            mFoundCompanyBankAccount.setSourceBankAccountName(mCompanyBankAccountDTO.getSourceBankAccountName());

            mFoundCompanyBankAccount = Application.save(mFoundCompanyBankAccount);
            mCompanyBankAccount = mFoundCompanyBankAccount;
        }

        if (Objects.nonNull(updateAccountServiceBankAccount)){
            processResult.merge(updateAccountServiceBankAccount.process());
        }

        processResult.setResult(mCompanyBankAccount);
        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Validate Company Bank Account DTO
        ProcessResult validateCompanyBankAccountResult = mCompanyBankAccountDTO.validateCompanyBankAccount();
        validationResult.merge(validateCompanyBankAccountResult);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Bank Account DTO
        if (mCompanyBankAccountDTO.getBankAccountDTO() == null) {
            validationResult.getMessages().BankAccountNotSpecified(EntityName.EmployeeBankAccount,
                    mCompanyBankAccountDTO.getCompanyBankAccountID());
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company exists

        Company foundCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (foundCompany == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.CompanyBankAccount, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
        } else {
            if (!foundCompany.isAllowedCapability(SystemCapabilityCode.ChangeEmployerBankAccount)) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        foundCompany.getSourceSystemCd().toString(),
                        foundCompany.getSourceCompanyId(), SystemCapabilityCode.ChangeEmployerBankAccount.toString());
            }

            // Check if the company bank account is active
            String sourceBankAccountId = mCompanyBankAccountDTO.getCompanyBankAccountID();
            mFoundCompanyBankAccount = CompanyBankAccount.findCompanyBankAccount(foundCompany, sourceBankAccountId);
            if (mFoundCompanyBankAccount == null) {
                // Verify that company bank account has not been deactivated at some time in the past
                if (foundCompany.deactivatedCBAExistsForSourceBankAccountId(sourceBankAccountId)) {
                    validationResult.getMessages().CompanyBankAccountNotActive(EntityName.CompanyBankAccount,
                            sourceBankAccountId, sourceBankAccountId, mSourceSystemCd.toString(), mSourceCompanyId);
                } else {
                    validationResult.getMessages().CompanyBankAccountDoesNotExist(EntityName.CompanyBankAccount,
                            sourceBankAccountId, sourceBankAccountId, mSourceSystemCd.toString(), mSourceCompanyId);
                }
            }
            //  Verify the status is Active or PendingVerification
            // If this is the case, only the bank name and/or source bank account name can be modified
            // Otherwise no updates are allowed
            else if ((mFoundCompanyBankAccount.getStatusCd() == BankAccountStatus.Active) || (mFoundCompanyBankAccount.getStatusCd() == BankAccountStatus.PendingVerification)) {
                if (!mFoundCompanyBankAccount.getBankAccount().equalsIgnoreBankNameSourceBankName(getBankAccountFromDTO())) {
                    validationResult.getMessages().CompanyBankAccountUpdateFailed(EntityName.CompanyBankAccount, sourceBankAccountId);
                }
            }
            if (StringUtils.isNotEmpty(foundCompany.getIAMRealmId())
                    && foundCompany.isMoneyMovementOnboardingEnabled()
                    && mUpdateAccountService) {
                updateAccountServiceBankAccount = new UpdateAccountServiceBankAccount(foundCompany, mCompanyBankAccountDTO);
                validationResult.merge(updateAccountServiceBankAccount.validate());
            }
        }
        return validationResult;
    }

    private BankAccount getBankAccountFromDTO() {
        BankAccount bankAccount = new BankAccount();

        bankAccount.setAccountNumber(mCompanyBankAccountDTO.getBankAccountDTO().getAccountNumber());
        BankAccountType domainBAType = BankAccountType
                .valueOf(mCompanyBankAccountDTO.getBankAccountDTO().getAccountType().toString());
        bankAccount.setAccountTypeCd(domainBAType);
        bankAccount.setACHAccountTypeCd(ACHBankAccountType.valueOf(domainBAType.toString()));
        bankAccount.setEffectiveDate(PSPDate.getPSPTime());
        bankAccount.setBankName(mCompanyBankAccountDTO.getBankAccountDTO().getBankName());
        bankAccount.setRoutingNumber(mCompanyBankAccountDTO.getBankAccountDTO().getRoutingNumber());

        return bankAccount;
    }
}

