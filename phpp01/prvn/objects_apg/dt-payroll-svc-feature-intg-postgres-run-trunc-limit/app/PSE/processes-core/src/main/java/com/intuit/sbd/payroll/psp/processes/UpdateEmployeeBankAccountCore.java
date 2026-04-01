package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.EmployeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.wallet.WalletCreateCore;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Process core used to update any bank accounts
 */
public class UpdateEmployeeBankAccountCore extends Process implements IProcess {

    /**
     * Core process for adding a new employee bank account.
     *
     * @author Marcela Villani
     */
	private static final Logger logger = LoggerFactory.getLogger(UpdateEmployeeBankAccountCore.class);

    private EmployeeBankAccount mEmployeeBankAccount;
    private EmployeeBankAccountDTO mEmployeeBankAccountDTO;
    private EmployeeBankAccount foundEmployeeBankAccount = null;
    private Employee mEmployee;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mEmployeeId;

    public EmployeeBankAccount getEmployeeBankAccount() {
        return mEmployeeBankAccount;
    }

    public UpdateEmployeeBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                         String pEmployeeId, EmployeeBankAccountDTO pEmployeeBankAccountDTO) {
        mEmployeeBankAccountDTO = pEmployeeBankAccountDTO;
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mEmployeeId = pEmployeeId;
    }


    public UpdateEmployeeBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                         Employee pEmployee, EmployeeBankAccountDTO pEmployeeBankAccountDTO) {
        mEmployeeBankAccountDTO = pEmployeeBankAccountDTO;
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mEmployee = pEmployee;
    }

    /**
     * Update the employee bank account and save to the database
     *
     * @return Collection of process messages, or an empty collection if all validations
     * pass
     */
    public ProcessResult<EmployeeBankAccount> process() {
        ProcessResult<EmployeeBankAccount> processResult = new ProcessResult<EmployeeBankAccount>();

        BankAccount bankAccount = getBankAccountFromDTO();

        // Check if something other than the bank name has changed. If yes, inactivate and expire the existing one
        // and create a new one. Otherwise, update the existing one
        if (bankAccount.equalsIgnoreBankNameSourceBankName(foundEmployeeBankAccount.getBankAccount())) {
            foundEmployeeBankAccount.getBankAccount().setBankName(mEmployeeBankAccountDTO.getBankAccount().getBankName());
            foundEmployeeBankAccount.setAccountOrder(mEmployeeBankAccountDTO.getOrder());

            //Create and Set WalletId for existing Employee Bank Account with NoWalletId
            setWalletId(foundEmployeeBankAccount, FeatureFlags.Key.ENABLE_WALLET_CREATION_EXISTING_BA);

            // this information does not exist on paycheck updates, do not updated it
            if(!mEmployeeBankAccountDTO.isPaycheckUpdate()) {
                foundEmployeeBankAccount.setAmount(mEmployeeBankAccountDTO.getAmount());
                foundEmployeeBankAccount.setAmountType(mEmployeeBankAccountDTO.getAmountType());
            }

            if (mEmployeeBankAccountDTO.getSessionId() != null) {
                foundEmployeeBankAccount.setSessionId(mEmployeeBankAccountDTO.getSessionId());
            }

            mEmployeeBankAccount = Application.save(foundEmployeeBankAccount);
        } else {
            foundEmployeeBankAccount.expireEmployeeBankAccount();

            foundEmployeeBankAccount = Application.save(foundEmployeeBankAccount);
            bankAccount = Application.save(bankAccount);

            // generate a new source id
            if(mEmployeeBankAccountDTO.isGenerateNewSourceId()) {
                mEmployeeBankAccountDTO.resetBankAccountId();
            }

            // Create new Employee Bank Account
            mEmployeeBankAccount = new EmployeeBankAccount();

            // Set Status =  Active
            mEmployeeBankAccount.setStatusCd(BankAccountStatus.Active);

            // Set status effective date to current date
            mEmployeeBankAccount.setStatusEffectiveDate(PSPDate.getPSPTime());

            // Set effective date to current date
            mEmployeeBankAccount.setEffectiveDate(PSPDate.getPSPTime());

            // Set Expiration Date to null
            mEmployeeBankAccount.setExpirationDate(null);

            // Set Bank Account
            mEmployeeBankAccount.setBankAccount(bankAccount);

            // Set Source Bank Account ID
            mEmployeeBankAccount.setSourceBankAccountId(mEmployeeBankAccountDTO.getEmployeeBankAccountId());

            mEmployeeBankAccount.setAmount(mEmployeeBankAccountDTO.getAmount());
            mEmployeeBankAccount.setAmountType(mEmployeeBankAccountDTO.getAmountType());
            mEmployeeBankAccount.setAccountOrder(mEmployeeBankAccountDTO.getOrder());

            // Associate Employee and Employee Bank Account
            mEmployeeBankAccount.setEmployee(mEmployee);

            //Create and Set WalletId for Employee Bank Account
            setWalletId(mEmployeeBankAccount, FeatureFlags.Key.ENABLE_WALLET_CREATION);

            if (mEmployeeBankAccountDTO.getSessionId() != null) {
                mEmployeeBankAccount.setSessionId(mEmployeeBankAccountDTO.getSessionId());
            }

            mEmployeeBankAccount = Application.save(mEmployeeBankAccount);

            mEmployee.addEmployeeBankAccount(mEmployeeBankAccount);

            // create a EmployeeBankAccountChange company event
            CompanyEvent.createEBAChangeEvent(foundEmployeeBankAccount, mEmployeeBankAccount);
        }

        processResult.setResult(mEmployeeBankAccount);

        return processResult;
    }

    /**
     * Validate the incoming DTO and business conditions to determine if the bank
     * account can be updated
     *
     * @return Collection of process messages, or an empty collection if all validations
     * pass
     */
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Validate DTOs
        // Employee Bank Account DTO
        validationResult.merge(mEmployeeBankAccountDTO.validateEmployeeBankAccount());

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Bank Account DTO
        if (mEmployeeBankAccountDTO.getBankAccount() == null) {
            validationResult.getMessages().BankAccountNotSpecified(EntityName.EmployeeBankAccount, mEmployeeBankAccountDTO.getEmployeeBankAccountId());
        }

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        validationResult.merge(mEmployeeBankAccountDTO.getBankAccount().validateBankAccountDTO());

        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company parameters are valid
        validationResult.merge(com.intuit.sbd.payroll.psp.api.managers.util.Validator.validCompanyParameters(mSourceSystemCd, mSourceCompanyId));
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Check if Company Exists

        Company company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);

        if (company == null) {
            validationResult.getMessages().CompanyDoesNotExist(EntityName.Company, mSourceCompanyId,
                    mSourceSystemCd.toString(), mSourceCompanyId);
            return validationResult;
        }

        if (! company.isAllowedCapability(SystemCapabilityCode.ChangeEmployeeBankAccount)) {
            validationResult.getMessages().CompanyOperationNotAllowed(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(), SystemCapabilityCode.ChangeEmployeeBankAccount.toString());
        }

        // Check if Employee Exists
        if(mEmployee == null) {
            mEmployee = Employee.findEmployee(company, mEmployeeId);
        }
        
        if (mEmployee == null) {
            validationResult.getMessages().EmployeeDoesNotExist(EntityName.Employee,
                    mEmployeeId, company.getSourceSystemCd().toString(),
                    mSourceCompanyId, mEmployeeId);
        } else {
            // Check if Employee is active
            if (company.getSourceSystemCd() != SourceSystemCode.QBDT && mEmployee.getStatusCd() != EmployeeStatus.Active) {
                validationResult.getMessages().EmployeeNotActive(EntityName.Employee, mEmployeeId,
                        mSourceSystemCd.toString(), mSourceCompanyId, mEmployeeId);
            }
            // Checks if the bank account  exists for the employee and is active
            String sourceBankAccountId = mEmployeeBankAccountDTO.getEmployeeBankAccountId();
            foundEmployeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(mEmployee, sourceBankAccountId);
            if (foundEmployeeBankAccount == null) {
                validationResult.getMessages().EmployeeBankAccountNotFound(EntityName.EmployeeBankAccount,
                        sourceBankAccountId, sourceBankAccountId, mEmployeeId);
            } else if (foundEmployeeBankAccount.getStatusCd() != BankAccountStatus.Active) {
                validationResult.getMessages().EmployeeBankAccountNotActive(EntityName.EmployeeBankAccount,
                        sourceBankAccountId, sourceBankAccountId, mEmployeeId);
            }
        }

        return validationResult;
    }

    /**
     * Populate the bank account domain object from the DTO
     *
     * @return BankAccount domain object
     */
    private BankAccount getBankAccountFromDTO() {
        BankAccountType domainBAType = BankAccountType.valueOf(mEmployeeBankAccountDTO.getBankAccount().getAccountType().toString());

        ACHBankAccountType domainACHBAType;
        if (mEmployeeBankAccountDTO.getBankAccount().getAchAccountType() != null) {
            domainACHBAType = ACHBankAccountType.valueOf(mEmployeeBankAccountDTO.getBankAccount().getAchAccountType().toString());
        } else {
            domainACHBAType = ACHBankAccountType.valueOf(mEmployeeBankAccountDTO.getBankAccount().getAccountType().toString());
        }

        BankAccount bankAccount = new BankAccount();
        bankAccount.setAccountNumber(mEmployeeBankAccountDTO.getBankAccount().getAccountNumber());
        bankAccount.setAccountTypeCd(domainBAType);
        bankAccount.setACHAccountTypeCd(domainACHBAType);
        bankAccount.setEffectiveDate(PSPDate.getPSPTime());
        bankAccount.setBankName(mEmployeeBankAccountDTO.getBankAccount().getBankName());
        bankAccount.setRoutingNumber(mEmployeeBankAccountDTO.getBankAccount().getRoutingNumber());

        return bankAccount;
    }

    private void setWalletId(EmployeeBankAccount employeeBankAccount, FeatureFlags.Key featureFlagKey) {
        String realmId = mEmployee.getCompany().getIAMRealmId();
        String psId = mEmployee.getCompany().getSourceCompanyId();
        if (!StringUtil.isNullOrEmpty(realmId)) {
            if(FeatureFlags.get().booleanValue(featureFlagKey, true)) {
                if (StringUtil.isNullOrEmpty(employeeBankAccount.getBankAccount().getWalletId())) {
                    try {
                        ProcessResult createWalletResult = new WalletCreateCore(employeeBankAccount).execute();
                    } catch (Exception e) {
                        logger.error("Wallet Creation Exception BASeq={} Realm={} PSID={}", employeeBankAccount.getId(), realmId, psId, e);
                    }
                }
            } else {
                logger.info("Wallet Creation {} Feature Flag is disabled Realm={} PSID={}", featureFlagKey, realmId, psId);
            }
        }
    }
}
