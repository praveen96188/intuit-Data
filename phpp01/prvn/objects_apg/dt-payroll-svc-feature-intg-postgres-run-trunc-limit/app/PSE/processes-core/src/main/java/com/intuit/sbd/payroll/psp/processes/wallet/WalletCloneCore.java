package com.intuit.sbd.payroll.psp.processes.wallet;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.walletv4service.WalletV4ServiceManager;
import com.intuit.sbd.payroll.psp.gateways.walletv4service.model.ParentType;
import com.intuit.sbd.payroll.psp.gateways.walletv4service.model.WalletV4CloneModel;
import com.intuit.sbd.payroll.psp.processes.IProcess;
import com.intuit.sbd.payroll.psp.processes.Process;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.wallet.clone.WalletCloneModel;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.v4.moneymovement.wallet.WalletBankAccount;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Takes old and new realmId
 *  Clones wallet for active bank accounts of all active employees and vendors with non-null/empty walletId
 */
public class WalletCloneCore extends Process implements IProcess {

    private static final Logger logger = LoggerFactory.getLogger(WalletCloneCore.class);
    private Company company;
    private String oldRealmId;
    private String newRealmId;
    private List<WalletCloneModel> walletCloneModels = new ArrayList<>();
    private WalletV4ServiceManager walletV4ServiceManager;

    public WalletCloneCore(@NotNull Company company, String newRealmId) {
        this.company = company;
        this.oldRealmId = company.getIAMRealmId();
        this.newRealmId = newRealmId;
        this.walletV4ServiceManager = PayrollApplicationBeanFactory.getBean(WalletV4ServiceManager.class);
        addEmployeesForCloneInWalletModelList();
        addPayeesForCloneInWalletModelList();
    }

    @Override
    public ProcessResult validate() {
        ProcessResult validationResult = new ProcessResult();
        logger.info("Wallet Clone validation OldRealm={} NewRealm={}", oldRealmId, newRealmId);
        if (StringUtil.isNullOrEmpty(oldRealmId)) {
            validationResult.getMessages()
                    .BadProcessArgument("Old RealmId for company null or empty");
            return validationResult;
        }
        if (StringUtil.isNullOrEmpty(newRealmId)) {
            validationResult.getMessages()
                    .BadProcessArgument("New RealmId for company null or empty");
            return validationResult;
        }
        return validationResult;
    }

    @Override
    public ProcessResult process() {
        ProcessResult processResult = new ProcessResult();

        long startTime = System.currentTimeMillis();
        int bankAccountsInCompany = 0;
        Map<String, CompletableFuture> completableFutures = new HashMap<>();

        for(WalletCloneModel walletCloneModel : walletCloneModels) {
            List<WalletV4CloneModel> walletV4CloneModels = new ArrayList<>();

            logger.info("WalletClone=Attempted ParentId={} BankAccountSize={} ParentType={} OldRealmId={} NewRealmId={}",
                    walletCloneModel.getParentId(), walletCloneModel.getBankAccounts().size(), walletCloneModel.getParentType(),
                    oldRealmId, newRealmId);

            for(BankAccount bankAccount : walletCloneModel.getBankAccounts()) {
                String oldWalletId = bankAccount.getWalletId();
                WalletV4CloneModel walletV4CloneModel = new WalletV4CloneModel(Collections.singletonList(oldWalletId),
                        walletCloneModel.getParentId(), walletCloneModel.getParentType().toString(), oldRealmId, newRealmId);
                walletV4CloneModels.add(walletV4CloneModel);
            }
            bankAccountsInCompany += walletCloneModel.getBankAccounts().size();
            CompletableFuture<Map<String, WalletBankAccount>> completableFuture = walletV4ServiceManager.cloneWalletIdForBAs(
                    walletV4CloneModels, oldRealmId, newRealmId);
            completableFutures.put(walletCloneModel.getParentId(), completableFuture);
        }

        if(completableFutures.isEmpty()) {
            processResult.setResult(true);
            return processResult;
        }

        Boolean isSuccess = processCompletableFutures(walletCloneModels, completableFutures);
        logger.info("WalletClone=Done Action=CompanyProcessed Result={} ParentSize={} BankAccountSize={}" +
                        " OldRealmId={} NewRealmId={} TimeTaken={}", isSuccess, walletCloneModels.size(), bankAccountsInCompany,
                oldRealmId, newRealmId, (System.currentTimeMillis() - startTime));
        processResult.setResult(isSuccess);
        return processResult;
    }

