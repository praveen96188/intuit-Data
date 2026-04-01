package com.intuit.sbd.payroll.psp.adapters.mobile.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSResponse;
import com.intuit.sbd.payroll.psp.adapters.mobile.factories.MobileFactory;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.CompanyEventFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.processes.AuthenticationProcess;
import com.intuit.sbd.payroll.psp.adapters.mobile.utils.DemoData;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Jeff Jones
 */
@Path("/events")
public class EventsWS {

    private static final SpcfLogger logger = PayrollServices.getLogger(EventsWS.class);

    @GET
    @Path("/{days}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getEvents(@HeaderParam("username")String pEIN,
                                   @HeaderParam("password")String pPIN,
                                   @PathParam("days")int pDays) {
        RSResponse rsResponse = new RSResponse();
        try {
            if (pEIN == null || pPIN == null) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            if ("DEMO".equalsIgnoreCase(pEIN) && "DEMO".equalsIgnoreCase(pPIN)) {
                rsResponse.getEvents().addAll(DemoData.getEvents(pDays));
            } else {
                PayrollServices.beginUnitOfWork();

                AuthenticationProcess authProcess = new AuthenticationProcess(pEIN, pPIN);
                authProcess.execute();

                Company company = authProcess.getCompany();

                DomainEntitySet<CompanyEvent> companyEvents = CompanyEventFinder.findCompanyEvents(company, pDays);
                
                for (CompanyEvent companyEvent : companyEvents) {
                    rsResponse.getEvents().add(MobileFactory.createRSEvent(companyEvent));
                }
            }
        } catch (WebApplicationException wae) {
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

    @GET
    @Path("/{days}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getEvents(@HeaderParam("username")String pEIN,
                                   @HeaderParam("password")String pPIN,
                                   @QueryParam("start") int pStart,
                                   @QueryParam("size") int pSize) {
        RSResponse rsResponse = new RSResponse();
        try {
            if (pEIN == null || pPIN == null) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            if ("DEMO".equalsIgnoreCase(pEIN) && "DEMO".equalsIgnoreCase(pPIN)) {
                //rsResponse.getEvents().addAll(DemoData.getEvents(pDays));
            } else {
                PayrollServices.beginUnitOfWork();

                AuthenticationProcess authProcess = new AuthenticationProcess(pEIN, pPIN);
                authProcess.execute();

                Company company = authProcess.getCompany();

                DomainEntitySet<CompanyEvent> companyEvents = CompanyEventFinder.findCompanyEvents(company, pStart, pSize);

                for (CompanyEvent companyEvent : companyEvents) {
                    rsResponse.getEvents().add(MobileFactory.createRSEvent(companyEvent));
                }
            }
        } catch (WebApplicationException wae) {
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
