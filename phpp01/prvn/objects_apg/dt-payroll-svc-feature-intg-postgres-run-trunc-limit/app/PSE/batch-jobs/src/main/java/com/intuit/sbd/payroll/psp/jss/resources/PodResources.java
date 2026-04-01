package com.intuit.sbd.payroll.psp.jss.resources;

import com.intuit.sbd.payroll.psp.jss.processors.podcleanup.PodCleanupProcess;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author ssharma17
 */
@Slf4j
@Path("/v1/pod")
@Api(value="Jss Pod cleanup API")
public class PodResources {
    public static Logger LOGGER = LoggerFactory.getLogger(PodResources.class);

    PodCleanupProcess podCleanupProcess;

    public PodResources() {
        podCleanupProcess = PayrollApplicationBeanFactory.getBean(PodCleanupProcess.class);
    }

    /**
     * Used to perform cleanup activities for podName provided
     * @param pod
     * @return
     */
    @DELETE
    @Path("/{pod}")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(value="Jss Pod cleanup")
    public Response cleanupPod(@PathParam("pod") String pod) {
        try {
            podCleanupProcess.cleanupPod(pod);
            return Response.ok("Done",MediaType.TEXT_PLAIN).build();
        } catch (HttpClientErrorException e) {
            log.error("podCleanUpProcess threw an HttpClientErrorException message={}",e.getMessage(),e);
            return Response.status(HttpStatus.BAD_REQUEST.value()).build();
        } catch (Exception e) {
            log.error("podCleanUpProcess threw an error, message={}",e.getMessage(),e);
            return Response.status(HttpStatus.INTERNAL_SERVER_ERROR.value()).build();
        }
    }
}
