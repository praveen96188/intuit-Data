package com.intuit.sbd.payroll.psp.gateways.walletv4service;

import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.payroll.iam.AuthorizationManager;
import com.intuit.sbg.psp.walletservice.v4.WalletV4ServiceException;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class WalletV4ServiceAuthorizationManager {

    private AuthorizationManager authorizationManager;
    private OfflineTicketClient offlineTicketClient;

    @Autowired
    public WalletV4ServiceAuthorizationManager(AuthorizationManager authorizationManager, OfflineTicketClient offlineTicketClient) {
        this.authorizationManager = authorizationManager;
        this.offlineTicketClient = offlineTicketClient;
    }

    public void setAuthorizationContext() {
        setAuthorizationContext(null);
    }

    public void setAuthorizationContext(String targetRealmId) {
        log.info("Setting authorization context to call Wallet Services, RealmId={}", targetRealmId);
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_ID2_ENABLED_FOR_WALLETSERVICE, true)){
            log.info("Setting authorization ID2 context to call Wallet Services");
            String offlineTicket = null;
            if(!StringUtils.isEmpty(targetRealmId)) {
                offlineTicket = this.offlineTicketClient.getOfflineTicket(targetRealmId);
            }
            else {
                offlineTicket = this.offlineTicketClient.getOfflineTicket();
            }
            if(offlineTicket.isEmpty()) {
                throw new WalletV4ServiceException(String.format("Unable to set Wallet Services Authorization Context ID2 for RealmId=%s", targetRealmId));
            }
            RequestAttributesUtils.setAttribute(ContextConstants.AUTHN_CONTEXT, offlineTicket);
        } else {
            AuthorizationContext walletV4ServiceAuthorizationContext = RequestAttributesUtils.getAttribute(ContextConstants.USER_AUTHORIZATION_CONTEXT, AuthorizationContext.class);
            if (Objects.nonNull(walletV4ServiceAuthorizationContext)) {
                return;
            }

            // Generate offline ticket
            walletV4ServiceAuthorizationContext = authorizationManager.getAuthorizationContext(WalletV4ServiceGatewayConfig.WALLET_V4_OFFLINE_TICKET_SERVICES, targetRealmId);
            if (Objects.isNull(walletV4ServiceAuthorizationContext)) {
                throw new WalletV4ServiceException(String.format("Unable to set Wallet Services Authorization Context for RealmId=%s", targetRealmId));
            }
            RequestAttributesUtils.setAttribute(ContextConstants.AUTHORIZATION_CONTEXT, walletV4ServiceAuthorizationContext);
        }
    }

    public void removeAuthorizationContext() {
        RequestAttributesUtils.removeAttribute(ContextConstants.AUTHORIZATION_CONTEXT);
        RequestAttributesUtils.removeAttribute(ContextConstants.AUTHN_CONTEXT);
    }
}
