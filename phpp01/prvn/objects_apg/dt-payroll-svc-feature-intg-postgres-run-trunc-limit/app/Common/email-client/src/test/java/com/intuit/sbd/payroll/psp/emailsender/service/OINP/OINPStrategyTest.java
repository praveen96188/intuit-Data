package com.intuit.sbd.payroll.psp.emailsender.service.OINP;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.gateway.OINPRestServiceGateway;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPEventRequest;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class OINPStrategyTest {

    @Mock
    private OINPRestServiceGateway oinpServiceAgent;

    @Mock
    private EmailRequestValidator emailRequestValidator;

    @Mock
    private OINPRequestUtility oinpRequestUtility;

    @InjectMocks
    private OINPStrategy oinpStrategy;

    @Before
    public void setUp() {
        oinpStrategy = new OINPStrategy(emailRequestValidator, oinpRequestUtility, oinpServiceAgent);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateSendEmailTest() throws Exception {

        EmailRequest emailRequest = mock(EmailRequest.class);
        EmailResponse response = mock(EmailResponse.class);

        when(oinpServiceAgent.publishEventToOINP(any(OINPEventRequest.class))).thenReturn(response);

        when(response.getStatus()).thenReturn(HttpStatus.SC_OK);
        when(response.getResponseBody()).thenReturn("OK");
        Map<String, String> headers = Mockito.mock(HashMap.class);
        when(response.getHttpServiceResponseHeaders()).thenReturn(headers);
        when(headers.get("intuit_tid")).thenReturn("tid");
        EmailResponse response1 = oinpStrategy.sendMail(emailRequest);
        verify(oinpServiceAgent).publishEventToOINP(any(OINPEventRequest.class));
        assertEquals(response.getStatus(), response1.getStatus());
        assertEquals(response.getResponseBody(), response1.getResponseBody());
    }
}
