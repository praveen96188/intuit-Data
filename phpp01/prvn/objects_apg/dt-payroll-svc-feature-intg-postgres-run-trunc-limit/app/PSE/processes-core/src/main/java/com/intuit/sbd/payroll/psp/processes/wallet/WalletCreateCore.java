package com.intuit.sbd.payroll.psp.processes.wallet;

import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.walletv4service.WalletV4ServiceManager;
import com.intuit.sbd.payroll.psp.gateways.walletv4service.model.ParentType;
import com.intuit.sbd.payroll.psp.gateways.walletv4service.model.WalletBankAccountBuilder;
import com.intuit.sbd.payroll.psp.processes.IProcess;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.v4.moneymovement.wallet.WalletBankAccount;
import com.intuit.v4.payments.definitions.PaymentsBankAccountTypeEnum;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Takes bank account and realmId
 * 1. Creates wallet
 * Return Bank Account object
 * To check success/failure use .isSuccess
 */
public class WalletCreateCore extends Process implements IProcess {
    private static final Logger logger = LoggerFactory.getLogger(WalletCreateCore.class);

    private Company company;
    private String realmId;
    private String parentId;
    private BankAccount bankAccount;
    private WalletBankAccount walletBankAccount;
    private String oldWalletId;
    private ParentType parentType;
    private EventTypeCode successWalletEvent;
    private EventTypeCode failureWalletEvent;
    private EventDetailTypeCode parentIdEventType;
    private String entityBankAccountSeq;

    private WalletV4ServiceManager walletV4ServiceManager;

    public WalletCreateCore(Company company, String parentId, String fullName, String phone, BankAccount bankAccount,
                            PaymentsBankAccountTypeEnum paymentsBankAccountTypeEnum, ParentType parentType,
                            EventTypeCode successWalletEvent, EventTypeCode failureWalletEvent,
                            EventDetailTypeCode parentIdEventType, String entityBankAccountSeq, Boolean defaultAccount) {
        this.walletV4ServiceManager = PayrollApplicationBeanFactory.getBean(WalletV4ServiceManager.class);
        this.company = company;
        this.realmId = company.getIAMRealmId();
        this.parentId = parentId;
        this.bankAccount = bankAccount;
        this.parentType = parentType;
        this.successWalletEvent = successWalletEvent;
        this.failureWalletEvent = failureWalletEvent;
        this.parentIdEventType = parentIdEventType;
        this.entityBankAccountSeq = entityBankAccountSeq;

        this.walletBankAccount = new WalletBankAccountBuilder()
                .setDefault(defaultAccount)
                .setAccountType(paymentsBankAccountTypeEnum)
                .setBankCode(bankAccount.getRoutingNumber())
                .setAccountNumber(bankAccount.getAccountNumber())
                .setPhone(phone)
                .setName(fullName)
                .setParentId(parentId)
                .setParentType(String.valueOf(parentType))
                .build();
    }

    public WalletCreateCore(EmployeeBankAccount employeeBankAccount) {
        this(employeeBankAccount.getEmployee().getCompany(), employeeBankAccount.getEmployee().getId().toString(),
                employeeBankAccount.getEmployee().getFullName(), employeeBankAccount.getEmployee().getPhone(),
                employeeBankAccount.getBankAccount(), (employeeBankAccount.getBankAccount().getAccountTypeCd().in(BankAccountType.Checking) ?
                        PaymentsBankAccountTypeEnum.PERSONAL_CHECKING : PaymentsBankAccountTypeEnum.PERSONAL_SAVINGS),
                ParentType.employees, EventTypeCode.EmployeeBankAccountWalletSuccess,
                EventTypeCode.EmployeeBankAccountWalletFailure, EventDetailTypeCode.EmployeeId,
                employeeBankAccount.getId().toString(), employeeBankAccount.getAccountOrder() == 0 ? true : false);
        EmployeeBankAccount oldEBA = EmployeeBankAccount.findOldEmployeeBankAccount(employeeBankAccount.getEmployee(), employeeBankAccount.getAccountOrder());
        this.oldWalletId = Objects.nonNull(oldEBA) ? oldEBA.getBankAccount().getWalletId() : null;
    }

