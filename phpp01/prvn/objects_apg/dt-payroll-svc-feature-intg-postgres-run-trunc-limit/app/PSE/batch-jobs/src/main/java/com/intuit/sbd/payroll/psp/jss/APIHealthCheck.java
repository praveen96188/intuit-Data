package com.intuit.sbd.payroll.psp.jss;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.Session;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Created with IntelliJ IDEA.
 * User: ssaxena2
 * Date: 4/24/17
 * Time: 11:01 PM
 * To change this template use File | Settings | File Templates.
 *
 */
@Path("/v1")
@Api(value="Health Check")
public class APIHealthCheck {

    private static final String GOOD_HEALTH="Health Check Ok";
    private static final String DB_DISCONNECT="DB_Disconnect";

    /**
     * This will just check if our service is up and running
     * @return
     */
    @GET
    @Path("/health")
    @ApiOperation(value="Half health check")
    @Consumes(MediaType.TEXT_PLAIN)
    public String checkHealth(){
        return GOOD_HEALTH;
    }


    /**
     * This will check full health of server, so we are checking DB connection too.
     * @return
     */
    @GET
    @Path("/health/full")
    @Consumes(MediaType.TEXT_PLAIN)
    @ApiOperation(value="Full health check")
    public String checkHealthFull(){
        PayrollServices.beginUnitOfWork();
        Application.initialize();
        ApplicationSecondary.initialize();
        if(Application.testDBConnect()) {
            PayrollServices.commitUnitOfWork();
            return GOOD_HEALTH;
        }
        else
            return  DB_DISCONNECT;
    }
}
