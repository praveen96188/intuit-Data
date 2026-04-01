package com.intuit.sbd.payroll.psp.emailsender.validator;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.service.OINP.OINPRequestUtility;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailRequestValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @InjectMocks
    EmailRequestValidator emailRequestValidator;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); //This could be pulled up into a shared base class
    }

    @Test
    public void validateEmailRequestTestToEmailNull() {
        String[] emailArray={} ;
        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getToEmailAddresses()).thenReturn(emailArray);
        exception.expect(Exception.class);
        emailRequestValidator.validate(emailRequest);
    }

    @Test
    public void validateEmailRequestTestEmailSubjectNull() {
        String[] emailArray={"a@gmail.com"} ;
        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getToEmailAddresses()).thenReturn(emailArray);
        when(emailRequest.getSubject()).thenReturn("");
        exception.expect(Exception.class);
        exception.expectMessage(containsString("Bad Request, Email Subject is Null or empty"));
        emailRequestValidator.validate(emailRequest);
    }

    @Test
    public void validateEmailRequestTestBodyNull() {
        String[] emailArray={"a@gmail.com"} ;
        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getToEmailAddresses()).thenReturn(emailArray);
        when(emailRequest.getSubject()).thenReturn("Test Subject");
        exception.expect(Exception.class);
        exception.expectMessage(containsString("Bad Request, Email body is Null or empty"));
        emailRequestValidator.validate(emailRequest);
    }

    @Test
    public void validateEmailRequestValidTest() {
        String[] emailArray={"a@gmail.com"} ;
        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getToEmailAddresses()).thenReturn(emailArray);
        when(emailRequest.getSubject()).thenReturn("Test Subject");
        when(emailRequest.getHtmlContent()).thenReturn("Test Content");
        Boolean isValid = emailRequestValidator.validate(emailRequest);
    }

    @Test
    public void validateEmailRequestInValidToEmail() {
        String[] emailArray={"a@gmail..com"} ;
        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getToEmailAddresses()).thenReturn(emailArray);
        when(emailRequest.getSubject()).thenReturn("Test Subject");
        when(emailRequest.getHtmlContent()).thenReturn("Test Content");
    }

    @Test
    public void validateEmailRequestInValidFromEmail() {
        String[] emailArray={"a@gmail.com"} ;
        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getToEmailAddresses()).thenReturn(emailArray);
        when(emailRequest.getSubject()).thenReturn("Test Subject");
        when(emailRequest.getHtmlContent()).thenReturn("Test Content");
        when(emailRequest.getFromEmailAddress()).thenReturn("c.com");
        exception.expect(Exception.class);
        exception.expectMessage(containsString("From Email address is not valid"));
        emailRequestValidator.validate(emailRequest);
    }
    
    @Test
    public void validateOINPTemplateTestNameNull() {
        exception.expect(Exception.class);
        exception.expectMessage(containsString("Bad Request, Template name is Null or empty"));
        emailRequestValidator.validateOINPTemplateProperties("","testType", "testServiceName");
    }

    @Test
    public void validateOINPTemplateTestObjectTypeNull() {
        exception.expect(Exception.class);
        exception.expectMessage(containsString("Bad Request, Template ObjectType is Null or empty"));
        emailRequestValidator.validateOINPTemplateProperties("testName","", "testServiceName");
    }

    @Test
    public void validateOINPTemplateTestServiceNull() {
        exception.expect(Exception.class);
        exception.expectMessage(containsString("Bad Request, Template sourceServiceName is Null or empty"));
        emailRequestValidator.validateOINPTemplateProperties("testName","testType", "");
    }

    @Test
    public void validateOINPEventDataTestNoSubjectKey() {
        Map<String,Object> prop = new HashMap<>();
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT, "testHTML");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS, "testFromEmail@gmail.com");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES, "testToEmail@gmail.com");

        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getTemplateAttributes()).thenReturn(prop);
        exception.expect(Exception.class);
        exception.expectMessage(containsString("no Email Subject"));
        emailRequestValidator.validateOINPStrategyEventData(emailRequest);
    }
    
    @Test
    public void validateOINPEventDataTestSubjectNull() {
        Map<String,Object> prop = new HashMap<>();
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT, "");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT, "testHTML");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS, "testFromEmail@gmail.com");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES, "testToEmail@gmail.com");

        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getTemplateAttributes()).thenReturn(prop);
        exception.expect(Exception.class);
        exception.expectMessage(containsString("Email Subject is Null or empty"));
        emailRequestValidator.validateOINPStrategyEventData(emailRequest);
    }

    @Test
    public void validateOINPEventDataTestNoHtmlContentKey() {
        Map<String,Object> prop = new HashMap<>();
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT, "testSubject");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS, "testFromEmail@gmail.com");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES, "testToEmail@gmail.com");

        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getTemplateAttributes()).thenReturn(prop);
        exception.expect(Exception.class);
        exception.expectMessage(containsString("no HTML Content"));
        emailRequestValidator.validateOINPStrategyEventData(emailRequest);
    }

    @Test
    public void validateOINPEventDataTestHtmlContentNull() {
        Map<String,Object> prop = new HashMap<>();
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT, "testSubject");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT, "");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS, "testFromEmail@gmail.com");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES, "testToEmail@gmail.com");

        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getTemplateAttributes()).thenReturn(prop);
        exception.expect(Exception.class);
        exception.expectMessage(containsString("Email body is Null or empty"));
        emailRequestValidator.validateOINPStrategyEventData(emailRequest);
    }

    @Test
    public void validateOINPEventDataTestNoToEmailKey() {
        Map<String,Object> prop = new HashMap<>();
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT, "testSubject");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS, "testFromEmail@gmail.com");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT, "testHTML");

        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getTemplateAttributes()).thenReturn(prop);
        exception.expect(Exception.class);
        exception.expectMessage(containsString("no to Email"));
        emailRequestValidator.validateOINPStrategyEventData(emailRequest);
    }

    @Test
    public void validateOINPEventDataTestToEmailNull() {
        Map<String,Object> prop = new HashMap<>();
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT, "testSubject");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT, "testHTML");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS, "testFromEmail@gmail.com");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES, "");

        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getTemplateAttributes()).thenReturn(prop);
        exception.expect(Exception.class);
        exception.expectMessage(containsString("to Email is Null or empty"));
        emailRequestValidator.validateOINPStrategyEventData(emailRequest);
    }

    @Test
    public void validateOINPEventDataTestNoFromAddressKey() {
        Map<String,Object> prop = new HashMap<>();
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT, "testSubject");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT, "testHTML");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES, "testToEmail@gmail.com");

        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getTemplateAttributes()).thenReturn(prop);
        exception.expect(Exception.class);
        exception.expectMessage(containsString("no from Email Address"));
        emailRequestValidator.validateOINPStrategyEventData(emailRequest);
    }
    
    @Test
    public void validateOINPEventDataValidTest() {
        Map<String,Object> prop = new HashMap<>();
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT, "testSubject");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT, "testHTML");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS, "testFromEmail@gmail.com");
        prop.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES, "testToEmail@gmail.com");

        EmailRequest emailRequest = mock(EmailRequest.class);
        when(emailRequest.getTemplateAttributes()).thenReturn(prop);
        boolean isValid = emailRequestValidator.validateOINPStrategyEventData(emailRequest);

        assertEquals(isValid, true);
    }


    
}