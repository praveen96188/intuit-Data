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


/**
 *
 * User: mvillani
 * Date: Aug 30, 2007
 * Time: 4:54:35 PM

 */
public class AddEmployeeBankAccountCore extends Process implements IProcess {

    private static final Logger logger = LoggerFactory.getLogger(AddEmployeeBankAccountCore.class);

    /**
     * Core process for adding a new employee bank account.
     *
     * @author Marcela Villani
     */
    private EmployeeBankAccount mEmployeeBankAccount = null;
    private EmployeeBankAccountDTO mEmployeeBankAccountDTO;
    private EmployeeBankAccount foundEmployeeBankAccount = null;
    private Employee mEmployee;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mEmployeeId;

    public EmployeeBankAccount getEmployeeBankAccount() {
        return mEmployeeBankAccount;
    }

    public AddEmployeeBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                      String pEmployeeId, EmployeeBankAccountDTO pEmployeeBankAccountDTO) {
        mEmployeeBankAccountDTO = pEmployeeBankAccountDTO;
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mEmployeeId = pEmployeeId;
    }

    // called from add employee core process
    public AddEmployeeBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                      Employee pEmployee, EmployeeBankAccountDTO pEmployeeBankAccountDTO) {
        mEmployeeBankAccountDTO = pEmployeeBankAccountDTO;
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mEmployee = pEmployee;
    }

    public ProcessResult<EmployeeBankAccount> process() {
        ProcessResult<EmployeeBankAccount> processResult = new ProcessResult<EmployeeBankAccount>();
        if(mEmployee == null) {
            Company company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
            mEmployee = Employee.findEmployee(company, mEmployeeId); //aaa
        }
        foundEmployeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(mEmployee, mEmployeeBankAccountDTO.getEmployeeBankAccountId());

        // If Employee Bank Account already exists and is inactive, expire it
        if (foundEmployeeBankAccount != null) {
            foundEmployeeBankAccount.expireEmployeeBankAccount();
            Application.save(foundEmployeeBankAccount);
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
        mEmployeeBankAccount.setBankAccount(getBankAccountFromDTO());

        // Set Source Bank Account ID
        mEmployeeBankAccount.setSourceBankAccountId(mEmployeeBankAccountDTO.getEmployeeBankAccountId());

        mEmployeeBankAccount.setAmount(mEmployeeBankAccountDTO.getAmount());
        mEmployeeBankAccount.setAmountType(mEmployeeBankAccountDTO.getAmountType());
        mEmployeeBankAccount.setAccountOrder(mEmployeeBankAccountDTO.getOrder());

        if (mEmployeeBankAccountDTO.getSessionId() != null) {
            mEmployeeBankAccount.setSessionId(mEmployeeBankAccountDTO.getSessionId());
        }

        // Associate Employee and Employee Bank Account
        mEmployeeBankAccount.setEmployee(mEmployee);

        //Set WalletId for EmployeeBankAccount
        String realmId = mEmployee.getCompany().getIAMRealmId();
        String psId = mEmployee.getCompany().getSourceCompanyId();
        if (!StringUtil.isNullOrEmpty(realmId)) {
            if (FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_WALLET_CREATION, true)) {
                try {
                    ProcessResult createWalletResult = new WalletCreateCore(mEmployeeBankAccount).execute();
                } catch (Exception e) {
                    logger.error("Wallet Creation Exception BASeq={} Realm={} PSID={}", mEmployeeBankAccount.getId(), realmId, psId, e);
                }
            } else {
                logger.info("Wallet Creation New BA Feature Flag is disabled Realm={} PSID={}", realmId, psId);
            }
        }

        // Save new Employee Bank Account

        mEmployeeBankAccount = Application.save(mEmployeeBankAccount);

        mEmployee.addEmployeeBankAccount(mEmployeeBankAccount);

        // create a EmployeeBankAccountChange company event
        EmployeeBankAccount oldEBA = foundEmployeeBankAccount;
        if(oldEBA == null) {
            // Get the current Active EBA
            Company company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
            oldEBA = EmployeeBankAccount.findLatestActiveEBA(company, mEmployee);
        }

        CompanyEvent.createEBAChangeEvent(oldEBA, mEmployeeBankAccount);

        //mEmployeeBankAccount = EmployeeBankAccountBE.findEmployeeBankAccountByEmpIdSourceBankAccountId(mEmployee, mEmployeeBankAccountDTO.getEmployeeBankAccountId());
        processResult.setResult(mEmployeeBankAccount);

        if (processResult.isSuccess()) {
            Application.getSessionCache().addPrimaryKey(mEmployeeBankAccount.getNaturalKey(), mEmployeeBankAccount.getId());
        }

        return processResult;
    }


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
        String sourceSystemCode = (null == mSourceSystemCd) ? null : mSourceSystemCd.toString();
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
        
        if (! company.isAllowedCapability(SystemCapabilityCode.ChangeEmployeeBankAccount)){
            validationResult.getMessages().CompanyOperationNotAllowed(
                company.getSourceSystemCd().toString(),
                company.getSourceCompanyId(), SystemCapabilityCode.ChangeEmployeeBankAccount.toString());
            return validationResult;
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
            // Check if Employee is active (Employee status just means the employee is hidden in QBDT)
            if (company.getSourceSystemCd() != SourceSystemCode.QBDT && mEmployee.getStatusCd() != EmployeeStatus.Active) {
                validationResult.getMessages().EmployeeNotActive(EntityName.Employee, mEmployeeId,
                        mSourceSystemCd.toString(), mSourceCompanyId, mEmployeeId);
            }

            // Checks if the bank account already exists for the employee and is Active
            String sourceBankAccountId = mEmployeeBankAccountDTO.getEmployeeBankAccountId();
            foundEmployeeBankAccount = EmployeeBankAccount.findEmployeeBankAccount(mEmployee, sourceBankAccountId);
            if (foundEmployeeBankAccount != null && foundEmployeeBankAccount.getStatusCd() != BankAccountStatus.Inactive) {
                validationResult.getMessages().EmployeeBankAccountAlreadyExists(EntityName.EmployeeBankAccount, sourceBankAccountId, sourceBankAccountId, mEmployeeId);
                mEmployeeBankAccount = foundEmployeeBankAccount;
                validationResult.setResult(mEmployeeBankAccount);
            }
        }
        return validationResult;

    }

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

        return Application.save(bankAccount);
    }
}
