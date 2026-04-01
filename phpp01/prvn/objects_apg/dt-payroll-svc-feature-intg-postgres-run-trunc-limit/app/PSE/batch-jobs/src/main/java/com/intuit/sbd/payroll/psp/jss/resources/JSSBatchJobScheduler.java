package com.intuit.sbd.payroll.psp.jss.resources;


import java.util.Arrays;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intuit.sbd.payroll.psp.jss.JSSBatchJobManager;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

@Path("/jss")
public class JSSBatchJobScheduler{

    private static final Logger logger = LoggerFactory.getLogger(JSSBatchJobScheduler.class);

  @POST
   @Path("/schedule")
  @Consumes(MediaType.APPLICATION_JSON)
   public Response scheduleJob(String requestParam){
   	String jobId = null;
  	ResponseBuilder response = null;
   	JSSBatchJobSchedulerRequest requestJson = null;
   	ObjectMapper objectMapper = new ObjectMapper();
		try {
			requestJson = objectMapper.readValue(requestParam, JSSBatchJobSchedulerRequest.class);
			logger.info("Run job " + requestJson.getJobname() + " " + Arrays.toString(requestJson.getArg()));
			jobId = JSSBatchJobManager.scheduleJobWithTime(requestJson.getJobname(), requestJson.getTimerExpression(), requestJson.getArg());
			logger.info("Job Id " + jobId);
			response=Response.status(Status.OK).entity(jobId);
		} catch (Exception e) {
				response= Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage());  
		}
		return response.build();
   }
   
   
}
