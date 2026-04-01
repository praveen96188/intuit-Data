package com.intuit.sbd.payroll.psp.emailsender.processor;

import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.exception.EmailServiceException;
import com.intuit.sbd.payroll.psp.emailsender.model.ExactTargetResults;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.commons.httpclient.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class is to process the email request response
 *
 * @author vishalb849
 */
@Component
public class EmailResponseProcessor {

    private final Logger logger = LoggerFactory.getLogger(EmailResponseProcessor.class);

    private static List<Integer> SUCCESS_HTTP_STATUSES = Arrays.asList(HttpStatus.SC_OK, HttpStatus.SC_NO_CONTENT, HttpStatus.SC_CREATED);

    public EmailResponse processResponse(ClientResponse response) throws EmailServiceException {

        if (Objects.isNull(response)) {
            throw new EmailServiceException("TxE Service is not responding to requests. A request \" +\n" +
                    "\"was made to send email, but a null response was received");
        }


        if(!SUCCESS_HTTP_STATUSES.contains(response.getStatus())){
            throw new EmailServiceException("Error ocurred during response processing: "+response.getStatus());

        }

        return getEmailResponse(response);
    }

    public EmailResponse getEmailResponse(ClientResponse clientResponse) {

        EmailResponse emailResponse = new EmailResponse(clientResponse);
        return emailResponse;
    }

    public EmailResponse getEmailResponse(ClientResponse clientResponse, EmailRequest emailRequest) {

        EmailResponse emailResponse = new EmailResponse(clientResponse);

        Boolean success = false;
        if(emailResponse.getStatus()== HttpStatus.SC_OK){
            success = true;
        }
        ExactTargetResults results = new ExactTargetResults(emailRequest.getSendRequest().getNotification().getDestinations().getDestination(),success);
        emailResponse.setResults(results);

        return emailResponse;
    }



}
