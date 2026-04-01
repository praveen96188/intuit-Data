package com.intuit.sbd.payroll.psp.emailsender.service;

import com.intuit.sbd.payroll.psp.emailsender.NotificationDataType;
import com.intuit.sbd.payroll.psp.emailsender.NotificationTypeDataType;
import com.intuit.sbd.payroll.psp.emailsender.SendRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailSettings;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
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
public class TransactionalExactTargetStrategyTest {

    private TransactionalEmailExactTargetStrategy transactionalEmailExactTargetStrategy;

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
        transactionalEmailExactTargetStrategy = new TransactionalEmailExactTargetStrategy(emailResponseProcessor, emailRequestValidator, client, emailSettings);
    }


    @Test
    public void validateSendEmailTest() throws Exception {
        String endPoint = "/abcde";
        String baseUrl = "https://dummy.com";
        WebResource webResource = Mockito.mock(WebResource.class);
        when(emailSettings.getExactTargetApi()).thenReturn(endPoint);
        when(emailSettings.getUrl()).thenReturn(baseUrl);
        when(client.resource(baseUrl + endPoint)).thenReturn(webResource);
        WebResource.Builder builder = Mockito.mock(WebResource.Builder.class);


        EmailRequest emailRequest = getEmailReq();


        ClientResponse httpResponse = Mockito.mock(ClientResponse.class);
        when(webResource.getRequestBuilder()).thenReturn(builder);
        when(builder.header(Mockito.anyString(), Mockito.anyString())).thenReturn(builder);
        when(builder.post(ClientResponse.class, emailRequest)).thenReturn(httpResponse);

        EmailResponse response = mock(EmailResponse.class);

        when(response.getStatus()).thenReturn(HttpStatus.SC_OK);
        when(response.getResponseBody()).thenReturn("OK");
        MultivaluedMap<String, String> headers = Mockito.mock(MultivaluedMap.class);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get("intuit_tid")).thenReturn(Collections.singletonList("tid"));
        when(emailResponseProcessor.getEmailResponse(Mockito.any(ClientResponse.class), Mockito.any(EmailRequest.class))).thenReturn(response);

        EmailResponse response1 = transactionalEmailExactTargetStrategy.sendMail(emailRequest);
        verify(emailSettings).getExactTargetApi();
        verify(emailSettings).getUrl();
        verify(client).resource(Matchers.eq(baseUrl + endPoint));
        assertEquals(response.getStatus(), response1.getStatus());
        assertEquals(response.getResponseBody(), response1.getResponseBody());

    }

    public EmailRequest getEmailReq() {


        // Construct the Send request
        SendRequest request = new SendRequest();

        NotificationDataType notification = new NotificationDataType();

        NotificationDataType.Destinations destinations = new NotificationDataType.Destinations();
        NotificationDataType.Destinations.Destination destination = new NotificationDataType.Destinations.Destination();
        destinations.getDestination().add(destination);
        notification.setDestinations(destinations);


        notification.setNotificationType(NotificationTypeDataType.EMAIL);
        notification.setDeliveryType(new NotificationDataType.DeliveryType()
        );
        notification.setSenderProfile(new NotificationDataType.SenderProfile());
        notification.setContentProfile(new NotificationDataType.ContentProfile());


        request.setNotification(notification);
        // Request version
        request.setVersion("1.0");

        return EmailRequest.builder().sendRequest(request).emailStrategyType(EmailStrategyType.ExactTarget).build();

    }
}