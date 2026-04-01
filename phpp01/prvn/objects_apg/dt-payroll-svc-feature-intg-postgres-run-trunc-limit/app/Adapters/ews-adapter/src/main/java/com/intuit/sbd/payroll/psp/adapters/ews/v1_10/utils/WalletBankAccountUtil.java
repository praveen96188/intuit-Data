package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils;

import com.intuit.sbd.payroll.psp.api.PayrollServices;

import com.intuit.sbd.payroll.psp.gateways.walletv4service.gateway.WalletV4ServiceGateway;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.v4.moneymovement.wallet.WalletBankAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

@Slf4j
public class WalletBankAccountUtil {

    private WalletV4ServiceGateway walletV4ServiceGateway;

    private static final String WALLET_ID = "walletId:";

    public WalletBankAccountUtil() {
        this.walletV4ServiceGateway = PayrollApplicationBeanFactory.getBean(WalletV4ServiceGateway.class);
    }

    public boolean isValidWalletId(String walletId) {
        return StringUtils.containsIgnoreCase(walletId, WALLET_ID);
    }

    public WalletBankAccount getWalletBankAccount(String walletId, String realmId) {

        if(!isValidWalletId(walletId)) {
            log.warn("Received invalid walletId to get bank account information, walletId={}",walletId);
            return null;
        }
        String wId = StringUtils.substringAfter(walletId, WALLET_ID);
        log.info("realmId= {}, walletId= {}", realmId, wId);
        WalletBankAccount walletBankAccount = walletV4ServiceGateway.getV4WalletBankAccount(wId, realmId);
        if(Objects.nonNull(walletBankAccount)) {
            try {
                String accountNumber = walletBankAccount.getAccountNumber();
                String accountNumberLastFourDigit = accountNumber.length() > 4 ? accountNumber.substring(accountNumber.length() - 4) : "";
                String responseWalletLastFourDigit = wId.substring(wId.length() - 4);
                //Last 4 digit of Wallet and Account number match for all accounts should match
                //Exception - If account number is 4 digit, wallet
                if (accountNumber.length() > 4 && !accountNumberLastFourDigit.equals(responseWalletLastFourDigit)) {
                    log.info("Account Number mismatch with wallet");
                }
            } catch (Exception e) {
                log.info("Exception while validating wallet and account", e);
            }
        }

        return walletBankAccount;
    }
}
