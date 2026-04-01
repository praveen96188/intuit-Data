package com.intuit.sbd.payroll.psp.gateways.accountservice.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intuit.money.account.model.ProfileMigrationRequest;
import com.intuit.payments.cdm.v2.client.BankAccount;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.sbd.payroll.psp.gateways.accountservice.model.RiskLimits;
import com.intuit.sbg.psp.accountservices.AccountServicesException;
import com.intuit.sbg.psp.accountservices.AccountServicesProfileMigrationResponseModel;
import com.intuit.sbg.psp.webserviceclient.Exception.RetryableException;
import com.intuit.sbg.psp.webserviceclient.v4.client.V4ClientException;
import com.intuit.v4.moneymovement.profile.MoneyAccount;
import com.intuit.v4.payments.definitions.PaymentsBankAccountType;

import java.util.List;

public interface AccountServiceGateway {

    PaymentsAccount getPaymentsAccount(String realmId);

    void deletePaymentsAccount(String realmId);

    PaymentsAccount updatePaymentsAccount(String realmId, PaymentsAccount paymentsAccount);

    MoneyAccount updateBankAccount(String realmId, PaymentsBankAccountType bankAccount, boolean isVerified) throws V4ClientException;

    List<BankAccount> getBankAccounts(String realmId);

    AccountServicesProfileMigrationResponseModel migratePSPAccount(ProfileMigrationRequest profileMigrationRequest, String realmId, String tid) throws AccountServicesException,JsonProcessingException;

    RiskLimits getRiskProfileLimits(String realmId, String tid) throws JsonProcessingException, AccountServicesException, RetryableException;

    int updateRiskProfileLimits(String realmId, RiskLimits riskLimits, String tid) throws JsonProcessingException, AccountServicesException, RetryableException;
}
