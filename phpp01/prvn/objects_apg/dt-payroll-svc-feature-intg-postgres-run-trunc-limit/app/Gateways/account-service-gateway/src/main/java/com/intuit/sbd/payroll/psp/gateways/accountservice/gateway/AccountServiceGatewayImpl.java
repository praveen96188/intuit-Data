package com.intuit.sbd.payroll.psp.gateways.accountservice.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intuit.money.account.model.ProfileMigrationRequest;
import com.intuit.payments.cdm.v2.client.BankAccount;
import com.intuit.payments.cdm.v2.client.PaymentsAccount;
import com.intuit.payments.cdm.v2.client.RiskProfile;
import com.intuit.payments.cdm.v2.client.RiskProfileAttribute;
import com.intuit.payments.cdm.v2.client.enums.RiskProfileAttributesEnum;
import com.intuit.platform.integration.ius.common.types.IntuitContext;
import com.intuit.sbd.payroll.psp.domain.SMSMigrationStatus;
import com.intuit.sbd.payroll.psp.gateways.accountservice.model.RiskLimits;
import com.intuit.sbd.payroll.psp.payments.PaymentServiceAuthorizationManager;
import com.intuit.sbg.psp.accountservices.AccountServicesClient;
import com.intuit.sbg.psp.accountservices.AccountServicesException;
import com.intuit.sbg.psp.accountservices.AccountServicesProfileMigrationResponseModel;
import com.intuit.sbg.psp.accountservices.v4.AccountServicesV4Client;
import com.intuit.sbg.psp.webserviceclient.Exception.RetryableException;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.sbg.psp.webserviceclient.v4.client.V4ClientException;
import com.intuit.v4.moneymovement.profile.MoneyAccount;
import com.intuit.v4.payments.definitions.PaymentsBankAccountType;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.intuit.payments.cdm.v2.client.enums.EntitlementEnum.MONEYOUT;
import static com.intuit.payments.cdm.v2.client.enums.RiskProfileAttributesEnum.OWNER_LIMIT;
import static com.intuit.payments.cdm.v2.client.enums.RiskProfileAttributesEnum.PAYEE_LIMIT;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Service
@Slf4j
public class AccountServiceGatewayImpl implements AccountServiceGateway {

    private PaymentServiceAuthorizationManager authorizationManager;
    private AccountServicesClient accountServicesClient;
    private AccountServicesV4Client accountServicesV4Client;

    public AccountServiceGatewayImpl(AccountServicesClient accountServicesClient,AccountServicesV4Client accountServicesV4Client, PaymentServiceAuthorizationManager paymentServiceAuthorizationManager) {
        this.accountServicesClient = accountServicesClient;
        this.authorizationManager = paymentServiceAuthorizationManager;
        this.accountServicesV4Client = accountServicesV4Client;
    }

