package com.intuit.sbd.payroll.psp.jss.resources;

import com.intuit.sbd.payroll.psp.jss.util.WebhookForwarder;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.PathParam;

@Path("/argo")
public class ArgoEventsResource {
    private static final Logger logger = LoggerFactory.getLogger(ArgoEventsResource.class);

    private WebhookForwarder webhookForwarder;

    public ArgoEventsResource() {
        this.webhookForwarder = PayrollApplicationBeanFactory.getBean(WebhookForwarder.class);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{eventName}")
    public Response forwardRequest(@Context HttpServletRequest httpServletRequest, ArgoEventRequest argoEventRequest,
                                   @PathParam("eventName") String eventName) {

        logger.info("Starting processing for {}", argoEventRequest.getFileName());

        Boolean authenticationSuccessful = webhookForwarder.authenticateRequest(httpServletRequest);

        if (!authenticationSuccessful) {
            logger.error("Authentication failed for request {}", argoEventRequest.getFileName());
            return Response.serverError().entity("Authentication failure").build();
        }

        logger.info("Authentication Successful");
        HttpServiceResponse result = webhookForwarder.forwardRequestToWebhook(argoEventRequest, eventName);
        if (!result.isSuccessful()) {
            if (result.isServerErrors()) {
                logger.info("Could not Complete processing for {}. Check logs for any errors", argoEventRequest.getFileName());
                return Response.serverError().entity(result.getMessage()).build();
            } else if (result.isClientErrors()) {
                logger.info("Could not Complete processing for {}. Check logs for any errors", argoEventRequest.getFileName());
                return Response.status(Response.Status.BAD_REQUEST).entity(result.getMessage()).build();
            } else {
                logger.info("Could not Complete processing for {}. Check logs for any errors", argoEventRequest.getFileName());
                return Response.status(result.getStatusCode()).entity(result.getMessage()).build();
            }
        }

        logger.info("Completed processing for {} succesfully", argoEventRequest.getFileName());
        return Response.ok().build();
    }
}
