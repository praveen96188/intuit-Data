package com.intuit.sbd.payroll.psp.emailsender.gateway;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OINPRestServiceGatewayTest {

    @InjectMocks
    OINPRestServiceGateway oinpRestServiceGateway;

    @Mock
    HttpServiceClient httpServiceClient;


    @Test
    public void validatePublishEmailTest() {

        String endpoint = "testEndpoint";
        String request = "testRequest";
        Map<String, String> reqHeaders = new HashMap<>();

        HttpServiceResponse response = mock(HttpServiceResponse.class);

        when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(response.getMessage()).thenReturn("OK");
        when(response.isSuccessful()).thenReturn(true);
        Map<String, String> headers = Mockito.mock(HashMap.class);
        when(response.getHeaders()).thenReturn(headers);
        when(headers.get("intuit_tid")).thenReturn("tid");
        when(httpServiceClient.post(endpoint, request, reqHeaders)).thenReturn(response);
        EmailResponse response1 = oinpRestServiceGateway.publishEmail("testObject", request, endpoint, headers);
        assertEquals(response.getStatusCode(), response1.getStatus());
        assertEquals(response.getMessage(), response1.getResponseBody());

    }
}
