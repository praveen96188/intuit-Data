package com.intuit.sbd.payroll.psp.adapters.mobile.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSPayee;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSResponse;
import com.intuit.sbd.payroll.psp.adapters.mobile.factories.MobileFactory;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.EmployeeFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.processes.AuthenticationProcess;
import com.intuit.sbd.payroll.psp.adapters.mobile.utils.DemoData;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.Employee;
import com.intuit.sbd.payroll.psp.domain.Individual;
import com.intuit.sbd.payroll.psp.domain.Payee;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * @author Jeff Jones
 */
@Path("/payees")
public class PayeesWS {

    private static final SpcfLogger logger = PayrollServices.getLogger(PayeesWS.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getPayees(@HeaderParam("username")String pEIN,
                                   @HeaderParam("password")String pPIN) {
        RSResponse rsResponse = new RSResponse();

        try {
            if (pEIN == null || pPIN == null) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            if ("DEMO".equalsIgnoreCase(pEIN) && "DEMO".equalsIgnoreCase(pPIN)) {
                //get demo data
            } else {
                PayrollServices.beginUnitOfWork();

                AuthenticationProcess authProcess = new AuthenticationProcess(pEIN, pPIN);
                authProcess.execute();

                Company company = authProcess.getCompany();
                DomainEntitySet<Payee> payees = Payee.findPayees(company);

/*                for (Payee payee : payees) {
                    rsResponse.getPayees().add(MobileFactory.createRSPayee(payee));
                }*/

                DomainEntitySet<Employee> employees = EmployeeFinder.findEmployees(company);
                for (Employee employee : employees) {
                    String key = employee.getLastName().substring(0,1);
                    List<RSPayee> rsPayees = rsResponse.getPayees().get(key.toUpperCase());
                    if (rsPayees != null) {
                        rsPayees.add(MobileFactory.createRSPayee(employee));
                    } else {
                        rsPayees = rsResponse.getPayees().get("#");
                        rsPayees.add(MobileFactory.createRSPayee(employee));
                    }
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

}
