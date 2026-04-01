package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.gateways.walletservice.gateway.WalletServiceGateway;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

public class BankAccountDeTokenizer {

    private static SpcfLogger logger = PayrollServices.getLogger(BankAccountDeTokenizer.class);

    private static final String TOKEN = "token:";

    private WalletServiceGateway walletServiceGateway;

    public BankAccountDeTokenizer() {
        this.walletServiceGateway = PayrollApplicationBeanFactory.getBean(WalletServiceGateway.class);
    }

    public boolean isBanToken(String banToken) {
        return StringUtils.containsIgnoreCase(banToken, TOKEN);
    }

    public String getDetokenizedBankAccount(String accountNumber) {
        if(!isBanToken(accountNumber)) {
            logger.warn("Received non tokenized bank account for de tokenization, banToken="+accountNumber);
            return accountNumber;
        }

        String tokenizedBankAccount = StringUtils.substringAfter(accountNumber, TOKEN);

        return  walletServiceGateway.getDetokenizeBankAccountString(tokenizedBankAccount);
    }

}
