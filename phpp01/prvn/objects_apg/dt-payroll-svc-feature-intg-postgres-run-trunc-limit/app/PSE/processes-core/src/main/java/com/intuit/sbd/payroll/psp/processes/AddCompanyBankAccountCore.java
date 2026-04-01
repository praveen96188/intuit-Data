package com.intuit.sbd.payroll.psp.processes;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.payroll.authorization.utils.RequestSourceIdentifier;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;


/**
 * User: mvillani
 * Date: Jul 31, 2007
 * Time: 2:09:15 PM
 */
public final class AddCompanyBankAccountCore extends Process implements IProcess {

    private static final SpcfLogger logger = PayrollServices.getLogger(AddCompanyBankAccountCore.class);

    /**
     * Core process for adding a new company bank account.
     *
     * @author Marcela Villani
     */

    private Company mCompany;
    private CompanyBankAccountDTO mCompanyBankAccountDTO;
//    private CompanyBankAccount foundCompanyBankAccount;
    private CompanyBankAccount mCompanyBankAccount, mOldCBA;
    private SourceSystemCode mSourceSystemCd;
    private String mSourceCompanyId;
    private boolean shouldAddRandomDebits;
    private boolean shouldCheckForExistingBA;
    private DeactivateCompanyBankAccountCore deactivateActiveCBAprocess;
    private boolean isPSPRandomDollarVerificationRequired;
    private RequestSourceIdentifier requestSourceIdentifier;


