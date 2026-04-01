package com.intuit.sbd.payroll.psp.gateways.walletv4service.gateway;

import com.intuit.v4.Query;
import com.intuit.v4.moneymovement.wallet.Wallet;
import com.intuit.v4.moneymovement.wallet.WalletBankAccount;
import org.springframework.stereotype.Service;

public interface WalletV4ServiceGateway {

     WalletBankAccount createV4Wallet(WalletBankAccount walletBankAccount, String realmId);

     Wallet cloneV4Wallet(Wallet.CloneInput cloneInput, String targetRealmId);

     WalletBankAccount getV4WalletBankAccount(String walletId, String realmId);
}
