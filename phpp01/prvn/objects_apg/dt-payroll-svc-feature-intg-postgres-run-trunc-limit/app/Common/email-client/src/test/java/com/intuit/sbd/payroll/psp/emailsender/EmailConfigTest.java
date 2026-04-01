package com.intuit.sbd.payroll.psp.emailsender;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailSettings;
import com.intuit.sbd.payroll.psp.emailsender.service.EmailSendStrategyAbstract;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class EmailConfigTest {

    @InjectMocks
    EmailSendStrategyAbstract emailSendStrategyAbstract;

    @Mock
    EmailConfig emailConfig;

    @Mock
    EmailSettings emailSettings;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this); //This could be pulled up into a shared base class
    }

    @Test
    public void validateGetUrl() {
        EmailConfig emailConfig = mock(EmailConfig.class);
        EmailSettings emailSettings = mock(EmailSettings.class);
        //doReturn(String).when(emailSendStrategyAbstract.)
    }

//    @Test
//    public void validateHystrixAspect() {
//        emailConfig = mock(EmailConfig.class);
//        HystrixCommandAspect hystrixCommandAspect = mock(HystrixCommandAspect.class);
//        when(emailConfig.hystrixAspect()).thenReturn(hystrixCommandAspect);
//        verify(emailConfig,times(0)).hystrixAspect();
//    }
//
//    @Test
//    public void validateThreadPoolTaskExecutor(){
//        emailConfig = mock(EmailConfig.class);
//        ThreadPoolTaskExecutor threadPoolTaskExecutor = mock(ThreadPoolTaskExecutor.class);
//        when(emailConfig.threadPoolTaskExecutor()).thenReturn(threadPoolTaskExecutor);
//        verify(emailConfig, times(0)).threadPoolTaskExecutor();
//    }
//
//    @Test
//    public void validateJerseyClient() {
//        emailConfig = mock(EmailConfig.class);
//        Client client = mock(Client.class);
//        when(emailConfig.client()).thenReturn(client);
//        verify(emailConfig,times(0)).client();
//    }


}
