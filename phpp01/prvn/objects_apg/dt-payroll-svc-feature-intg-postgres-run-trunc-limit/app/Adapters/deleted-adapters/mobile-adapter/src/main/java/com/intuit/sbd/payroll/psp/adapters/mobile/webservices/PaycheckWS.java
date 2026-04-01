package com.intuit.sbd.payroll.psp.adapters.mobile.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSResponse;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSTransmission;
import com.intuit.sbd.payroll.psp.adapters.mobile.factories.MobileFactory;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.CompanyEventFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.PayrollRunFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.processes.AuthenticationProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;

/**
 * @author Jeff Jones
 */
public class PaycheckWS {

    private static final SpcfLogger logger = PayrollServices.getLogger(PaycheckWS.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPaycheck(@HeaderParam("username")String pEIN,
                               @HeaderParam("password")String pPIN,
                               @PathParam("id")String pPaycheckId) {
        RSResponse rsResponse = new RSResponse();
        try {
            if (pEIN == null || pPIN == null) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            if ("DEMO".equalsIgnoreCase(pEIN) && "DEMO".equalsIgnoreCase(pPIN)) {
                //rsResponse.getTransmissions().addAll(DemoData.getTransmissions(pDays));
            } else {
                PayrollServices.beginUnitOfWork();

                AuthenticationProcess authProcess = new AuthenticationProcess(pEIN, pPIN);
                authProcess.execute();

                Company company = authProcess.getCompany();

                Paycheck paycheck = Application.findById(Paycheck.class, SpcfUniqueId.createInstance(pPaycheckId));

                if (paycheck == null) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }

                rsResponse.getPaychecks().add(MobileFactory.createRSPaycheck(paycheck));
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
