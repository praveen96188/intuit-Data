package com.intuit.sbd.payroll.psp.emailsender.domain;


import com.intuit.sbd.payroll.psp.emailsender.SendRequest;
import com.intuit.sbd.payroll.psp.emailsender.service.OINP.OINPRequestUtility;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class EmailRequestTest {

    @Mock
    private EmailSettings emailSettings;

    @Test
    public void testEmailRequestBuilder() {
        String[] emailArray={"a@gmail.com"} ;
        String toEmail = "a@gmail.com";
        EmailRequest emailRequest = EmailRequest.builder()
                .toEmailAddresses(toEmail.split(","))
                .subject("testsubject")
                .htmlContent("testbody")
                .build();
        assertEquals(emailRequest.getToEmailAddresses(), emailArray);
        assertEquals(emailRequest.getSubject(), "testsubject");
        assertEquals(emailRequest.getHtmlContent(), "testbody");
    }

    @Test
    public void testEmailRequestGetters() {
        EmailRequest emailRequest = EmailRequest.builder()
                .subject("testsubject")
                .htmlContent("testbody")
                .emailStrategyType(EmailStrategyType.SendGrid)
                .fromEmailAddress("fromEmailAddress")
                .fromEmailDisplayName("testDsiplayName")
                .pHighPriority(true)
                .replyEmailAddress("replyemailaddress")
                .build();
        emailRequest.setAttachmentList(Collections.singletonList("list"));

        assertEquals(emailRequest.getEmailStrategyType(), EmailStrategyType.SendGrid);
        assertEquals(emailRequest.getFromEmailAddress(), "fromEmailAddress");
        assertEquals(emailRequest.getFromEmailDisplayName(), "testDsiplayName");
        assertEquals(emailRequest.getPriority(), true);
        assertEquals(emailRequest.getReplyEmailAddress(), "replyemailaddress");
        assertEquals(emailRequest.getAttachmentList(), Arrays.asList("list"));
    }

    @Test
    public void testEmailRequestGettersForExactTarget() {
        SendRequest sendRequest = new SendRequest();
        EmailRequest emailRequest = EmailRequest.builder().sendRequest(sendRequest)
                .emailStrategyType(EmailStrategyType.ExactTarget)
                .build();
        assertEquals(emailRequest.getEmailStrategyType(), EmailStrategyType.ExactTarget);
        assertEquals(emailRequest.getSendRequest(), sendRequest);

    }

    @Test
    public void testEmailRequestGettersForOINP() {
        EmailRequest emailRequest = EmailRequest.builder()
                .subject("testsubject")
                .htmlContent("testbody")
                .emailStrategyType(EmailStrategyType.OINP)
                .fromEmailAddress("fromEmailAddress")
                .intuitTid("tid")
                .templateObjectType("testObject")
                .templateName("testName")
                .build();

        assertEquals(emailRequest.getEmailStrategyType(), EmailStrategyType.OINP);
        assertEquals(emailRequest.getIntuitTid(), "tid");
        assertEquals(emailRequest.getTemplateName(), "testName");
        assertEquals(emailRequest.getTemplateObjectType(),"testObject");

    }

    @Test
    public void testEmailRequestForOINPStrategy() {
        Map<String,Object> properties = new HashMap<>();
        properties.put(OINPRequestUtility.OINPStrategyPropertyKeys.TO_EMAIL_ADDRESSES,"toEmail");
        properties.put(OINPRequestUtility.OINPStrategyPropertyKeys.FROM_EMAIL_ADDRESS,"fromEmail");
        properties.put(OINPRequestUtility.OINPStrategyPropertyKeys.SUBJECT,"subject");
        properties.put(OINPRequestUtility.OINPStrategyPropertyKeys.HTML_CONTENT,"htmlContent");

        EmailRequest emailRequest = EmailRequest.builder()
                .subject("testsubject")
                .htmlContent("testbody")
                .emailStrategyType(EmailStrategyType.OINP)
                .fromEmailAddress("fromEmailAddress")
                .intuitTid("tid")
                .templateObjectType("testObject")
                .templateName("testName")
                .templateAttributes(properties)
                .build();

        assertEquals(emailRequest.getEmailStrategyType(), EmailStrategyType.OINP);
        assertEquals(emailRequest.getIntuitTid(), "tid");
        assertEquals(emailRequest.getTemplateName(), "testName");
        assertEquals(emailRequest.getTemplateObjectType(),"testObject");
        assertEquals(emailRequest.getTemplateAttributes(), properties);

    }
}