package com.intuit.sbd.payroll.psp.emailsender.service.OINP;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.OINPKafkaResponse;

import com.intuit.sbd.payroll.psp.emailsender.gateway.OINPKafkaServiceGateway;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPKafkaRequest;
import com.intuit.sbd.payroll.psp.emailsender.validator.EmailRequestValidator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class OINPBulkKafkaStrategyTest {

    @Mock
    private OINPKafkaServiceGateway oinpKafkaServiceGateway;

    @Mock
    private EmailRequestValidator emailRequestValidator;

    @Mock
    private OINPRequestUtility oinpRequestUtility;

    @InjectMocks
    private OINPBulkKafkaStrategy oinpStrategy;

    @Before
    public void setUp() {
        oinpStrategy = new OINPBulkKafkaStrategy(emailRequestValidator,oinpRequestUtility, oinpKafkaServiceGateway);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void validateSendEmailTest() throws Exception {

        EmailRequest emailRequest = mock(EmailRequest.class);
        OINPKafkaResponse response1 = mock(OINPKafkaResponse.class);

        when(oinpKafkaServiceGateway.publishEventViaKafka(any(OINPKafkaRequest.class))).thenReturn(response1);

        OINPKafkaResponse response2 = oinpStrategy.process(emailRequest);
        verify(oinpKafkaServiceGateway).publishEventViaKafka(any(OINPKafkaRequest.class));

    }

}
