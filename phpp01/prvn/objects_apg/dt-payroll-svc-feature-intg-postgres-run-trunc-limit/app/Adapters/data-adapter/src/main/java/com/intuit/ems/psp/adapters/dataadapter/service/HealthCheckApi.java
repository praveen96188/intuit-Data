package com.intuit.ems.psp.adapters.dataadapter.service;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.hibernate.Session;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;


/**
 * Created by vdammur1 on 5/23/2017.dom
 */

@Path("/v1")
public class HealthCheckApi {

    private static SpcfLogger logger = Application.getLogger(HealthCheckApi.class);

        private static final String GOOD_HEALTH = "Health Check Ok";
        private static final String DB_DISCONNECT = "DB_Disconnect";

        /**
         * This will just check if our service is up and running
         * @return
         */
        @GET
        @Path("/health")
        @Consumes(MediaType.TEXT_PLAIN)
        public String checkHealth () {
            return GOOD_HEALTH;
        }

        /**
         * This will check full health of server, so we are checking DB connection too.
         * @return
         */
        @GET
        @Path("/health/full")
        @Consumes(MediaType.TEXT_PLAIN)
        public String checkHealthFull () {
            PayrollServices.beginUnitOfWork();
            Application.initialize();
            ApplicationSecondary.initialize();
            if(Application.testDBConnect()){
                PayrollServices.commitUnitOfWork();
                return GOOD_HEALTH;
            }
            else
                return DB_DISCONNECT;
        }
}