    @Override
    public PaymentsAccount getPaymentsAccount(String realmId) {
        PaymentsAccount paymentsAccount=null;

        try {
            authorizationManager.setAuthorizationContext(realmId);
            paymentsAccount = accountServicesClient.getPaymentsAccount(realmId);

            if (isNull(paymentsAccount)) {
                log.info("Couldn't obtain the Payments Account for RealmId={}", realmId);
                return null;
            }
            log.info("Payments Account found for RealmId={}", realmId);
        } catch (Exception accountServicesException) {
           throw accountServicesException;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
        return paymentsAccount;

    }

    @Override
    public void deletePaymentsAccount(String realmId) {
        try {
            authorizationManager.setAuthorizationContext(realmId);
            accountServicesClient.deletePaymentsAccount(realmId);
            log.info("Payments Account deleted for RealmId={}", realmId);
        } catch (Exception accountServicesException) {
           throw accountServicesException;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
    }

    @Override
    public PaymentsAccount updatePaymentsAccount(String realmId, PaymentsAccount paymentsAccount) {
        PaymentsAccount result = null;
        try {
            authorizationManager.setAuthorizationContext(realmId);
            log.info("Updating PaymentsAccount for RealmId={}", realmId);
            result = accountServicesClient.updatePaymentsAccount(realmId,paymentsAccount);
            log.info("Updated PaymentsAccount for RealmId={}", realmId);
        } catch (Exception e) {
            log.error("Error updating PaymentsAccount for RealmId={}", realmId,e);
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
        return result;
    }

    @Override
    public MoneyAccount updateBankAccount(String realmId, PaymentsBankAccountType bankAccount, boolean isVerified) throws V4ClientException {
        MoneyAccount moneyAccount=null;
        try {
            authorizationManager.setAuthorizationContext(realmId);
            log.info("Triggering V4 Call to update BankAccount in AccountService for the RealmID={}", realmId);
            moneyAccount = accountServicesV4Client.updateMoneyOutBankInfo(realmId,bankAccount,isVerified);

             if(moneyAccount == null){
                 log.info("Error updating bank account in AccountServices for the RealmID={}", realmId);
                throw new V4ClientException("Error updating the bank");
             }
        } finally {
            authorizationManager.removeAuthorizationContext();
        }

        return moneyAccount;
    }

    @Override
    public List<BankAccount> getBankAccounts(String realmId) {
        List<BankAccount> result = null;
        try {
            authorizationManager.setAuthorizationContext(realmId);
            log.info("Fetching BankAccount for RealmId={}", realmId);
            result = accountServicesClient.getBankAccounts(realmId);
            log.info("Fetching BankAccount for RealmId={}", realmId);
        } catch (Exception e) {
            log.error("Error fetching BankAccount for RealmId={}", realmId,e);
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
        return result;
    }

    @Override
    public AccountServicesProfileMigrationResponseModel migratePSPAccount(ProfileMigrationRequest profileMigrationRequest, String realmId, String tid) throws AccountServicesException,JsonProcessingException  {
        SMSMigrationStatus status = null;
        String logPrefix = "job=PSPtoSMSMigration, Action=migratePSPAccount, Status={}, RealmId={}, tid={}";
        try {

            log.info(logPrefix, "Start", realmId, tid);
            setIntuitContextTid(tid);
            authorizationManager.setAuthorizationContext(realmId);
            AccountServicesProfileMigrationResponseModel profileMigrationResponse = accountServicesClient.migratePSPAccount(realmId, profileMigrationRequest);
            log.info(logPrefix, "Complete", realmId, tid);
            return profileMigrationResponse;
        } catch (AccountServicesException | JsonProcessingException | HttpClientErrorException | CallNotPermittedException e) {
            log.error(logPrefix, "Error", realmId, tid, e);
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
            removeIntuitContextTid();
        }
    }

    @Override
    public RiskLimits getRiskProfileLimits(String realmId, String tid) throws JsonProcessingException, AccountServicesException, RetryableException {
        try {
            log.info("Action=getRiskProfileLimits, Status=started, RealmId={}", realmId);
            setIntuitContextTid(tid);
            authorizationManager.setAuthorizationContext(realmId);
            RiskLimits riskLimits = getRiskProfileAttributes(accountServicesClient.getRiskProfile(realmId), realmId);
            log.info("Action=getRiskProfileLimits, Status=completed, RealmId={}", realmId);
            return riskLimits;
        } catch (AccountServicesException | RetryableException | JsonProcessingException | CallNotPermittedException |
                 HttpClientErrorException e) {
            log.error("Action=getRiskProfileLimits, Status=error, RealmId={}", realmId, e);
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
            removeIntuitContextTid();
        }
    }

    @Override
    public int updateRiskProfileLimits(String realmId, RiskLimits riskLimits, String tid) throws JsonProcessingException, AccountServicesException, RetryableException {
        try {
            log.info("Action=updateRiskProfileLimits, Status=started, RealmId={}", realmId);
            setIntuitContextTid(tid);
            authorizationManager.setAuthorizationContext(realmId);
            int status = accountServicesClient.updateRiskProfile(realmId, getRiskProfile(riskLimits));
            log.info("Action=updateRiskProfileLimits, Status=completed, RealmId={}, Status={}", realmId, status);
            return status;
        } catch (AccountServicesException | RetryableException | JsonProcessingException | HttpClientErrorException | CallNotPermittedException e) {
            log.error("Action=updateRiskProfileLimits, Status=error, RealmId={}", realmId, e);
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
            removeIntuitContextTid();
        }
    }

    private void setIntuitContextTid(String tid) {
        IntuitContext intuitContext = RequestAttributesUtils.getAttribute(ContextConstants.INTUIT_CONTEXT, IntuitContext.class);
        if (isNull(intuitContext)) {
            intuitContext = new IntuitContext();
            RequestAttributesUtils.setAttribute(ContextConstants.INTUIT_CONTEXT, intuitContext);
        }
        intuitContext.setTransactionId(tid);
    }

    private void removeIntuitContextTid() {
        IntuitContext intuitContext = RequestAttributesUtils.getAttribute(ContextConstants.INTUIT_CONTEXT, IntuitContext.class);
        if (Objects.nonNull(intuitContext)) {
            intuitContext.setTransactionId(StringUtils.EMPTY);
        }
    }

    private RiskLimits getRiskProfileAttributes(RiskProfile riskProfile, String realmId) {
        if (isNull(riskProfile)) {
            throw new IllegalArgumentException(format("Action=getRiskProfileAttributes, Desc=Risk profile cannot be null, %s", realmId));
        }
        RiskLimits riskLimits = new RiskLimits();
        for (RiskProfileAttribute a : riskProfile.getRiskProfileAttributes()) {
            if (nonNull(a) && (MONEYOUT.value().equals(a.getEntitlementName()))) {
                String name = a.getName();
                if (OWNER_LIMIT.value().equals(name)) {
                    riskLimits.setOwnerLimit(a.getValue());
                } else if (PAYEE_LIMIT.value().equals(name)) {
                    riskLimits.setPayeeLimit(a.getValue());
                }
            }
        }
        return riskLimits;
    }

    private RiskProfileAttribute getRiskProfileAttribute(RiskProfileAttributesEnum riskProfileAttributesEnum, String value) {
        RiskProfileAttribute riskProfileAttribute = new RiskProfileAttribute();
        riskProfileAttribute.setEntitlementName(MONEYOUT.value());
        riskProfileAttribute.setName(riskProfileAttributesEnum.value());
        riskProfileAttribute.setValue(value);
        return riskProfileAttribute;
    }

    private RiskProfile getRiskProfile(RiskLimits riskLimits) {
        RiskProfile riskProfile = new RiskProfile();
        List<RiskProfileAttribute> riskProAttrList = new ArrayList<>(2);
        riskProAttrList.add(getRiskProfileAttribute(OWNER_LIMIT, riskLimits.getOwnerLimit()));
        riskProAttrList.add(getRiskProfileAttribute(PAYEE_LIMIT, riskLimits.getPayeeLimit()));
        riskProfile.setRiskProfileAttributes(riskProAttrList);
        return riskProfile;
    }
}
