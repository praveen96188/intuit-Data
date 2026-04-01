package com.intuit.sbd.payroll.psp.emailsender.processor;

import com.intuit.sbd.payroll.psp.emailsender.NotificationDataType;
import com.intuit.sbd.payroll.psp.emailsender.NotificationTypeDataType;
import com.intuit.sbd.payroll.psp.emailsender.SendRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.exception.EmailServiceException;
import com.intuit.sbd.payroll.psp.emailsender.model.ExactTargetResults;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.testng.annotations.BeforeMethod;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class EmailResponseProcessorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    EmailResponseProcessor emailResponseProcessor;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.initMocks(this); //This could be pulled up into a shared base class
    }

    @Test(expected = EmailServiceException.class)
    public void validateEmailResponseTestNull() {
        emailResponseProcessor.processResponse(null);
    }

    @Test
    public void validateEmailResponseTest204() {
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.getStatus()).thenReturn(HttpStatus.SC_NO_CONTENT);
        EmailResponse emailResponse = emailResponseProcessor.processResponse(clientResponse);
        assertEquals(HttpStatus.SC_NO_CONTENT, emailResponse.getStatus());
    }

    @Test
    public void validateEmailResponseTest201() {
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.getStatus()).thenReturn(HttpStatus.SC_CREATED);
        EmailResponse emailResponse = emailResponseProcessor.processResponse(clientResponse);
        assertEquals(HttpStatus.SC_CREATED, emailResponse.getStatus());
    }

    @Test(expected = EmailServiceException.class)
    public void validateEmailResponseTest500() {
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        EmailResponse emailResponse = emailResponseProcessor.processResponse(clientResponse);
    }

    @Test(expected = EmailServiceException.class)
    public void validateEmailResponseTest401() {
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.getStatus()).thenReturn(HttpStatus.SC_UNAUTHORIZED);
        EmailResponse emailResponse = emailResponseProcessor.processResponse(clientResponse);
    }

    @Test(expected = EmailServiceException.class)
    public void validateEmailResponseTest400() {
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.getStatus()).thenReturn(HttpStatus.SC_BAD_REQUEST);
        EmailResponse emailResponse = emailResponseProcessor.processResponse(clientResponse);
    }

    @Test
    public void validateEmailResponseTest200() {
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(clientResponse.getStatus()).thenReturn(HttpStatus.SC_OK);
        EmailResponse emailResponse = emailResponseProcessor.processResponse(clientResponse);
        assertEquals(HttpStatus.SC_OK, emailResponse.getStatus());
    }
    @Test
    public void validateExactTargetEmailResponseTest200() {
        EmailResponse emailResponse = mock(EmailResponse.class);
        when(emailResponse.getStatus()).thenReturn(200);
        EmailRequest emailRequest = getEmailReq();
        ExactTargetResults exactTargetResults = mock(ExactTargetResults.class);
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(emailResponse.getClientResponse()).thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(200);
        emailResponseProcessor.getEmailResponse(emailResponse.getClientResponse(),emailRequest);
        assertEquals(200, emailResponse.getStatus());
    }
    @Test
    public void validateExactTargetEmailResponseTestNot200() {
        EmailResponse emailResponse = mock(EmailResponse.class);
        when(emailResponse.getStatus()).thenReturn(500);
        EmailRequest emailRequest = getEmailReq();
        ExactTargetResults exactTargetResults = mock(ExactTargetResults.class);
        ClientResponse clientResponse = mock(ClientResponse.class);
        when(emailResponse.getClientResponse()).thenReturn(clientResponse);
        when(clientResponse.getStatus()).thenReturn(500);
        emailResponseProcessor.getEmailResponse(emailResponse.getClientResponse(),emailRequest);
        assertEquals(500, emailResponse.getStatus());

    }

    public EmailRequest getEmailReq(){
        // Construct the Send request
        SendRequest request = new SendRequest();

        NotificationDataType notification = new NotificationDataType();

        notification.setNotificationType(NotificationTypeDataType.EMAIL);
        notification.setDeliveryType(new NotificationDataType.DeliveryType()
        );
        notification.setSenderProfile(new NotificationDataType.SenderProfile());
        notification.setContentProfile(new NotificationDataType.ContentProfile( ));
        notification.setDestinations(new NotificationDataType.Destinations());

        request.setNotification(notification);
        // Request version
        request.setVersion("1.0");


        return EmailRequest.builder().sendRequest(request).emailStrategyType(EmailStrategyType.ExactTarget).build();

    }

}