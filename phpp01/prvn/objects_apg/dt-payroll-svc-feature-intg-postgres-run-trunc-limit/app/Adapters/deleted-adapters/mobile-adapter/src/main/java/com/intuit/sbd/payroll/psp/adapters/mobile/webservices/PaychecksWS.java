package com.intuit.sbd.payroll.psp.adapters.mobile.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSResponse;
import com.intuit.sbd.payroll.psp.adapters.mobile.factories.MobileFactory;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.MobileFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.processes.AuthenticationProcess;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Paycheck;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Jeff Jones
 */
public class PaychecksWS {

    private static final SpcfLogger logger = PayrollServices.getLogger(PaychecksWS.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPaychecks(@HeaderParam("username")String pEIN,
                               @HeaderParam("password")String pPIN,
                               @PathParam("id")String pEmployeeId,
                               @QueryParam("start") int pStart,
                               @QueryParam("size") int pSize) {
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

                Employee employee = Application.findById(Employee.class, SpcfUniqueId.createInstance(pEmployeeId));

                if (employee == null) {
                    throw new WebApplicationException(Response.Status.NOT_FOUND);
                }

                DomainEntitySet<Paycheck> paychecks = MobileFinder.findPaychecksByEmployee(company, employee, pStart, pSize);
                for (Paycheck paycheck : paychecks) {
                    rsResponse.getPaychecks().add(MobileFactory.createRSPaycheck(paycheck));
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
        return gson.toJson(rsResponse);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPaychecks(@HeaderParam("username")String pEIN,
                               @HeaderParam("password")String pPIN,
                               //@PathParam("payroll")String pPayrollId,
                               @QueryParam("start") int pStart,
                               @QueryParam("size") int pSize) {
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

                Paycheck paycheck = Application.findById(Paycheck.class, SpcfUniqueId.createInstance("")); //pPaycheckId));

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
