package com.intuit.sbd.payroll.psp.adapters.mobile.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSPayee;
import com.intuit.sbd.payroll.psp.adapters.mobile.factories.MobileFactory;
import com.intuit.sbd.payroll.psp.adapters.mobile.processes.AuthenticationProcess;
import com.intuit.sbd.payroll.psp.adapters.mobile.utils.DemoData;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Jeff Jones
 */

@Path("/payee")
public class PayeeWS {

    private static final SpcfLogger logger = PayrollServices.getLogger(PayeeWS.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPayee(@HeaderParam("username")String pEIN,
                                  @HeaderParam("password")String pPIN,
                                  @QueryParam("id")String pId) {
        RSPayee rsPayee = new RSPayee();
        try {
            if (pEIN == null || pPIN == null) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            if ("DEMO".equalsIgnoreCase(pEIN) && "DEMO".equalsIgnoreCase(pPIN)) {
                rsPayee = DemoData.getPayee(pId);
            } else {
                PayrollServices.beginUnitOfWork();

                AuthenticationProcess authProcess = new AuthenticationProcess(pEIN, pPIN);
                authProcess.execute();
            }

            Employee employee = Application.findById(Employee.class, SpcfUniqueId.createInstance(pId));
            if (employee != null) {
                rsPayee = MobileFactory.createRSPayeeWithDetail(employee);
            } else {
                Payee payee = Application.findById(Payee.class, SpcfUniqueId.createInstance(pId));
                if (payee != null) {
                     rsPayee = MobileFactory.createRSPayeeWithDetail(payee);
                } else {
                     throw new WebApplicationException(Response.Status.NOT_FOUND);
                }
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
        return gson.toJson(rsPayee);
    }
}
