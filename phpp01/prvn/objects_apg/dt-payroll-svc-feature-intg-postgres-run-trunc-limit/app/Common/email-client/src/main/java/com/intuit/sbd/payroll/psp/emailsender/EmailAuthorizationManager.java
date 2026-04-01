package com.intuit.sbd.payroll.psp.emailsender;

import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.emailsender.exception.EmailServiceException;
//import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.payroll.iam.AuthorizationManager;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Objects;

public class EmailAuthorizationManager {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(EmailAuthorizationManager.class);
    private AuthorizationManager authorizationManager;
    private OfflineTicketClient offlineTicketClient;

    @Autowired
    public EmailAuthorizationManager(AuthorizationManager authorizationManager, OfflineTicketClient offlineTicketClient) {
        this.authorizationManager = authorizationManager;
        this.offlineTicketClient = offlineTicketClient;
    }

    public void setAuthorizationContext() {
        setAuthorizationContext(null);
    }

    public void setAuthorizationContext(String targetRealmId) {
        logger.info("Setting authorization context to call Email Sender Services");
        logger.info("Setting Authn ID2 context");
        String offlineTicket = offlineTicketClient.getOfflineTicket();
        if(StringUtils.isEmpty(offlineTicket)) {
            throw new EmailServiceException(String.format("Unable to set Email Services Authorization ID2 Context for RealmId=%s", targetRealmId));
        }
        RequestAttributesUtils.setAttribute(ContextConstants.AUTHN_CONTEXT, offlineTicket);
    }

    public void removeAuthorizationContext() {
        RequestAttributesUtils.removeAttribute(ContextConstants.AUTHN_CONTEXT);
    }
}