    public AddCompanyBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                     CompanyBankAccountDTO pCompanyBankAccountDTO, boolean pShouldAddRandomDebits,
                                     boolean pShouldCheckForExistingBA) {
        this(pSourceSystemCd,pSourceCompanyId,pCompanyBankAccountDTO,pShouldAddRandomDebits,pShouldCheckForExistingBA,true);
    }

    public AddCompanyBankAccountCore(SourceSystemCode pSourceSystemCd, String pSourceCompanyId,
                                     CompanyBankAccountDTO pCompanyBankAccountDTO, boolean pShouldAddRandomDebits,
                                     boolean pShouldCheckForExistingBA,boolean pIsPSPRandomDollarVerificationRequired) {
        mSourceSystemCd = pSourceSystemCd;
        mSourceCompanyId = pSourceCompanyId;
        mCompanyBankAccountDTO = pCompanyBankAccountDTO;
        shouldAddRandomDebits = pShouldAddRandomDebits;
        shouldCheckForExistingBA = pShouldCheckForExistingBA;
        isPSPRandomDollarVerificationRequired = pIsPSPRandomDollarVerificationRequired;
        this.requestSourceIdentifier = PayrollApplicationBeanFactory.getBean(RequestSourceIdentifier.class);

    }


    public CompanyBankAccount getCompanyBankAccount() {
        return mCompanyBankAccount;
    }

    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        // if we need to deactivate the currently-active CBA, do so now...
        // (doing this later risks re-deactivating an existing CBA that we reactivate)
        if (deactivateActiveCBAprocess != null) {
            processResult.merge(deactivateActiveCBAprocess.process());
        }
        
        CompanyBankAccount companyBankAccount = null;
        mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
        BankAccount bankAccount = getBankAccountFromDTO();
        CompanyBankAccount oldCBA, foundCompanyBankAccount =
                CompanyBankAccount.findExistingCBAIncludingExpired(mCompany,
                        mCompanyBankAccountDTO.getCompanyBankAccountID(), bankAccount);
        // If the company bank account already exists and is inactive re-activate it
        if (foundCompanyBankAccount != null) {
                //if everything is same except bank name re-activate the existing
                companyBankAccount = foundCompanyBankAccount;
                companyBankAccount.setExpirationDate(null);
                oldCBA = Application.save(companyBankAccount);

        } else {
            bankAccount = Application.save(bankAccount);

            // If mOldCBA is null then take the latest CBA which is Active
            if(mOldCBA != null)
                oldCBA = mOldCBA;
            else
                oldCBA = CompanyBankAccount.findCompanyBankAccount(mCompany, mCompanyBankAccountDTO.getCompanyBankAccountID());

            // Create a new company bank account
            companyBankAccount = new CompanyBankAccount();
            // Link company and companybankaccount
            companyBankAccount.setCompany(mCompany);
            companyBankAccount.setEffectiveDate(PSPDate.getPSPTime());
            companyBankAccount.setBankAccount(bankAccount);            
        }

        // Default values
        companyBankAccount.setStatusCd(BankAccountStatus.PendingVerification);
        companyBankAccount.setStatusEffectiveDate(PSPDate.getPSPTime());
        companyBankAccount.setVerifyRetryCount(0L);

        // Get Values from DTO
        companyBankAccount.setSourceBankAccountId(mCompanyBankAccountDTO.getCompanyBankAccountID());
        companyBankAccount.setSourceBankAccountName(mCompanyBankAccountDTO.getSourceBankAccountName());

        // Save Company Bank Account
        companyBankAccount = Application.save(companyBankAccount);

        // Raise CBA change event & notify Company_FK
        CompanyEvent.createCBAChangeEvent(oldCBA, companyBankAccount, !shouldAddRandomDebits);

        // If new company bank account, need to add to companyBankAccount collection in company
        if (foundCompanyBankAccount == null || foundCompanyBankAccount.getStatusCd().equals(BankAccountStatus.Inactive)) {
            companyBankAccount.getCompany().addCompanyBankAccount(companyBankAccount);
        }

        if(isPSPRandomDollarVerificationRequired){
            if (shouldAddRandomDebits) {

                if(isMoneyMovementOnboardingEnabled()){
                    logger.info("Skipping PSP Random Dollar Verification due to TRON activation");
                }else{
                    // Add two verification transactions
                    for (int i = 0; i < 2; i++) {
                        FinancialTransaction ft = companyBankAccount.addVerificationTransaction();
                        NaturalKey naturalKey = new NaturalKey(FinancialTransaction.class, companyBankAccount.getId(), i);
                        Application.getSessionCache().addPrimaryKey(naturalKey, ft.getId());
                    }
                }

            } else {
                    companyBankAccount.updateBankAccountStatus(BankAccountStatus.Active);

                    for (CompanyService companyService : mCompany.getCompanyServiceCollection()) {
                        ServiceSubStatusCode nextServiceSubStatusCd = companyService.getNextValidServiceStatus(ServiceSubStatusCode.PendingBankVerification);
                        companyService.updateCompanyServiceStatus(nextServiceSubStatusCd);
                    }
                    CompanyEvent.createCBAVerifiedEvent(companyBankAccount);

            }
        } else {
            logger.info("Skipping PSP Random Dollar Verification due to TRON activation");
        }


        //Find already-existing companies that have er or ee bank accounts in common with the new company bank account to be added
        StringBuilder fraudNotes = new StringBuilder();
        if (companyBankAccount.bankAccountMeetsFraudCriteria(fraudNotes)) {
            mCompany = Company.findCompany(mSourceCompanyId, mSourceSystemCd);
            processResult.getMessages().CompanyPendingActivation(EntityName.Company, mSourceCompanyId, mSourceSystemCd.toString(), mSourceCompanyId);
            PayrollServices.companyManager.addOnHoldReason(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), ServiceSubStatusCode.FraudReview);

            //Create CompanyMatchesFraudulentCompany event
            CompanyEvent.createFraudSignUpEvent(mCompany, EventTypeCode.CompanyMatchesFraudulentCompany, fraudNotes.toString());
        }

        mCompanyBankAccount = companyBankAccount;
        if (processResult.isSuccess()) {
            Application.getSessionCache().addPrimaryKey(mCompanyBankAccount.getNaturalKey(), mCompanyBankAccount.getId());
        }
        return processResult;
    }

    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        // Validate if Company DTO is null
        if (mCompanyBankAccountDTO == null) {
            validationResult.getMessages().CompanyBankAccountNotSpecified(EntityName.CompanyBankAccount, mSourceCompanyId);
            return validationResult;
        }

        // Validate Company Bank Account DTO
        ProcessResult validateCompanyBankAccountResult = mCompanyBankAccountDTO.validateCompanyBankAccount();
        validationResult.merge(validateCompanyBankAccountResult);
        if (!validationResult.isSuccess()) {
            return validationResult;
        }

        // Validate Bank Account DTO because it's optional for companybankaccountdto
        if (mCompanyBankAccountDTO.getBankAccountDTO() == null) {
            validationResult.getMessages().BankAccountNotSpecified(EntityName.EmployeeBankAccount, mCompanyBankAccountDTO.getCompanyBankAccountID());
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
            return validationResult;
        }

        DomainEntitySet<CompanyBankAccount> cbaList = CompanyBankAccount.findCompanyBankAccounts(foundCompany);

        //If the CompanyBankAccount is adding first time to the company, then use 'AddEmployerBankAccount' for
        //the SystemCapabilityCode other wise user 'ChangeEmployerBankAccount' as the SystemCapabilityCode
        if (cbaList.size() == 0) {
            if (!foundCompany.isAllowedCapability(SystemCapabilityCode.AddEmployerBankAccount)) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        foundCompany.getSourceSystemCd().toString(),
                        foundCompany.getSourceCompanyId(), SystemCapabilityCode.AddEmployerBankAccount.toString());
            }
        } else {
            if (!foundCompany.isAllowedCapability(SystemCapabilityCode.ChangeEmployerBankAccount)) {
                validationResult.getMessages().CompanyOperationNotAllowed(
                        foundCompany.getSourceSystemCd().toString(),
                        foundCompany.getSourceCompanyId(), SystemCapabilityCode.ChangeEmployerBankAccount.toString());
            }
        }

        // Check if the bank account already exists for the company and if it does exits, if it is inactive

        String sourceBankAccountId = mCompanyBankAccountDTO.getCompanyBankAccountID();
        CompanyBankAccount foundActiveCBAWithSameSrcId =
                CompanyBankAccount.findCompanyBankAccount(foundCompany,
                        sourceBankAccountId);
        if (shouldCheckForExistingBA) {
            if (foundActiveCBAWithSameSrcId != null) {
                if (foundActiveCBAWithSameSrcId.getStatusCd() != BankAccountStatus.Inactive) {
                    // active company bank account exists with the same source bank account Id
                    // return error message
                    validationResult.getMessages().CompanyBankAccountAlreadyExists(EntityName.CompanyBankAccount,
                            sourceBankAccountId, sourceBankAccountId, mSourceSystemCd.toString(), mSourceCompanyId);
                    mCompanyBankAccount = foundActiveCBAWithSameSrcId;
                    return validationResult;
                }
            }
        }

        // Try to deactivate old active CBA if the Direct Deposit reactivation happens from TRON workflow (Payroll Plugin)
        if (false == shouldAddRandomDebits || !isPSPRandomDollarVerificationRequired) {
            // The new CBA will be activated immediately, so we need to deactivate a current active CBA
            mOldCBA = CompanyBankAccount.findActiveCompanyBankAccount(foundCompany);
            if (mOldCBA != null) {
                logger.info("Trying to deactivate Active Company Bank Account because shouldAddRandomDebits="+shouldAddRandomDebits+" and isPSPRandomDollarVerificationRequired="+ isPSPRandomDollarVerificationRequired);
                deactivateActiveCBAprocess = new DeactivateCompanyBankAccountCore(
                        mSourceSystemCd, mSourceCompanyId, mOldCBA.getSourceBankAccountId(), true, false);
                validationResult.merge(deactivateActiveCBAprocess.validate());
            }
        }

        return validationResult;
    }

    /**
     * Builds a BankAccount domain object from a Bank Account DTO
     *
     * @return
     */
    private BankAccount getBankAccountFromDTO() {
        BankAccount bankAccount = new BankAccount();

        bankAccount.setAccountNumber(mCompanyBankAccountDTO.getBankAccountDTO().getAccountNumber());
        BankAccountType domainBAType = BankAccountType.valueOf(mCompanyBankAccountDTO.getBankAccountDTO().getAccountType().toString());
        bankAccount.setAccountTypeCd(domainBAType);
        bankAccount.setACHAccountTypeCd(ACHBankAccountType.valueOf(domainBAType.toString()));
        bankAccount.setEffectiveDate(PSPDate.getPSPTime());
        bankAccount.setBankName(mCompanyBankAccountDTO.getBankAccountDTO().getBankName());
        bankAccount.setRoutingNumber(mCompanyBankAccountDTO.getBankAccountDTO().getRoutingNumber());

        return bankAccount;
    }

    private boolean isMoneyMovementOnboardingEnabled() {
        return requestSourceIdentifier.isPayrollPlugin() || mCompany.isMoneyMovementOnboardingEnabled();
    }
}