    public WalletCreateCore(PayeeBankAccount payeeBankAccount) {
        this(payeeBankAccount.getPayee().getCompany(), payeeBankAccount.getPayee().getId().toString(),
                payeeBankAccount.getPayee().getName(), payeeBankAccount.getPayee().getPhone(), payeeBankAccount.getBankAccount(),
                payeeBankAccount.getBankAccount().getAccountTypeCd().in(BankAccountType.Checking) ?
                        PaymentsBankAccountTypeEnum.BUSINESS_CHECKING : PaymentsBankAccountTypeEnum.BUSINESS_SAVINGS,
                ParentType.vendors, EventTypeCode.VendorBankAccountWalletSuccess, EventTypeCode.VendorBankAccountWalletFailure,
                EventDetailTypeCode.VendorId, payeeBankAccount.getId().toString(), true);
        PayeeBankAccount oldPBA = PayeeBankAccount.findOldPayeeBankAccount(payeeBankAccount.getPayee());
        this.oldWalletId = Objects.nonNull(oldPBA) ? oldPBA.getBankAccount().getWalletId() : null;
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        if(Objects.isNull(walletBankAccount)) {
            validationResult.getMessages()
                    .BadProcessArgument("BankAccount null or empty");
            return validationResult;
        }
        if(!walletBankAccount.isDefaultSet()) {
            validationResult.getMessages()
                    .BadProcessArgument("Default flag not set");
            return validationResult;
        }
        if(!walletBankAccount.isAccountTypeSet()) {
            validationResult.getMessages()
                    .BadProcessArgument("AccountType not set");
            return validationResult;
        }
        if(!walletBankAccount.isBankCodeSet()) {
            validationResult.getMessages()
                    .BadProcessArgument("BankCode not set");
            return validationResult;
        }
        if(!walletBankAccount.isAccountNumberSet()) {
            validationResult.getMessages()
                    .BadProcessArgument("AccountNumber not set");
            return validationResult;
        }
        if(!walletBankAccount.isNameSet()) {
            validationResult.getMessages()
                    .BadProcessArgument("Name not set");
            return validationResult;
        }
        if(!walletBankAccount.isParentIdSet()) {
            validationResult.getMessages()
                    .BadProcessArgument("ParentId not set");
            return validationResult;
        }
        if(!walletBankAccount.isParentTypeSet()) {
            validationResult.getMessages()
                    .BadProcessArgument("ParentType not set");
            return validationResult;
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        long startTime = System.currentTimeMillis();
        logger.info("WalletCreation=Attempted ParentId={} ParentType={} BASeq={} RealmId={}",
                parentId, parentType, entityBankAccountSeq, realmId);
        WalletBankAccount walletBankAccountResponse = walletV4ServiceManager.createWalletIdForWalletBA(walletBankAccount, realmId);
        long timeTaken = System.currentTimeMillis() - startTime;
        return handleWalletCreateCompanyEvent(walletBankAccountResponse, timeTaken);
    }

    private ProcessResult handleWalletCreateCompanyEvent(WalletBankAccount walletBankAccountResponse, long timeTaken) {
        ProcessResult processResult = new ProcessResult();

        String newWalletId =  Objects.nonNull(walletBankAccountResponse) ? walletBankAccountResponse.getId().getLocalId() : null;
        if(StringUtil.isNullOrEmpty(newWalletId)) {
            CompanyEvent.createBankAccountWalletEvent(company, failureWalletEvent, oldWalletId, newWalletId, parentIdEventType, parentId);
            logger.error("WalletCreation=Failed Action=CompanyEventWalletIdSet ParentId={} ParentType={} " +
                    "BASeq={} RealmId={} TimeTaken={}", parentId, parentType, entityBankAccountSeq, realmId, timeTaken);
            processResult.getMessages().BadProcessArgument("WalletId null or empty");
        } else {
            bankAccount.setWalletId(newWalletId);
            CompanyEvent.createBankAccountWalletEvent(company, successWalletEvent, oldWalletId, newWalletId, parentIdEventType, parentId);
            logger.info("WalletCreation=Success Action=CompanyEventWalletIdSet ParentId={} ParentType={} " +
                            "BASeq={} WalletId={} RealmId={} TimeTaken={}", parentId, parentType, entityBankAccountSeq,
                    newWalletId, realmId, timeTaken);
        }
        return processResult;
    }
}