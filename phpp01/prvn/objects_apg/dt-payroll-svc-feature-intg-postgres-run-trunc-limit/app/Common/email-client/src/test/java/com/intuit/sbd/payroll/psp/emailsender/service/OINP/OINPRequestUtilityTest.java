package com.intuit.sbd.payroll.psp.emailsender.service.OINP;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPEventRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OINPRequestUtilityTest {

    @InjectMocks
    private OINPRequestUtility oinpRequestUtility;

    @Test
    public void validateCreateOINPRequestTest() {

        String testName = "testName";
        String testObjectType = "testObject";
        String testIntuitTid = "-1";
        EmailRequest emailRequest = mock(EmailRequest.class);

        Map<String,Object> attribute = new HashMap<>();
        when(emailRequest.getIntuitTid()).thenReturn(testIntuitTid);
        when(emailRequest.getTemplateAttributes()).thenReturn(attribute);
        when(emailRequest.getTemplateObjectType()).thenReturn(testObjectType);
        when(emailRequest.getTemplateName()).thenReturn(testName);

        OINPEventRequest eventRequest = oinpRequestUtility.createOINPRequest(emailRequest);

        assertEquals(eventRequest.getEventMetaData().getIntuitTid(), testIntuitTid);
        assertEquals(eventRequest.getEventData(), attribute);
        assertEquals(eventRequest.getSourceObjectType(), testObjectType);
        assertEquals(eventRequest.getName(), testName);

    }
}
