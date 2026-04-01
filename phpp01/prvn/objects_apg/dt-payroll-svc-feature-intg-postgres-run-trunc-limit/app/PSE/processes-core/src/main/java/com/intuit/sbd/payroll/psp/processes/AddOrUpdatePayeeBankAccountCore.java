package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.dtos.BankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayeeBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.processes.wallet.WalletCreateCore;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * User: mvillani
 * Date: Aug 30, 2007
 * Time: 4:54:35 PM
 */
public class AddOrUpdatePayeeBankAccountCore extends Process implements IProcess {

    /**
     * Core process for adding a new payee bank account.
     *
     * @author Marcela Villani
     */
    private static final Logger logger = LoggerFactory.getLogger(AddOrUpdatePayeeBankAccountCore.class);

    private PayeeBankAccount mPayeeBankAccount = null;
    private PayeeBankAccountDTO mPayeeBankAccountDTO;
    private PayeeBankAccount foundPayeeBankAccount = null;
    private Payee mPayee;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private String mSourcePayeeId;


    public PayeeBankAccount getPayeeBankAccount() {
        return mPayeeBankAccount;
    }

    public AddOrUpdatePayeeBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                           String pPayeeId, PayeeBankAccountDTO pPayeeBankAccountDTO) {
        mPayeeBankAccountDTO = pPayeeBankAccountDTO;
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mSourcePayeeId = pPayeeId;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        PayeeBankAccount oldPBA = null;
        boolean addCompanyEvent = true;
        if (foundPayeeBankAccount != null) {
            oldPBA = mPayeeBankAccount = foundPayeeBankAccount;

            BankAccount bankAccount = getBankAccountFromDTO(null);
            // Raise PBA Event if something other than the bank name has changed.
            if (bankAccount.equalsIgnoreBankNameSourceBankName(foundPayeeBankAccount.getBankAccount())) {
                foundPayeeBankAccount.getBankAccount().setBankName(mPayeeBankAccountDTO.getBankAccount().getBankName());

                //Create wallet for existing vendor bank account if not created already
                setWalletId(foundPayeeBankAccount, FeatureFlags.Key.ENABLE_PAYEE_WALLET_CREATION_EXISTING_BA);

                addCompanyEvent = false;
            }

            // TODO : Create new Record

            // Expire all old PBA
            // PayeeBankAccount.deactivateAllActivePBA(mPayee);
        } else {
            // Create new Payee Bank Account
            mPayeeBankAccount = new PayeeBankAccount();

            Company company = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
            oldPBA = PayeeBankAccount.findActivePayeeBankAccount(company, mPayee);
        }

        String oldAccNum = null, oldRoutingNum = null;
        if(oldPBA != null) {
            oldAccNum = oldPBA.getBankAccount().getAccountNumber();
            oldRoutingNum = oldPBA.getBankAccount().getRoutingNumber();
        }

        // Set Status =  Active
        mPayeeBankAccount.setStatusCd(BankAccountStatus.Active);
        if(addCompanyEvent) {
            // Set status effective date to current date
            mPayeeBankAccount.setStatusEffectiveDate(PSPDate.getPSPTime());
        }


        // Set effective date to current date
        mPayeeBankAccount.setEffectiveDate(PSPDate.getPSPTime());

        // Set Expiration Date to null
        mPayeeBankAccount.setExpirationDate(null);

        // Set Bank Account
        BankAccount newBA = Application.save(getBankAccountFromDTO(mPayeeBankAccount));
        mPayeeBankAccount.setBankAccount(newBA);
        if (mPayeeBankAccountDTO.getBankAccount().getAchEntryClass() == null) {
            mPayeeBankAccount.getBankAccount().setACHEntryClass(EntryClassCode.PPD);
        }

        // Set Source Bank Account ID
        mPayeeBankAccount.setSourceBankAccountId(mPayeeBankAccountDTO.getPayeeBankAccountId());
        
        if(mPayeeBankAccountDTO.getBankAccount()!=null){
            mPayeeBankAccount.setSessionId(mPayeeBankAccountDTO.getBankAccount().getSessionId());
        }

        // Associate Payee and Payee Bank Account
        mPayeeBankAccount.setPayee(mPayee);

        //set walletId for new PayeeBankAccount
        if(addCompanyEvent) {
            setWalletId(mPayeeBankAccount, FeatureFlags.Key.ENABLE_PAYEE_WALLET_CREATION);
        }

        if (mPayeeBankAccountDTO.getSessionId() != null) {
            mPayeeBankAccount.setSessionId(mPayeeBankAccountDTO.getSessionId());
        }
        // Save new Employee Bank Account
        mPayeeBankAccount = Application.save(mPayeeBankAccount);

        mPayee.addPayeeBankAccount(mPayeeBankAccount);

        processResult.setResult(mPayeeBankAccount);

        if(addCompanyEvent) {
            CompanyEvent.createPBAChangeEvent(oldAccNum, oldRoutingNum, mPayeeBankAccount);
        }

        if (processResult.isSuccess()) {
            Application.getSessionCache().addPrimaryKey(mPayeeBankAccount.getNaturalKey(), mPayeeBankAccount.getId());
        }

        return processResult;
    }


    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Validate DTOs
        // Payee Bank Account DTO
        validationResult.merge(mPayeeBankAccountDTO.validatePayeeBankAccount());
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Bank Account DTO
        if (mPayeeBankAccountDTO.getBankAccount() == null) {
            validationResult.getMessages().BankAccountNotSpecified(EntityName.PayeeBankAccount, mPayeeBankAccountDTO.getPayeeBankAccountId());
        }
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        validationResult.merge(mPayeeBankAccountDTO.getBankAccount().validateBankAccountDTO());
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

        // Check if Payee Exists
        mPayee = Payee.findPayee(company, mSourcePayeeId);
        if (mPayee == null) {
            validationResult.getMessages().PayeeDoesNotExist(EntityName.Payee,
                    mSourcePayeeId, company.getSourceSystemCd().toString(),
                    mSourceCompanyId, mSourcePayeeId);
        } else {
            // Checks if the bank account already exists for the Payee and is Active
            String sourceBankAccountId = mPayeeBankAccountDTO.getPayeeBankAccountId();
            foundPayeeBankAccount = PayeeBankAccount.findPayeeBankAccount(mPayee, sourceBankAccountId);
            if (foundPayeeBankAccount == null) {
                BankAccountDTO bankAccountDTO = mPayeeBankAccountDTO.getBankAccount();
                foundPayeeBankAccount = PayeeBankAccount.findActivePayeeBankAccount(company,
                                                                                    mSourcePayeeId,
                                                                                    bankAccountDTO.getAccountNumber(),
                                                                                    bankAccountDTO.getRoutingNumber(),
                                                                                    bankAccountDTO.getAccountType());
                /* If bank account is found here which means that
                   already existing old bank account is now being made the payment and selected as active bank account
                   This will change the default status of wallet id hence setting null,
                   which will create new wallet with correct default behaviour as true */
                if(Objects.nonNull(foundPayeeBankAccount)) {
                    foundPayeeBankAccount.getBankAccount().setWalletId(null);
                }
            }
        }
        return validationResult;
    }

    private BankAccount getBankAccountFromDTO(PayeeBankAccount pPayeeBankAccount) {
        BankAccount bankAccount = (pPayeeBankAccount != null ? pPayeeBankAccount.getBankAccount() : null);
        if (bankAccount == null) {
             bankAccount = new BankAccount();
        }

        BankAccountType domainBAType = BankAccountType.valueOf(mPayeeBankAccountDTO.getBankAccount().getAccountType().toString());
        ACHBankAccountType domainACHBAType;
        if (mPayeeBankAccountDTO.getBankAccount().getAchAccountType() != null) {
            domainACHBAType = ACHBankAccountType.valueOf(mPayeeBankAccountDTO.getBankAccount().getAchAccountType().toString());
        } else {
            domainACHBAType = ACHBankAccountType.valueOf(mPayeeBankAccountDTO.getBankAccount().getAccountType().toString());
        }

        bankAccount.setAccountTypeCd(domainBAType);
        bankAccount.setACHAccountTypeCd(domainACHBAType);
        bankAccount.setEffectiveDate(PSPDate.getPSPTime());
        bankAccount.setBankName(mPayeeBankAccountDTO.getBankAccount().getBankName());
        bankAccount.setRoutingNumber(mPayeeBankAccountDTO.getBankAccount().getRoutingNumber());
        bankAccount.setAccountNumber(mPayeeBankAccountDTO.getBankAccount().getAccountNumber());

        return bankAccount;
    }

    //Based on FF value, set wallet id for new or existing bank account
    public void setWalletId(PayeeBankAccount payeeBankAccount, FeatureFlags.Key featureFlags) {
        String realmId = mPayee.getCompany().getIAMRealmId();
        String psId = mPayee.getCompany().getSourceCompanyId();
        if (!StringUtil.isNullOrEmpty(realmId)) {
            if (FeatureFlags.get().booleanValue(featureFlags, true)) {
                if (StringUtil.isNullOrEmpty(payeeBankAccount.getBankAccount().getWalletId())) {
                    try {
                        ProcessResult createWalletResult = new WalletCreateCore(payeeBankAccount).execute();
                    } catch (Exception e) {
                        logger.error("Wallet Creation Exception BASeq={} Realm={} PSID={}", payeeBankAccount.getId(), realmId, psId, e);
                    }
                }
            } else {
                logger.info("Wallet Creation {} Feature Flag is disabled Realm={} PSID={}", featureFlags, realmId, psId);
            }
        }


    }
}