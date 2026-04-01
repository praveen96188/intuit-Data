package com.intuit.sbd.payroll.psp.processes.accountservice;


import com.intuit.payments.cdm.v2.client.BankAccount;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.domain.util.ThreadLocalManager;
import com.intuit.sbd.payroll.psp.gateways.accountservice.AccountServiceSyncDecisionManager;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbd.payroll.psp.gateways.accountservice.translator.AccountServiceTranslator;
import com.intuit.sbd.payroll.psp.processes.AMSValidationProcess;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.*;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class AccountServiceSyncCore extends Process{
    private static final SpcfLogger logger = SpcfLogManager.getLogger(AccountServiceSyncCore.class);
    private String realmId;
    private Company mDomainCompany;
    private AccountServiceGateway accountServiceGateway;
    private AccountServiceTranslator accountServiceTranslator;
    private AccountServiceSyncDecisionManager decisionManager;
    private PaymentsAccount paymentsAccount;
    private CompanyDTO companyDTO;
    private Set<ServiceSubStatusCode> removedHolds;

    public AccountServiceSyncCore(String realmId) {
        this.realmId = realmId;
        accountServiceGateway = PayrollApplicationBeanFactory.getBean(AccountServiceGateway.class);
        decisionManager = PayrollApplicationBeanFactory.getBean(AccountServiceSyncDecisionManager.class);
        accountServiceTranslator = PayrollApplicationBeanFactory.getBean(AccountServiceTranslator.class);
        removedHolds = new HashSet<>();
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();

        try {
            mDomainCompany =  Company.findActiveCompanyByRealmId(realmId);
        } catch (RuntimeException e) {
            validationResult.getMessages().DuplicateActiveCompaniesFoundForRealm(EntityName.Company, realmId);
            return validationResult;
        }

        if (isNull(mDomainCompany)) {
            logger.info("Company Not found for RealmId="+realmId);
            validationResult.getMessages().TronCompanyDoesNotExistOnRealmId(EntityName.Company, realmId);
            return validationResult;
        }

        if( !mDomainCompany.isMoneyMovementOnboardingEnabled()){
            logger.info("Money Movement Flag is not set for PSID="+mDomainCompany.getSourceCompanyId());
            validationResult.getMessages().TronCompanyDoesNotExistOnRealmId(EntityName.Company, realmId);
            return validationResult;
        }

        try {
            paymentsAccount = accountServiceGateway.getPaymentsAccount(realmId);
        } catch (Exception accountServicesException) {

            validationResult.getMessages().MoneymovementAccountDoesNotExistOnRealmId(EntityName.Company, realmId);


            String failureReason = accountServicesException.getMessage();
            validationResult.getMessages().AStoPSPSyncError(realmId, failureReason);

            PayrollServices.executeTransactionThread(new TransactionThread() {
                public ProcessResult transaction() {
                    SMSSyncFailure.saveSMSSyncFailureIfAbsent(realmId, failureReason);
                    Company localCompany = Application.findById(Company.class, mDomainCompany.getId());
                    CompanyEvent.createSMSSyncEvent(realmId, localCompany, EventTypeCode.SMSToPSPSyncFailure, Optional.of(failureReason));
                    return new ProcessResult();
                }
            });


            return validationResult;
        }

        if (isNull(paymentsAccount) || !decisionManager.isPaymentsAccountValid(paymentsAccount)) {
            logger.info("There is no Tron enabled Payments Account for RealmId="+realmId);
            validationResult.getMessages().MoneymovementAccountDoesNotExistOnRealmId(EntityName.Company, realmId);
            return validationResult;
        }

        companyDTO=accountServiceTranslator.getUpdatedPSPCompanyDTO(paymentsAccount,mDomainCompany);

        validationResult.setResult(mDomainCompany);
        return validationResult;
    }


    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();
        Boolean isAMSMigrated = null;
        SMSMigrationStatus smsMigrationStatus = null;
        try {

            isAMSMigrated = mDomainCompany.isMoneyMovementOnboardingEnabled();
            smsMigrationStatus = SMSMigration.getSMSMigrationStatusByCompany(mDomainCompany);

            processResult.merge(removeHolds());

            //If there is a hold the system will not allow to update PSP so we remove holds temporarily and put back in the last after update
            processResult.merge(removeTemporaryHolds());

            // This is for the the activation of the cancelled service . We will enable this once Account service supports this service
            // processResult.merge(activate());
            processResult.merge(updateCompany());
            processResult.merge(updateBank());
            processResult.merge(createVBDEvents());

            processResult.merge(activateBank());
            processResult.merge(deactivate());

            //Putting back the holds removed as part of the step removeTemporaryHolds()
            processResult.merge(putRemovedTemporaryHolds());
            processResult.merge(putNewHolds());

            logger.info("Action=AccountServiceSyncCore_process, status=complete, realmId=" + realmId + ", psid=" +
                    (mDomainCompany == null ? EMPTY : mDomainCompany.getSourceCompanyId()) + ", AMS_Migrated=" + isAMSMigrated + ", MigrationStatus="
                    + smsMigrationStatus);

        } catch (Exception e) {

            logger.error("Action=AccountServiceSyncCore_process, status=Error, realmId=" + realmId + ", psid=" +
                    (mDomainCompany == null ? EMPTY : mDomainCompany.getSourceCompanyId()) + ", AMS_Migrated=" + isAMSMigrated + ", MigrationStatus="
                    + smsMigrationStatus + ", errType=" + e.getClass().getSimpleName() + ", errMsg=" + e.getMessage(), e);
            throw e;
        } finally {
            ThreadLocalManager.flushHoldEventCreationRequired();
        }

        validateAMSUpdate(smsMigrationStatus);

        return processResult;
    }

    private ProcessResult activate() {
        ProcessResult processResult = new ProcessResult();
        if(!decisionManager.isActivateServiceRequired(paymentsAccount)){
            logger.info("Skipping service Activate step");
            return processResult;
        }

        processResult.merge(activateService(mDomainCompany,ServiceCode.Tax));
        processResult.merge(activateService(mDomainCompany,ServiceCode.DirectDeposit));
        return processResult;
    }

    private ProcessResult activateService(Company mDomainCompany,ServiceCode serviceCode) {
        ProcessResult processResult=new ProcessResult();
        CompanyService companyService = CompanyService.findCompanyService(mDomainCompany, serviceCode);
        if(!isNull(companyService) && companyService.isCancelTerm()){
            logger.info("Activating service "+serviceCode);

            return PayrollServices.companyManager.reactivateService(mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), serviceCode);

        }
        return processResult;
    }

    private ProcessResult deactivate() {
        ProcessResult processResult = new ProcessResult();
        if(!decisionManager.isDeactivateServiceRequired(paymentsAccount)){
            logger.info("Skipping service deactivate step");
            return processResult;
        }

        processResult.merge(deactivateService(mDomainCompany,ServiceCode.Tax));
        processResult.merge(deactivateService(mDomainCompany,ServiceCode.DirectDeposit));
        return processResult;
    }

    private ProcessResult deactivateService(Company mDomainCompany,ServiceCode serviceCode) {
        ProcessResult processResult=new ProcessResult();
        CompanyService companyService = CompanyService.findCompanyService(mDomainCompany, serviceCode);
        if(!isNull(companyService) && !companyService.isCancelTerm()){
            logger.info("DeActivating service "+serviceCode);

            return PayrollServices.companyManager.deactivateService(mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), serviceCode);

        }
        return processResult;
    }

    private ProcessResult removeHolds() {
        ProcessResult processResult = new ProcessResult();
        Collection<ServiceSubStatusCode> holdsToRemove= decisionManager.getHoldsTobeRemoved(paymentsAccount,mDomainCompany);
        if(holdsToRemove.size()==0){
            logger.info("Skipping removePermanentHolds step");
            return processResult;
        }


        for (ServiceSubStatusCode onHoldReasonCd : holdsToRemove) {
            logger.info("Removing hold"+onHoldReasonCd.name());
            ThreadLocalManager.setHoldEventCreationRequired(true);
            ProcessResult pr = PayrollServices.companyManager.removeOnHoldReason(mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), onHoldReasonCd);
            processResult.merge(pr);
            if (!processResult.isSuccess()) {
                logger.error("Failed to remove Hold reason " + onHoldReasonCd.toString() + "from Company ID:" + mDomainCompany.getSourceCompanyId() + " source system code:" + mDomainCompany.getSourceSystemCd());
            }

        }
        return processResult;
    }

    private ProcessResult removeTemporaryHolds() {
        ProcessResult processResult = new ProcessResult();
        Collection<ServiceSubStatusCode> holdsToRemove= decisionManager.getHoldsTobeRemovedTemporary(paymentsAccount,mDomainCompany);
        if(holdsToRemove.size()==0){
            logger.info("Skipping removeTemporaryHolds step");
            return processResult;
        }


        for (ServiceSubStatusCode onHoldReasonCd : holdsToRemove) {
            logger.info("Removing hold"+onHoldReasonCd.name());
            ThreadLocalManager.setHoldEventCreationRequired(false);
            removedHolds.add(onHoldReasonCd);
            ProcessResult pr = PayrollServices.companyManager.removeOnHoldReason(mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), onHoldReasonCd);
            processResult.merge(pr);
            if (!processResult.isSuccess()) {
                logger.error("Failed to remove Hold reason " + onHoldReasonCd.toString() + "from Company ID:" + mDomainCompany.getSourceCompanyId() + " source system code:" + mDomainCompany.getSourceSystemCd());
            }

        }
        return processResult;
    }


    private ProcessResult putNewHolds() {

        ProcessResult processResult = new ProcessResult();
        ServiceSubStatusCode newHolds=decisionManager.getHoldsForNonActiveCompany(paymentsAccount);

        if(newHolds == null || decisionManager.getCurrentHolds(mDomainCompany).contains(newHolds)){
            logger.info("Skipping putOnHold step");
            return processResult;
        }

        logger.info("putting hold  "+newHolds.name());
        ThreadLocalManager.setHoldEventCreationRequired(true);
        ProcessResult pr = PayrollServices.companyManager.addOnHoldReason(mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), newHolds);
        processResult.merge(pr);
        if (!processResult.isSuccess()) {
            logger.error("Failed to put Hold reason " + newHolds.toString() + "from Company ID:" + mDomainCompany.getSourceCompanyId() + " source system code:" + mDomainCompany.getSourceSystemCd());

        }

        return processResult;
    }

    private ProcessResult putRemovedTemporaryHolds() {

        ProcessResult processResult = new ProcessResult();

        if(removedHolds.size()==0){
            logger.info("Skipping putOnHold step");
            return processResult;
        }

        for (ServiceSubStatusCode onHoldReasonCd : removedHolds) {
            logger.info("putting hold  "+onHoldReasonCd.name());
            ThreadLocalManager.setHoldEventCreationRequired(false);
            ProcessResult pr = PayrollServices.companyManager.addOnHoldReason(mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), onHoldReasonCd);
            processResult.merge(pr);
            if (!processResult.isSuccess()) {
                logger.error("Failed to put Hold reason " + onHoldReasonCd.toString() + "from Company ID:" + mDomainCompany.getSourceCompanyId() + " source system code:" + mDomainCompany.getSourceSystemCd());

            }
        }
        return processResult;
    }



    private ProcessResult updateCompany() {
        ProcessResult processResult = new ProcessResult();

        if(!decisionManager.isUpdateCompanyRequired(companyDTO,mDomainCompany)){
            logger.info("Skipping updateCompany step");
            return processResult;
        }
        logger.info("Updating the Company attributes");
        processResult = PayrollServices.companyManager.updateCompany(mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), companyDTO);

        return processResult;
    }

    private ProcessResult createVBDEvents() {
        ProcessResult processResult=new ProcessResult();
        decisionManager.handleVBDEvents(paymentsAccount, mDomainCompany, realmId);
        return processResult;
    }


    private ProcessResult activateBank() {

        ProcessResult processResult=new ProcessResult();
        if(!decisionManager.isActivateBankAccountRequired(paymentsAccount,mDomainCompany, realmId)){
            logger.info("Skipping activate bank step");
            return processResult;
        }
        BankAccount paymentsBankAccount = accountServiceTranslator.getPaymentBank(paymentsAccount);

        CompanyBankAccount companyBankAccount = accountServiceTranslator.getCompanyBankAccountByAccountNumber(paymentsBankAccount, mDomainCompany);
        logger.info("Action=activateBank, method=findCompanyBankAccountByAccountNumber, isPspCompanyBankAccountFound=" + Objects.nonNull(companyBankAccount) + ", realmId=" + realmId);

        logger.info("Activating the company bankAccount="+ companyBankAccount.getSourceBankAccountId());

        processResult = PayrollServices.companyManager.verifyCompanyBankAccount(mDomainCompany.getSourceSystemCd(),mDomainCompany.getSourceCompanyId(),companyBankAccount.getSourceBankAccountId(),
                null,null,true);
        return processResult;
    }


    private ProcessResult updateBank() {

        ProcessResult processResult=new ProcessResult();
        if(!decisionManager.isUpdateBankAccountRequired(paymentsAccount,mDomainCompany, realmId)){
            logger.info("Skipping updateBank step, Action=updateBank, updateRequired=false, realmId=" + realmId);
            return processResult;
        }else{
            logger.info("Action=updateBank, updateRequired=true, realmId=" + realmId);
        }

        CompanyBankAccount pspCompanyBankAccount =  CompanyBankAccount.findCompanyBankAccount(mDomainCompany);
        logger.info("Action=updateBank, method=findCompanyBankAccountByStatus, isPspCompanyBankAccountFound=" + Objects.nonNull(pspCompanyBankAccount) + ", realmId=" + realmId);

        com.intuit.sbd.payroll.psp.domain.BankAccount pspBankAccount = pspCompanyBankAccount.getBankAccount();

        BankAccount paymentsBankAccount = accountServiceTranslator.getPaymentBank(paymentsAccount);

        try {
            CompanyBankAccount pspCompanyBankAccountByAccountNumber = accountServiceTranslator.getCompanyBankAccountByAccountNumber(paymentsBankAccount, mDomainCompany);
            logger.info("Action=updateBank, method=findCompanyBankAccountByAccountNumber, isPspCompanyBankAccountFound=" + Objects.nonNull(pspCompanyBankAccountByAccountNumber) + ", realmId=" + realmId);
        } catch (Exception e) {
            logger.error("Action=updateBank, status=Error, method=findCompanyBankAccountByAccountNumber, realmId=" + realmId, e);
        }

        CompanyBankAccountDTO companyBankAccountDTO = accountServiceTranslator.getUpdatedCompanyBankAccountDTO(paymentsAccount, pspCompanyBankAccount);

        boolean bankAccountSame = decisionManager.isBankAccountEquals(paymentsBankAccount, pspBankAccount, realmId);

        if (!bankAccountSame) {
            return changeBankAccount(companyBankAccountDTO,mDomainCompany, realmId);
        } else {
            return updateBankAccount(companyBankAccountDTO,mDomainCompany, realmId);
        }

    }

    private ProcessResult changeBankAccount(CompanyBankAccountDTO companyBankAccountDTO,Company mDomainCompany, String realmId) {
        logger.info("Action=changeBankAccount, realmId=" + realmId);
        ProcessResult<CompanyBankAccount> companyBankAccountPR = null;
        companyBankAccountPR = PayrollServices.companyManager.changeCompanyBankAccountWithAccountService
                (mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), companyBankAccountDTO, false, false, false,false,false);
        return companyBankAccountPR;
    }


    private ProcessResult updateBankAccount(CompanyBankAccountDTO companyBankAccountDTO,Company mDomainCompany, String realmId) {
        logger.info("Action=updateBankAccount, realmId=" + realmId);
        ProcessResult<CompanyBankAccount> companyBankAccountPR = null;
        companyBankAccountPR = PayrollServices.companyManager.updateCompanyBankAccount
                (mDomainCompany.getSourceSystemCd(), mDomainCompany.getSourceCompanyId(), companyBankAccountDTO);

        return companyBankAccountPR;
    }

    /**
     * This method compares PSP Account profile with AMS profile
     *
     * @return true if same, false otherwise
     */
    private void validateAMSUpdate(SMSMigrationStatus smsMigrationStatus) {

        boolean mSmsMigrationStatus = false;

        //if TRON company, then smsMigrationStatus=null
        if (nonNull(smsMigrationStatus)) {
            mSmsMigrationStatus = true;
        }

        String logPrefix = "job=AccountServiceSyncCore, Action=validateAMSUpdate, realmId=" + realmId + " , MigrationStatus=" + mSmsMigrationStatus + ", Status=";

        try {
            logger.info(logPrefix + "Start");
            AMSValidationProcess amsValidationProcess = new AMSValidationProcess(mDomainCompany);
            amsValidationProcess.setMigrationStatus(mSmsMigrationStatus);
            amsValidationProcess.setBeforeSyncValidation(false);
            ProcessResult<Boolean> pr = amsValidationProcess.process();
            boolean validationResult = pr.getResult();
            logger.info(logPrefix + "Complete, validationResult=" + validationResult);
        } catch (Exception e) {
            logger.error(logPrefix + "Error", e);
        }
    }
}
