package com.intuit.sbd.payroll.psp.emailsender.gateway;

import com.intuit.sbd.payroll.psp.emailsender.EmailAuthorizationManager;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailRequest;
import com.intuit.sbd.payroll.psp.emailsender.domain.EmailResponse;
import com.intuit.sbd.payroll.psp.emailsender.domain.OINPEmailSettings;
import com.intuit.sbd.payroll.psp.emailsender.exception.OINPServicesException;
import com.intuit.sbd.payroll.psp.emailsender.model.OINP.OINPEventRequest;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import com.intuit.sbg.psp.webserviceclient.support.json.JsonConverter;
import org.apache.commons.lang3.Validate;
import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to publish events to OINP
 *
 * @author nramesh1
 */

@Component
public class OINPRestServiceGateway {

    private final Logger logger = LoggerFactory.getLogger(OINPRestServiceGateway.class);

    private final HttpServiceClient httpServiceClient;
    private final OINPEmailSettings oinpEmailSettings;
    private final JsonConverter jsonConverter;
    private final EmailAuthorizationManager authorizationManager;

    @Autowired
    public OINPRestServiceGateway(HttpServiceClient httpServiceClient, OINPEmailSettings oinpEmailSettings, JsonConverter jsonConverter, EmailAuthorizationManager authorizationManager)
    {
        this.httpServiceClient = httpServiceClient;
        this.oinpEmailSettings = oinpEmailSettings;
        this.jsonConverter = jsonConverter;
        this.authorizationManager = authorizationManager;

    }

    public EmailResponse publishEventToOINP(OINPEventRequest event) {

        try {
            authorizationManager.setAuthorizationContext();

            Validate.notNull(event, "OINP emailEvent cannot be null");

            String oinpEventString = jsonConverter.serialize(event);

            String fullUrl = oinpEmailSettings.getOinpEventsApiEndpoint();

            logger.info("OINP: Publishing event with intuit_tid:" + event.getEventMetaData().getIntuitTid() + ", objectId:" + event.getSourceObjectId());

            return publishEmail(event.getSourceObjectType(), oinpEventString, fullUrl, getHeaders(event));
        } catch (Exception e) {
            throw e;
        } finally {
            authorizationManager.removeAuthorizationContext();
        }
    }

    public EmailResponse publishEmail(String templateObjectType, String requestString, String oinpEndpoint, Map<String,String> headers) throws OINPServicesException {

        HttpServiceResponse httpServiceResponse = httpServiceClient.post(oinpEndpoint, requestString, headers);

        if(httpServiceResponse.isSuccessful()) {

            logger.info("OINP: Successfully published event: " + templateObjectType);
            EmailResponse response = new EmailResponse(httpServiceResponse);
            return response;
        } else {
            logger.error("OINP: Event Failure - Response status: {}, Response body: {}", httpServiceResponse.getStatusCode(), httpServiceResponse.getBody());
        }

        throw new OINPServicesException(String.format("OINP: Unable to publish email event/s to OINP for object Type %s", templateObjectType), httpServiceResponse);
    }

    public Map<String,String> getHeaders(OINPEventRequest request){
        Map<String,String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put("intuit_tid", request.getEventMetaData().getIntuitTid());
        return headers;

    }
}
