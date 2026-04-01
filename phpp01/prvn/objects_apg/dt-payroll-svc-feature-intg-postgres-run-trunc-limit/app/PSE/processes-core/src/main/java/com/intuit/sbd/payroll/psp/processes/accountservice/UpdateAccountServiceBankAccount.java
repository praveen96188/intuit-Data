package com.intuit.sbd.payroll.psp.processes.accountservice;

import com.intuit.payments.cdm.v2.client.BankAccount;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyBankAccountDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.BankAccountStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGateway;
import com.intuit.sbd.payroll.psp.gateways.accountservice.gateway.AccountServiceGatewayImpl;
import com.intuit.sbd.payroll.psp.gateways.accountservice.translator.AccountServiceTranslator;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.v4.client.V4ClientException;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.v4.moneymovement.profile.BankInfo;
import com.intuit.v4.moneymovement.profile.MoneyAccount;
import com.intuit.v4.moneymovement.profile.definitions.EntitlementEnum;
import com.intuit.v4.payments.definitions.PaymentsBankAccountType;

import java.util.Objects;
import java.util.stream.Collectors;

public class UpdateAccountServiceBankAccount extends Process {

    private final String iamRealmId;
    private AccountServiceTranslator accountServiceTranslator;
    private AccountServiceGateway accountServiceGateway;
    private static final SpcfLogger logger = SpcfLogManager.getLogger(AccountServiceSyncCore.class);
    private CompanyBankAccountDTO mCompanyBankAccountDTO;
    private BankAccount bankAccount;
    private SourceSystemCode sourceSystemCd;
    private String sourceCompanyId;
    private Company mCompany;
    private BankInfo bankInfo;
    private PaymentsAccount paymentsAccount=null;


    public UpdateAccountServiceBankAccount(Company pCompany, CompanyBankAccountDTO pCompanyBankAccountDTO) {
        this.iamRealmId = pCompany.getIAMRealmId();
        this.mCompanyBankAccountDTO = pCompanyBankAccountDTO;
        this.accountServiceGateway = PayrollApplicationBeanFactory.getBean(AccountServiceGateway.class);
        this.accountServiceTranslator = PayrollApplicationBeanFactory.getBean(AccountServiceTranslator.class);
        this.mCompany = pCompany;


    }

    @Override
    public ProcessResult validate() {
        logger.info("Received and Validating BankAccount update Request for the RealmId=" +iamRealmId);
        ProcessResult validationResult = new ProcessResult();
        if (Objects.isNull(this.mCompanyBankAccountDTO)) {
            validationResult.getMessages()
                    .BadProcessArgument("CompanyBankAccountDTO in null");
            return validationResult;
        }
        if (Objects.isNull(this.iamRealmId)) {
            validationResult.getMessages()
                    .BadProcessArgument("IAMRealmId in null can't fetch details from account service");
            return validationResult;
        }

        try {
            paymentsAccount = accountServiceGateway.getPaymentsAccount(iamRealmId);
        } catch (Exception accountServicesException) {
            validationResult.getMessages().MoneymovementAccountDoesNotExistOnRealmId(EntityName.Company, iamRealmId);
            return validationResult;
        }

        if (Objects.isNull(paymentsAccount)) {
            validationResult.getMessages()
                    .ExceptionOccurred("PaymentsAccount in null in Account service for realm="
                            + this.iamRealmId);
            return validationResult;
        }
        this.bankAccount = accountServiceTranslator.findMoneyOutBankAccount(paymentsAccount);
        if (Objects.isNull(bankAccount)) {
            validationResult.getMessages()
                    .ExceptionOccurred("MoneyOutBankAccount in null in Account service for realm="
                            + this.iamRealmId);
            return validationResult;
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        logger.info("Processing BankAccount update Request to AccountService for the RealmId=" +iamRealmId);

        ProcessResult processResult = new ProcessResult();
        try {

            CompanyBankAccount companyBankAccount= CompanyBankAccount.findCompanyBankAccountByAccountNumber(mCompany, mCompanyBankAccountDTO.getBankAccountDTO().getAccountNumber(), mCompanyBankAccountDTO.getBankAccountDTO().getRoutingNumber(),
                    mCompanyBankAccountDTO.getBankAccountDTO().getAccountType());

            if(companyBankAccount.getBankAccount().getAccountTypeCd() == com.intuit.sbd.payroll.psp.domain.BankAccountType.Savings){
                processResult.setSuccess(false);
                processResult.getMessages().ExceptionOccurred("Currently not supporting saving Account type please select Checking Account");
                return processResult;
            }

            PaymentsBankAccountType request = accountServiceTranslator.getV4BankAccountType(bankAccount,companyBankAccount,mCompany);

            MoneyAccount moneyAccount = accountServiceGateway.updateBankAccount(this.iamRealmId, request,companyBankAccount.getStatusCd() == BankAccountStatus.Active);

            logger.info("BankAccount update Request to AccountService is completed for the RealmId=" +iamRealmId);
            logger.info("Verifying whether the BankAccount is successfully updated in AccountService for RealmId=" +iamRealmId);

            if(Objects.isNull(moneyAccount)){
                logger.info("BankAccount is Not updated in AccountService for RealmId=" +iamRealmId);

                throw new V4ClientException("Bank account is not updated in the Account Service");
            }
            logger.info("BankAccount is successfully updated in AccountService for RealmId=" +iamRealmId);

        } catch(V4ClientException ex){
            logger.error("Error in updating the BankAccount to Account services for realmId=" + this.iamRealmId, ex);
            processResult.getMessages().ExceptionOccurred("Bank Account updation failed for the reason="+ex.getMessage());

        }

        return processResult;
    }

}
