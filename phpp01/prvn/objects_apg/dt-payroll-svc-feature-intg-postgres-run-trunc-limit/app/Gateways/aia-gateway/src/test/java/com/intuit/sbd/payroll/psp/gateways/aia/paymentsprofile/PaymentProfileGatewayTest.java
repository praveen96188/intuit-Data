package com.intuit.sbd.payroll.psp.gateways.aia.paymentsprofile;

import com.intuit.platform.integration.ius.common.types.AuthorizationContext;
import com.intuit.platform.services.ebpi.billing.v2.PaymentProfile;
import com.intuit.platform.services.ebpi.billing.v2.PaymentProfileImpl;
import com.intuit.platform.services.ebpi.billing.v2.SearchPaymentProfileResponse;
import com.intuit.platform.services.ebpi.billing.v2.SearchPaymentProfileResponseImpl;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.payments.PaymentServiceException;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.psp.payroll.iam.AuthorizationManager;
import com.intuit.sbg.psp.webserviceclient.context.request.RequestAttributesUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;

@PowerMockIgnore({"javax.net.ssl.*", "javax.security.auth.x500.X500Principal", "javax.management.*"})
@RunWith(PowerMockRunner.class)
@PrepareForTest({PaymentProfileGateway.class, FeatureFlags.class, RequestAttributesUtils.class})
public class PaymentProfileGatewayTest {

    private PaymentProfileGateway paymentProfileGateway;
    private FeatureFlags featureFlags;
    private AuthorizationManager authorizationManagerMock;
    private OfflineTicketClient offlineTicketClientMock;
    private PaymentsProfileClient paymentsProfileClientMock;

    @Before
    public void setup() throws Exception {
        authorizationManagerMock = PowerMockito.mock(AuthorizationManager.class);
        offlineTicketClientMock = PowerMockito.mock(OfflineTicketClient.class);
        featureFlags = PowerMockito.mock(FeatureFlags.class);
        paymentsProfileClientMock = PowerMockito.mock(PaymentsProfileClient.class);
        PowerMockito.whenNew(AuthorizationManager.class).withAnyArguments().thenReturn(authorizationManagerMock);
        PowerMockito.mockStatic(FeatureFlags.class);
        PowerMockito.when(FeatureFlags.get()).thenReturn(featureFlags);
        paymentProfileGateway = new PaymentProfileGateway(paymentsProfileClientMock, authorizationManagerMock, offlineTicketClientMock);
    }

    @Test(expected= PaymentServiceException.class)
    public void getPaymentProfileDetailsFromEOCLIC_setAuthzContextWith() {
        SearchPaymentProfileResponse searchPaymentProfileResponse = new SearchPaymentProfileResponseImpl();
        List<PaymentProfile> list = new ArrayList<>();
        PaymentProfile pp = new PaymentProfileImpl();
        pp.setName("TestName");
        list.add(pp);
        searchPaymentProfileResponse.setPaymentProfiles(list);
        PowerMockito.when(paymentsProfileClientMock.getPaymentProfileDetailsFromEOCLIC(any(),any(),any())).thenReturn(searchPaymentProfileResponse);
        PowerMockito.when(featureFlags.booleanValue(any(), anyBoolean())).thenReturn(false);
        PowerMockito.when(authorizationManagerMock.getAuthorizationContext(any(),any())).thenReturn(null);
        PaymentProfile paymentProfile = paymentProfileGateway.getPaymentProfileDetailsFromEOCLIC("eoc","lic","can");
    }

    @Test
    public void getPaymentProfileDetailsFromEOCLIC_setAuthzContextWithOfflineTicket() {
        PowerMockito.mockStatic(RequestAttributesUtils.class);
        AuthorizationContext authzContext = new AuthorizationContext();
        PowerMockito.when(RequestAttributesUtils.getAttribute(any(), any())).thenReturn(authzContext);
        SearchPaymentProfileResponse searchPaymentProfileResponse = new SearchPaymentProfileResponseImpl();
        List<PaymentProfile> list = new ArrayList<>();
        PaymentProfile pp = new PaymentProfileImpl();
        pp.setName("TestName");
        list.add(pp);
        searchPaymentProfileResponse.setPaymentProfiles(list);
        PowerMockito.when(paymentsProfileClientMock.getPaymentProfileDetailsFromEOCLIC(any(),any(),any())).thenReturn(searchPaymentProfileResponse);
        PowerMockito.when(featureFlags.booleanValue(any(), anyBoolean())).thenReturn(true);
        PowerMockito.when(offlineTicketClientMock.getOfflineTicket()).thenReturn("offlineTicket");
        PaymentProfile paymentProfile = paymentProfileGateway.getPaymentProfileDetailsFromEOCLIC("eoc","lic","can");
        Assert.assertNotNull(paymentProfile);
        Assert.assertEquals(paymentProfile.getName(), "TestName");
    }

    @Test(expected= PaymentServiceException.class)
    public void getPaymentProfileDetailsFromEOCLIC_setAuthzContextWithOfflineTicketIsEmpty() {
        PowerMockito.mockStatic(RequestAttributesUtils.class);
        AuthorizationContext authzContext = new AuthorizationContext();
        PowerMockito.when(RequestAttributesUtils.getAttribute(any(), any())).thenReturn(authzContext);
        SearchPaymentProfileResponse searchPaymentProfileResponse = new SearchPaymentProfileResponseImpl();
        List<PaymentProfile> list = new ArrayList<>();
        PaymentProfile pp = new PaymentProfileImpl();
        pp.setName("TestName");
        list.add(pp);
        searchPaymentProfileResponse.setPaymentProfiles(list);
        PowerMockito.when(paymentsProfileClientMock.getPaymentProfileDetailsFromEOCLIC(any(),any(),any())).thenReturn(searchPaymentProfileResponse);
        PowerMockito.when(featureFlags.booleanValue(any(), anyBoolean())).thenReturn(true);
        PowerMockito.when(offlineTicketClientMock.getOfflineTicket()).thenReturn(null);
        PaymentProfile paymentProfile = paymentProfileGateway.getPaymentProfileDetailsFromEOCLIC("eoc","lic","can");
    }
}