    private Boolean processCompletableFutures(List<WalletCloneModel> walletCloneModels, Map<String, CompletableFuture> completableFutures) {
        Boolean isSuccess = true;
        for( WalletCloneModel walletCloneModel : walletCloneModels) {
            Map<String, WalletBankAccount> oldWalletNewWalletMap = null;
            try {
                oldWalletNewWalletMap = (Map<String, WalletBankAccount>) completableFutures.get(walletCloneModel.getParentId()).get();
            } catch (Exception e) {
                walletCloneFailures(walletCloneModel, "FutureObjectFailedToGet");
                isSuccess = false;
                continue;
            }
            if(Objects.isNull(oldWalletNewWalletMap) || oldWalletNewWalletMap.isEmpty()){
                walletCloneFailures(walletCloneModel, "NullOrEmptyObjectForOldNewWalletMap");
                isSuccess = false;
                continue;
            } else {
                isSuccess = processOldNewWalletMapPerParent(walletCloneModel, oldWalletNewWalletMap) && isSuccess;
            }
        }
        return isSuccess;
    }

    Boolean processOldNewWalletMapPerParent(WalletCloneModel walletCloneModel, Map<String, WalletBankAccount> oldWalletNewWalletMap) {
        Boolean isSuccess = true;
        for (BankAccount bankAccount : walletCloneModel.getBankAccounts()) {
            String oldWalletId = bankAccount.getWalletId();
            WalletBankAccount walletBankAccount = oldWalletNewWalletMap.get(oldWalletId);
            String newWalletId = (Objects.nonNull(walletBankAccount) && walletBankAccount.isIdSet()) ? walletBankAccount.getId().getLocalId() : null;

            if (StringUtil.isNullOrEmpty(newWalletId)) {
                walletCloneFailure(walletCloneModel, bankAccount, "CompanyEventWalletIdSet");
                isSuccess = false;
            } else {
                bankAccount.setWalletId(newWalletId);
                CompanyEvent.createCloneWalletOnRealmChangeEvent(company, walletCloneModel.getSuccessEventTypeCode(), newWalletId,
                        oldWalletId, newRealmId, oldRealmId, walletCloneModel.getParentTypeEvent(), walletCloneModel.getParentId());
                logger.info("WalletClone=Success Action=CompanyEventWalletIdSet ParentId={} ParentType={}" +
                                " OldWalletId={} NewWalletId={} OldRealmId={} NewRealmId={}",
                        walletCloneModel.getParentId(), walletCloneModel.getParentType(), oldWalletId, newWalletId, oldRealmId, newRealmId);
                Application.save(bankAccount);
            }
        }
        return isSuccess;
    }

    void walletCloneFailures(WalletCloneModel walletCloneModel, String action) {
        for (BankAccount bankAccount : walletCloneModel.getBankAccounts()) {
            walletCloneFailure(walletCloneModel, bankAccount, action);
        }
    }

    private void walletCloneFailure(WalletCloneModel walletCloneModel, BankAccount bankAccount, String action) {
        String oldWalletId = bankAccount.getWalletId();
        CompanyEvent.createCloneWalletOnRealmChangeEvent(company, walletCloneModel.getFailedEventTypeCode(), null, oldWalletId,
                newRealmId, oldRealmId, walletCloneModel.getParentTypeEvent(), walletCloneModel.getParentId());
        logger.error("WalletClone=Failed Action={} ParentId={} ParentType={} OldWalletId={} OldRealmId={} NewRealmId={} ",
                action, walletCloneModel.getParentId(), walletCloneModel.getParentType(), oldWalletId, oldRealmId, newRealmId);
        bankAccount.setWalletId(null);
        Application.save(bankAccount);
    }

    private void addEmployeesForCloneInWalletModelList() {
        List<Employee> employees = Employee.findActiveEmployeesByCompanyPSID(company.getSourceCompanyId());
        for (Employee employee : employees) {
            List<EmployeeBankAccount> employeeBankAccounts = EmployeeBankAccount.getActiveBankAccountsForWalletIdCriteria(employee, false);
            if (employeeBankAccounts.isEmpty()) {
                continue;
            }
            List<BankAccount> bankAccounts = new ArrayList<>();
            for (EmployeeBankAccount employeeBankAccount : employeeBankAccounts) {
                bankAccounts.add(employeeBankAccount.getBankAccount());
            }
            WalletCloneModel walletCloneModel = new WalletCloneModel(bankAccounts, employee.getId().toString(), ParentType.employees);
            this.walletCloneModels.add(walletCloneModel);
        }
    }

    private void addPayeesForCloneInWalletModelList() {
        List<Payee> payees = new ArrayList<>(Payee.findPayees(company));
        for (Payee payee : payees) {
            List<PayeeBankAccount> payeeBankAccounts = PayeeBankAccount.getActiveBankAccountsForWalletIdCriteria(payee, false);
            if (payeeBankAccounts.isEmpty()) {
                continue;
            }
            List<BankAccount> bankAccounts = new ArrayList<>();
            for (PayeeBankAccount payeeBankAccount : payeeBankAccounts) {
                bankAccounts.add(payeeBankAccount.getBankAccount());
            }
            WalletCloneModel walletCloneModel = new WalletCloneModel(bankAccounts, payee.getId().toString(), ParentType.vendors);
            this.walletCloneModels.add(walletCloneModel);
        }
    }
}
