package com.intuit.sbd.payroll.psp.payments;


import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.payroll.iam.AuthorizationManager;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class PaymentServiceAuthorizationManager {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(PaymentServiceAuthorizationManager.class);
    private AuthorizationManager authorizationManager;
    private OfflineTicketClient offlineTicketClient;

    public PaymentServiceAuthorizationManager(AuthorizationManager authorizationManager, OfflineTicketClient offlineTicketClient) {
        this.authorizationManager = authorizationManager;
        this.offlineTicketClient = offlineTicketClient;
    }

    public void setAuthorizationContext() {
        setAuthorizationContext(null);
    }

    public void setAuthorizationContext(String targetRealmId) {
        logger.info("Setting authorization context to call Payment Services");
        logger.info("Setting Authn ID2 context");
        String offlineTicket = getAuthnOfflineTicket(targetRealmId);
        RequestAttributesUtils.setAttribute(ContextConstants.AUTHN_CONTEXT, offlineTicket);
    }

    public void setValidationServiceAuthorizationContext() {
        logger.info("Setting authorization context to call Validation Services");
        logger.info("Setting Authn ID2 context");
        String offlineTicket = getAuthnOfflineTicket(null);
        RequestAttributesUtils.setAttribute(ContextConstants.AUTHN_CONTEXT, offlineTicket);
    }

    public void removeAuthorizationContext() {
        RequestAttributesUtils.removeAttribute(ContextConstants.AUTHN_CONTEXT);
    }

    public void removeBrowserAuthorizationContext() {
        RequestAttributesUtils.removeAttribute(ContextConstants.USER_BROWSER_AUTHORIZATION_CONTEXT);
        RequestAttributesUtils.removeAttribute(ContextConstants.AUTHN_CONTEXT);
    }

    private String getAuthnOfflineTicket(String targetRealmId) {
        String offlineTicket = null;
        if(!StringUtils.isEmpty(targetRealmId)) {
            logger.info("Offline ticket for RealmId = {}" + targetRealmId);
            offlineTicket = this.offlineTicketClient.getOfflineTicket(targetRealmId);
        }
        else {
            offlineTicket = this.offlineTicketClient.getOfflineTicket();
        }
        if(offlineTicket == null || offlineTicket.isEmpty()) {
            throw new PaymentServiceException(String.format("Unable to set Payment Services Authorization Context ID2 for RealmId=%s", targetRealmId));
        }
        return offlineTicket;
    }

}
