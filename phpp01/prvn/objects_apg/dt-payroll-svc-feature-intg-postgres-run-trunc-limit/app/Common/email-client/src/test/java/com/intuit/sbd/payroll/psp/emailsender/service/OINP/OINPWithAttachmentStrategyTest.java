package com.intuit.sbd.payroll.psp.emailsender.service.OINP;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailStrategyType;
import com.intuit.sbd.payroll.psp.emailsender.gateway.DocumentService.DocumentServiceGateway;
import com.intuit.sbd.payroll.psp.emailsender.gateway.OINPRestServiceGateway;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OINPWithAttachmentStrategyTest {

    @Mock
    private OINPRestServiceGateway oinpRestServiceGateway;

    @Mock
    private DocumentServiceGateway documentServiceGateway;

    @Mock
    private EmailRequestValidator emailRequestValidator;

    @Mock
    private OINPRequestUtility oinpRequestUtility;

    @InjectMocks
    private OINPWithAttachmentsStrategy oinpWithAttachmentsStrategy;


    @Before
    public void setUp() {
        oinpWithAttachmentsStrategy = new OINPWithAttachmentsStrategy(documentServiceGateway,emailRequestValidator,oinpRequestUtility, oinpRestServiceGateway);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateSendEmailWithAttachmentsTest() throws Exception {
        String testURI="test3.jpg";
        String testdocID="IntuitDocID";
        String testSourceKey="IntuitSourceKey";
        Map<String,String> testMap=new HashMap<>();
        testMap.put(testdocID,testSourceKey);
        List<String> testattachments=new ArrayList<>(); testattachments.add(testURI);
        EmailRequest request = EmailRequest.builder()
                .emailStrategyType(EmailStrategyType.OINPWithAttachments).attachmentList(testattachments)
                .build();
        when(documentServiceGateway.uploadDocument(testURI)).thenReturn(testdocID);
        when(documentServiceGateway.getSourceKey(testdocID)).thenReturn(testSourceKey);

        EmailResponse response = mock(EmailResponse.class);

        when(oinpWithAttachmentsStrategy.process(request)).thenReturn(response);

        when(response.getStatus()).thenReturn(HttpStatus.SC_OK);
        when(response.getResponseBody()).thenReturn("OK");

        Map<String, String> headers = Mockito.mock(HashMap.class);

        when(response.getHttpServiceResponseHeaders()).thenReturn(headers);
        when(headers.get("intuit_tid")).thenReturn("tid");

        EmailResponse response1 = oinpWithAttachmentsStrategy.sendMail(request);
        verify(documentServiceGateway).uploadDocument(testURI);
        verify(documentServiceGateway).getSourceKey(testdocID);

        assertEquals(request.getDocumentMetaDataMap(),testMap);
        assertEquals(response.getStatus(), response1.getStatus());
        assertEquals(response.getResponseBody(), response1.getResponseBody());
    }
}
