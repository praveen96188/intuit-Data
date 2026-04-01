package com.intuit.sbd.payroll.psp.gateways.walletv4service;

import com.intuit.sbd.payroll.psp.gateways.walletv4service.gateway.WalletV4ServiceGateway;
import com.intuit.sbd.payroll.psp.gateways.walletv4service.model.WalletV4CloneModel;
import com.intuit.sbg.psp.walletservice.v4.types.WalletConstants;
import com.intuit.v4.moneymovement.wallet.Wallet;
import com.intuit.v4.moneymovement.wallet.WalletBankAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class WalletV4ServiceManager {

    private WalletV4ServiceGateway walletV4ServiceGateway;
    private WalletV4Validator walletV4Validator;

    @Autowired
    public WalletV4ServiceManager(WalletV4ServiceGateway walletV4ServiceGateway, WalletV4Validator walletV4Validator) {
        this.walletV4ServiceGateway = walletV4ServiceGateway;
        this.walletV4Validator = walletV4Validator;
    }

    public WalletBankAccount createWalletIdForWalletBA(WalletBankAccount walletBankAccountRequest, String realmId) {
        walletBankAccountRequest = walletV4Validator.sanitiseFieldsInWalletBankAccount(walletBankAccountRequest);
        WalletBankAccount walletBankAccountResponse = walletV4ServiceGateway.createV4Wallet(walletBankAccountRequest, realmId);
        if(Objects.nonNull(walletBankAccountResponse)){
            walletV4Validator.validateWalletCreateResponse(walletBankAccountRequest, walletBankAccountResponse, realmId);
        }
        return walletBankAccountResponse;
    }

    @Async("walletCloneThreadPoolExecutor")
    public CompletableFuture<Map<String, WalletBankAccount>> cloneWalletIdForBAs(List<WalletV4CloneModel> walletV4CloneModels, String oldRealmId, String newRealmId) {
        Map<String, WalletBankAccount> oldWalletNewWalletMap = new HashMap<>();
        for (WalletV4CloneModel walletCloneModel : walletV4CloneModels) {
            String oldWalletId = walletCloneModel.getWalletIds().get(0);

            Wallet wallet = walletV4ServiceGateway.cloneV4Wallet(createCloneInputPayload(walletCloneModel), walletCloneModel.getNewRealmId());
            WalletBankAccount walletBankAccount = null;
            if(Objects.nonNull(wallet) && !wallet.getBankAccounts().isEmpty()) {
                walletV4Validator.validateWalletCloneResponse(walletCloneModel, wallet.getBankAccounts(0));
                walletBankAccount = wallet.getBankAccounts(0);
            }
            oldWalletNewWalletMap.put(oldWalletId, walletBankAccount);
        }
        return CompletableFuture.completedFuture(oldWalletNewWalletMap);
    }

    private Wallet.CloneInput createCloneInputPayload(WalletV4CloneModel walletCloneModel) {

        Wallet.Ownership source = createOwnership(walletCloneModel.getOldRealmId(), walletCloneModel.getParentId(), walletCloneModel.getParentType());
        Wallet.Ownership target = createOwnership(walletCloneModel.getNewRealmId(), walletCloneModel.getParentId(), walletCloneModel.getParentType());
        Wallet.CloneInput cloneInput = new Wallet.CloneInput();
        cloneInput.set(WalletConstants.WALLET_OP_KEY, WalletConstants.CLONE_WALLET_OP_VALUE);
        cloneInput.setWalletIds(walletCloneModel.getWalletIds());
        cloneInput.setSource(source);
        cloneInput.setTarget(target);
        return cloneInput;
    }

    private Wallet.Ownership createOwnership(String companyId, String parentId, String parentType){
        Wallet.Ownership ownership = new Wallet.Ownership();
        ownership.setCompanyId(companyId);
        ownership.setParentId(parentId);
        ownership.setParentType(parentType);
        return ownership;
    }
}
