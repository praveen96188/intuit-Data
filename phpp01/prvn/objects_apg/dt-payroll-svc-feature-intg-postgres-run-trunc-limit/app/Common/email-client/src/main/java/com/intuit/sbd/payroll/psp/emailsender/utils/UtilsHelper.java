package com.intuit.sbd.payroll.psp.emailsender.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;

public class UtilsHelper {


    public String getJsonString(EmailRequest emailRequest) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        String emailRequestString;
        emailRequestString = mapper.writeValueAsString(emailRequest.getSendRequest());
        return emailRequestString;
    }

}
