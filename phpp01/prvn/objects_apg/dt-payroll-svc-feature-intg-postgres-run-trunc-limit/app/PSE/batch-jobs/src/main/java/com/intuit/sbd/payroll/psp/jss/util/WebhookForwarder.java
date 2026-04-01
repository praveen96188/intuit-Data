package com.intuit.sbd.payroll.psp.jss.util;

import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.jss.resources.ArgoEventRequest;
import com.intuit.sbd.payroll.psp.jss.resources.WebHookAuthenticationContext;
import com.intuit.sbd.payroll.psp.jss.util.strategies.SourceIPBasedAuthenticationStrategy;
import com.intuit.sbd.payroll.psp.jss.util.strategies.WebHookAuthenticationStrategy;
import com.intuit.sbd.payroll.psp.mapper.jackson.CustomObjectMapperResolver;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

@Service
public class WebhookForwarder {
    private static final Logger logger = LoggerFactory.getLogger(WebhookForwarder.class);
    private HttpServiceClient httpServiceClient;

    @Autowired
    public WebhookForwarder(HttpServiceClient httpServiceClient) {
        this.httpServiceClient = httpServiceClient;
    }

    private WebHookAuthenticationContext authenticationContext;

    public Boolean authenticateRequest(HttpServletRequest httpServletRequest) {
        WebHookAuthenticationStrategy authenticationStrategy = new SourceIPBasedAuthenticationStrategy();
        this.authenticationContext = new WebHookAuthenticationContext(authenticationStrategy);
        return authenticationContext.authenticate(httpServletRequest);
    }

    public HttpServiceResponse forwardRequestToWebhook(ArgoEventRequest argoEventRequest, String webhookEventName) {
        if(StringUtils.isEmpty(webhookEventName))
            throw new IllegalArgumentException("WebhookEventName is null or empty");

        logger.info("Forwarding request : {}", argoEventRequest.getFileName());
        CustomObjectMapperResolver customObjectMapperResolver = new CustomObjectMapperResolver();
        String payload = customObjectMapperResolver.serialize(argoEventRequest);
        logger.info("Request : {}", payload);
        return this.httpServiceClient.post(getEventSourceServiceUrl() + webhookEventName, payload, getCommonHeaders());
    }

    private Map<String, String> getCommonHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
        headers.put(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getEventSourceServiceUrl() {
        return ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_argo_event_source_service_url");
    }
}
