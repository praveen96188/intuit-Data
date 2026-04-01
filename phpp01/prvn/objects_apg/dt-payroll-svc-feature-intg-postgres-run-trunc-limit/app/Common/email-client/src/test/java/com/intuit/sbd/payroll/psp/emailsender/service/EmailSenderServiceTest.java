package com.intuit.sbd.payroll.psp.emailsender.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class EmailSenderServiceTest {

    private EmailSenderService emailSenderService;

    @InjectMocks
    List<EmailSendStrategy> emailSenderServices;
}