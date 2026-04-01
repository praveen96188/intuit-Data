package com.intuit.sbd.payroll.psp.payments;

import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.payroll.iam.AuthorizationManager;
import com.intuit.sbg.psp.webserviceclient.context.ContextConstants;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Objects;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;

@PowerMockIgnore({"javax.net.ssl.*", "javax.security.auth.x500.X500Principal", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PaymentServiceAuthorizationManager.class, FeatureFlags.class, RequestAttributesUtils.class, ConfigurationManager.class})
public class PaymentServiceAuthorizationManagerTest {


    private FeatureFlags featureFlags;
    private AuthorizationManager authorizationManagerMock;
    private OfflineTicketClient offlineTicketClientMock;
    private PaymentServiceAuthorizationManager paymentServiceAuthorizationManager;

    @Before
    public void setUp() throws Exception {
        authorizationManagerMock = PowerMockito.mock(AuthorizationManager.class);
        offlineTicketClientMock = PowerMockito.mock(OfflineTicketClient.class);
        featureFlags = PowerMockito.mock(FeatureFlags.class);
        PowerMockito.whenNew(AuthorizationManager.class).withAnyArguments().thenReturn(authorizationManagerMock);
        PowerMockito.mockStatic(FeatureFlags.class);
        PowerMockito.when(FeatureFlags.get()).thenReturn(featureFlags);
        paymentServiceAuthorizationManager = new PaymentServiceAuthorizationManager(authorizationManagerMock, offlineTicketClientMock);
    }

    @Test(expected=PaymentServiceException.class)
    public void setAuthorizationContextTest_elseAuthContextIsNull() {
        PowerMockito.when(featureFlags.booleanValue(any(), anyBoolean())).thenReturn(false);
        PowerMockito.when(authorizationManagerMock.getAuthorizationContext(any(),any())).thenReturn(null);
        paymentServiceAuthorizationManager.setAuthorizationContext();
    }

    @Test
    public void setAuthorizationContextTest_ifSuccessWithRealmIdNotNull() {
        PowerMockito.when(featureFlags.booleanValue(any(), anyBoolean())).thenReturn(true);
        PowerMockito.when(offlineTicketClientMock.getOfflineTicket(any())).thenReturn("offlineTicket");
        paymentServiceAuthorizationManager.setAuthorizationContext("9130359795302666");
        Assert.assertNotNull(RequestAttributesUtils.getAttribute(ContextConstants.AUTHN_CONTEXT, AuthorizationContext.class));
    }

    @Test
    public void setAuthorizationContextTest_ifSuccessWithRealmIdNull() {
        PowerMockito.when(featureFlags.booleanValue(any(), anyBoolean())).thenReturn(true);
        PowerMockito.when(offlineTicketClientMock.getOfflineTicket()).thenReturn("offlineTicket");
        paymentServiceAuthorizationManager.setAuthorizationContext(null);
        Assert.assertNotNull(RequestAttributesUtils.getAttribute(ContextConstants.AUTHN_CONTEXT, AuthorizationContext.class));
    }

    @Test(expected=PaymentServiceException.class)
    public void setAuthorizationContextTest_offlineTicketIsEmpty() {
        PowerMockito.when(featureFlags.booleanValue(any(), anyBoolean())).thenReturn(true);
        PowerMockito.when(offlineTicketClientMock.getOfflineTicket()).thenReturn(null);
        paymentServiceAuthorizationManager.setAuthorizationContext(null);
    }

    @Test
    public void setValidationServiceAuthorizationContext_ifSuccess() {
        PowerMockito.when(featureFlags.booleanValue(any(), anyBoolean())).thenReturn(true);
        PowerMockito.when(offlineTicketClientMock.getOfflineTicket()).thenReturn("offlineTicket");
        paymentServiceAuthorizationManager.setValidationServiceAuthorizationContext();
        Assert.assertNotNull(RequestAttributesUtils.getAttribute(ContextConstants.AUTHN_CONTEXT, AuthorizationContext.class));
    }

    @Test(expected=PaymentServiceException.class)
    public void setValidationServiceAuthorizationContext_offlineTicketIsEmpty() {
        PowerMockito.when(featureFlags.booleanValue(any(), anyBoolean())).thenReturn(true);
        PowerMockito.when(offlineTicketClientMock.getOfflineTicket()).thenReturn(null);
        paymentServiceAuthorizationManager.setValidationServiceAuthorizationContext();
    }
}
