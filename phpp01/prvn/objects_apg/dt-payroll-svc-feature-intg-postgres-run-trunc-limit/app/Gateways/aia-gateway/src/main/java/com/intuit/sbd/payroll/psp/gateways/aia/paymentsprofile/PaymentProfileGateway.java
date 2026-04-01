package com.intuit.sbd.payroll.psp.gateways.aia.paymentsprofile;

import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.platform.services.ebpi.billing.v2.PaymentProfile;
import com.intuit.platform.services.ebpi.billing.v2.SearchPaymentProfileResponse;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.gateways.aia.AIAGatewayConfig;
import com.intuit.sbd.payroll.psp.payments.PaymentServiceException;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.payroll.iam.AuthorizationManager;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PaymentProfileGateway {

    private static final SpcfLogger logger = PayrollServices.getLogger(PaymentProfileGateway.class);

    private AuthorizationManager authorizationManager;
    private PaymentsProfileClient paymentsProfileClient;
    private OfflineTicketClient offlineTicketClient;

    @Autowired
    public PaymentProfileGateway(PaymentsProfileClient paymentsProfileClient, AuthorizationManager authorizationManager, OfflineTicketClient offlineTicketClient){
        this.authorizationManager = authorizationManager;
        this.paymentsProfileClient = paymentsProfileClient;
        this.offlineTicketClient = offlineTicketClient;
    }

    protected void setAuthorizationContext() {
        setAuthorizationContext(null);
    }

    protected void setAuthorizationContext(String targetRealmId) {
        logger.info("Setting authorization context to call Payment Services");
        logger.info("Setting AuthN ID2 context");
        String offlineTicket = offlineTicketClient.getOfflineTicket();
        if(StringUtils.isEmpty(offlineTicket)) {
            throw new PaymentServiceException(String.format("Unable to set Payment Services Authorization Context ID2 for RealmId=%s", targetRealmId));
        }
        RequestAttributesUtils.setAttribute(ContextConstants.AUTHN_CONTEXT, offlineTicket);
    }

    protected void removeAuthorizationContext() {
        RequestAttributesUtils.removeAttribute(ContextConstants.AUTHN_CONTEXT);
    }

    public PaymentProfile getPaymentProfileDetailsFromEOCLIC(String eoc, String lic, String can){
        SearchPaymentProfileResponse searchPaymentProfileResponse = searchPaymentProfileDetailsFromEOCLIC(eoc,lic,can);
        if(CollectionUtils.isNotEmpty(searchPaymentProfileResponse.getPaymentProfiles())) {
            return searchPaymentProfileResponse.getPaymentProfiles().get(0);
        }
        logger.error("Payment Profile is empty ");
        return null;
    }

    public SearchPaymentProfileResponse searchPaymentProfileDetailsFromEOCLIC(String eoc, String lic, String can){
        logger.info("Begin of getPaymentProfileDetailsFromEOCLIC method:");
        SearchPaymentProfileResponse searchPaymentProfileResponse = null;
        try{
            setAuthorizationContext();
            searchPaymentProfileResponse = paymentsProfileClient.getPaymentProfileDetailsFromEOCLIC(eoc, lic, can);
        } finally {
            removeAuthorizationContext();
        }
        return searchPaymentProfileResponse;
    }
}
