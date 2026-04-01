package com.intuit.sbd.payroll.psp.adapters.mobile.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.mobile.factories.MobileFactory;
import com.intuit.sbd.payroll.psp.adapters.mobile.processes.AuthenticationProcess;
import com.intuit.sbd.payroll.psp.adapters.mobile.utils.DemoData;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 @author Jeff Jones
 */

@Path("/transmission")
public class TransmissionWS {

    private static final SpcfLogger logger = PayrollServices.getLogger(TransmissionWS.class);

    @GET
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getTransmission(@HeaderParam("username")String pEIN,
                                  @HeaderParam("password")String pPIN,
                                  @PathParam("id")String pId) {
        RSTransmission rsTransmission = new RSTransmission();
        try {
            if (pEIN == null || pPIN == null) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            if ("DEMO".equalsIgnoreCase(pEIN) && "DEMO".equalsIgnoreCase(pPIN)) {
                rsTransmission = DemoData.getTransmission(pId);
            } else {
                PayrollServices.beginUnitOfWork();

                AuthenticationProcess authProcess = new AuthenticationProcess(pEIN, pPIN);
                authProcess.execute();

                PayrollRun payrollRun = Application.findById(PayrollRun.class, SpcfUniqueId.createInstance(pId));
                if (payrollRun == null) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }

                rsTransmission = MobileFactory.createRSTransmissionWithDetails(payrollRun);
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
        return gson.toJson(rsTransmission);
    }
}
