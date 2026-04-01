package com.intuit.sbd.payroll.psp.ius;

import com.intuit.cto.auth.utils.AuthHeaderUtils;
import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.platform.integration.ius.common.types.IAMTicket;
import com.intuit.platform.jsk.security.iam.autoconfig.IntuitSecurityProperties;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.payroll.iam.context.AuthorizationContextBuilder;
import com.intuit.sbg.psp.payroll.iam.context.AuthorizationUtils;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.apache.commons.lang3.Validate;

public class TestAuthorizationManager {

    private IntuitSecurityProperties intuitSecurityProperties;
    private OfflineTicketClient offlineTicketClient;

    public TestAuthorizationManager() {
        intuitSecurityProperties = PayrollApplicationBeanFactory.getBean(IntuitSecurityProperties.class);
        offlineTicketClient = PayrollApplicationBeanFactory.getBean(OfflineTicketClient.class);
    }

    public void setUserAuthorizationContext(String userId, String token) {
        Validate.notEmpty(userId, "userId cannot be null or empty");
        Validate.notEmpty(token, "token cannot be null or empty");
        AuthorizationContext authorizationContext = new AuthorizationContextBuilder().userId(userId).token(token).tokenType(AuthHeaderUtils.TOKEN_TYPE_IAM_TICKET).appId(intuitSecurityProperties.getAppId()).appSecret(intuitSecurityProperties.getAppSecret()).build();
        setUserAuthorizationContext(authorizationContext);
    }

    public void setUserAuthorizationContext() {
        AuthorizationUtils authorizationUtils = new AuthorizationUtils();
        setUserAuthorizationContext(authorizationUtils.buildAuthorizationContext(offlineTicketClient.getOfflineTicket()));
    }

    public void setUserAuthorizationContext(IAMTicket iamTicket) {
        AuthorizationContext authorizationContext = new AuthorizationContext();
        authorizationContext.setRealmId(iamTicket.getRealmId());
        authorizationContext.setIAMTicket(iamTicket);
        authorizationContext.setUserId(iamTicket.getUserId());
        setUserAuthorizationContext(authorizationContext);
    }

    public void setUserAuthorizationContext(AuthorizationContext authorizationContext) {
        RequestAttributesUtils.setAttribute(ContextConstants.USER_AUTHORIZATION_CONTEXT, authorizationContext);
    }

    public void removeUserAuthorizationContext() {
        RequestAttributesUtils.removeAttribute(ContextConstants.USER_AUTHORIZATION_CONTEXT);
    }
}
