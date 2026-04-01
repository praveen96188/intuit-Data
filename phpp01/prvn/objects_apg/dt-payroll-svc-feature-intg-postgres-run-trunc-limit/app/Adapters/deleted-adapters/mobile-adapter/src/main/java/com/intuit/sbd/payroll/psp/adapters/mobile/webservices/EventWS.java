package com.intuit.sbd.payroll.psp.adapters.mobile.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSEvent;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSPayee;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSResponse;
import com.intuit.sbd.payroll.psp.adapters.mobile.factories.MobileFactory;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.CompanyEventFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.processes.AuthenticationProcess;
import com.intuit.sbd.payroll.psp.adapters.mobile.utils.DemoData;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 @author Jeff Jones
 */
@Path("/event")
public class EventWS {

    private static final SpcfLogger logger = PayrollServices.getLogger(EventWS.class);

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public static String getEvent(@HeaderParam("username")String pEIN,
                                  @HeaderParam("password")String pPIN,
                                  @PathParam("id")String pId) {
        RSEvent rsResponse;
        try {
            if (pEIN == null || pPIN == null) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            if ("DEMO".equalsIgnoreCase(pEIN) && "DEMO".equalsIgnoreCase(pPIN)) {
                rsResponse = DemoData.getEvent(pId);
            } else {
                PayrollServices.beginUnitOfWork();

                AuthenticationProcess authProcess = new AuthenticationProcess(pEIN, pPIN);
                authProcess.execute();

                rsResponse = MobileFactory.createRSEventWithDetail(CompanyEventFinder.findCompanyEvent(pId));
            }
        } catch (WebApplicationException wae) {
            logger.info(wae);
            throw wae;
        } catch (Exception e) {
            logger.warn(e);
            throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(rsResponse);
    }

}
