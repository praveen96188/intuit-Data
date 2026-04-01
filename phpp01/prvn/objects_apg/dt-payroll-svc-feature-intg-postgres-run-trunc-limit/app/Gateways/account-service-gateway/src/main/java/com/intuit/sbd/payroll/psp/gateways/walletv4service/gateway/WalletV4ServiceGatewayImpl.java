package com.intuit.sbd.payroll.psp.gateways.walletv4service.gateway;

import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.sbd.payroll.psp.gateways.walletv4service.WalletV4ServiceAuthorizationManager;
import com.intuit.sbg.psp.payroll.iam.context.AuthorizationContextBuilder;
import com.intuit.sbg.psp.walletservice.v4.WalletV4Client;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.v4.moneymovement.wallet.Wallet;
import com.intuit.v4.moneymovement.wallet.WalletBankAccount;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Objects;

@Service
@Slf4j
public class WalletV4ServiceGatewayImpl implements WalletV4ServiceGateway{

    private WalletV4ServiceAuthorizationManager walletV4ServiceAuthorizationManager;
    private WalletV4Client walletV4Client;

    @Autowired
    public WalletV4ServiceGatewayImpl(WalletV4ServiceAuthorizationManager walletV4ServiceAuthorizationManager,WalletV4Client walletV4Client)
    {
        this.walletV4ServiceAuthorizationManager = walletV4ServiceAuthorizationManager;
        this.walletV4Client = walletV4Client;
    }

    /* For the first time, when we make V4Client network call, it is observed that it takes ~20 seconds
       to construct network objects required to make V4Client calls. Attempting a warm-up call with dummy
       data to pre-load all the required objects before making actual call and prevent timeout
    */
    @PostConstruct
    private void warmUpCallForV4SDK() {
        String dummyAppIdSecret = "xxxxxxxxxxx";
        String dummyTokenUserIdRealId = "xxxxxxxxxxx";
        String dummyWalletId = "1111111111111111";
        String dummyRealmId = "9130358888795776";
        long startTime = 0;
        try{
            startTime = System.currentTimeMillis();
            // AuthorisationContext is required to pass the validation of Auth Header Interceptor in V4Client with dummy credentials
            AuthorizationContext authorizationContext = (new AuthorizationContextBuilder()).appId(dummyAppIdSecret)
                    .appSecret(dummyAppIdSecret).token(dummyTokenUserIdRealId)
                    .userId(dummyTokenUserIdRealId).realmId(dummyRealmId)
                    .build();

            RequestAttributesUtils.setAttribute(ContextConstants.AUTHORIZATION_CONTEXT, authorizationContext);
            walletV4Client.getWalletBankAccount(dummyWalletId, dummyRealmId);
        } catch(Exception e) {

        } finally {
            // Removing the dummy AuthorisationContext
            RequestAttributesUtils.removeAttribute(ContextConstants.AUTHORIZATION_CONTEXT);
            log.info("Warm Up call finished for v4 sdk. TimeTaken={}", System.currentTimeMillis() - startTime);
        }
    }

    @Override
    public WalletBankAccount createV4Wallet(WalletBankAccount walletBankAccount, String realmId) {
        try{
            walletV4ServiceAuthorizationManager.setAuthorizationContext(realmId);
            return walletV4Client.createWallet(walletBankAccount);
        } catch (Exception e) {
            log.error("Wallet Create Exception RealmId={}", realmId, e);
        } finally {
            walletV4ServiceAuthorizationManager.removeAuthorizationContext();
        }
        return null;
    }

    @Override
    public Wallet cloneV4Wallet(Wallet.CloneInput cloneInput, String targetRealmId) {
        try {
            walletV4ServiceAuthorizationManager.setAuthorizationContext(targetRealmId);
            return walletV4Client.cloneWallet(cloneInput);
        } catch (Exception e) {
            log.error("Wallet Clone Exception RealmId={}", cloneInput.getSource().getCompanyId(), e);
        } finally {
            walletV4ServiceAuthorizationManager.removeAuthorizationContext();
        }
        return null;
    }


    @Override
    public WalletBankAccount getV4WalletBankAccount(String walletId, String realmId) {
        try{
            walletV4ServiceAuthorizationManager.setAuthorizationContext(realmId);
            long startTime = System.currentTimeMillis();
            WalletBankAccount walletBankAccount =  walletV4Client.getWalletBankAccount(walletId, realmId);
            log.info("Get_Wallet_account_time_elapsed={}", System.currentTimeMillis() - startTime);
            if(Objects.isNull(walletBankAccount)) {
                log.error("Null Wallet Bank Account is returned");
            }
            return walletBankAccount;
        } catch (Exception e) {
            log.error("Wallet Bank Account Exception RealmId={}", realmId, e);
        } finally {
            walletV4ServiceAuthorizationManager.removeAuthorizationContext();
        }
        return null;
    }
}
