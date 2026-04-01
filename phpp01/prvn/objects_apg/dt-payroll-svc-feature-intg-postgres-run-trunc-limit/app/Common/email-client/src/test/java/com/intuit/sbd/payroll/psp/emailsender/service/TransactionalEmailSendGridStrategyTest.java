package com.intuit.sbd.payroll.psp.emailsender.service;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailSettings;
import com.intuit.sbd.payroll.psp.emailsender.processor.EmailResponseProcessor;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.core.MultivaluedMap;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionalEmailSendGridStrategyTest {

    private TransactionalEmailSendGridStrategy transactionalEmailSendGridStrategy;

    @Mock
    private EmailSettings emailSettings;

    @Mock
    private EmailRequestValidator emailRequestValidator;

    @Mock
    private EmailResponseProcessor emailResponseProcessor;

    @Mock
    private EmailRequest emailRequest;

    @Mock
    private EmailSendStrategyAbstract emailSendStrategyAbstract;

    @Mock
    private EmailResponse emailResponse;

    @Mock
    private Client client;


    @Before
    public void setUp() {
        transactionalEmailSendGridStrategy = new TransactionalEmailSendGridStrategy(emailResponseProcessor, emailRequestValidator, client, emailSettings);
    }


    @Test
    public void validateSendEmailTest() throws Exception {
        String endPoint = "/abcde";
        String baseUrl = "https://dummy.com";
        WebResource webResource = Mockito.mock(WebResource.class);
        when(emailSettings.getSendgridApi()).thenReturn(endPoint);
        when(emailSettings.getUrl()).thenReturn(baseUrl);
        when(client.resource(baseUrl+endPoint)).thenReturn(webResource);
        WebResource.Builder builder = Mockito.mock(WebResource.Builder.class);
        EmailRequest emailRequest = EmailRequest.builder().toEmailAddresses(new String[]{}).subject("").htmlContent("").build();
        ClientResponse httpResponse = Mockito.mock(ClientResponse.class);
        when(webResource.getRequestBuilder()).thenReturn(builder);
        when(builder.post(ClientResponse.class, emailRequest)).thenReturn(httpResponse);
        EmailResponse response = mock(EmailResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.SC_OK);
        when(response.getResponseBody()).thenReturn("OK");
        MultivaluedMap<String, String> headers = Mockito.mock(MultivaluedMap.class);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get("intuit_tid")).thenReturn(Collections.singletonList("tid"));
        when(emailResponseProcessor.getEmailResponse(Mockito.any(ClientResponse.class))).thenReturn(response);
        EmailResponse response1 = transactionalEmailSendGridStrategy.sendMail(emailRequest);
        verify(emailSettings).getSendgridApi();
        verify(emailSettings).getUrl();
        verify(client).resource(Matchers.eq(baseUrl+endPoint));
        verify(builder).post(ClientResponse.class, emailRequest);
        assertEquals(response.getStatus(), response1.getStatus());
        assertEquals(response.getResponseBody(), response1.getResponseBody());
    }
}