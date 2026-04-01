package com.intuit.sbd.payroll.psp.gateways.walletv4service.model;

import com.intuit.v4.moneymovement.wallet.WalletBankAccount;
import com.intuit.v4.payments.definitions.PaymentsBankAccountTypeEnum;

public class WalletBankAccountBuilder {

    private WalletBankAccount walletBankAccount;

    public WalletBankAccountBuilder() {
        this.walletBankAccount = new WalletBankAccount();
    }

    public WalletBankAccountBuilder setDefault(Boolean defaultAccount) {
        this.walletBankAccount.setDefault(defaultAccount);
        return this;
    }

    public WalletBankAccountBuilder setAccountType(PaymentsBankAccountTypeEnum paymentsBankAccountTypeEnum) {
        this.walletBankAccount.setAccountType(paymentsBankAccountTypeEnum);
        return this;
    }

    public WalletBankAccountBuilder setBankCode(String bankCode) {
        this.walletBankAccount.setBankCode(bankCode);
        return this;
    }

    public WalletBankAccountBuilder setAccountNumber(String accountNumber) {
        this.walletBankAccount.setAccountNumber(accountNumber);
        return this;
    }

    public WalletBankAccountBuilder setPhone(String phone) {
        this.walletBankAccount.setPhone(phone);
        return this;
    }

    public WalletBankAccountBuilder setName(String name) {
        this.walletBankAccount.setName(name);
        return this;
    }

    public WalletBankAccountBuilder setParentId(String parentId) {
        this.walletBankAccount.setParentId(parentId);
        return this;
    }

    public WalletBankAccountBuilder setParentType(String parentType) {
        this.walletBankAccount.setParentType(parentType);
        return this;
    }

    public WalletBankAccount build() {
        return this.walletBankAccount;
    }
}
