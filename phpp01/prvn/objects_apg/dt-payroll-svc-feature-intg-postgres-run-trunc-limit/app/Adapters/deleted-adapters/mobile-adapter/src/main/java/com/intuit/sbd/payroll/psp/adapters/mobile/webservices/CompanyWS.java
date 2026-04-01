package com.intuit.sbd.payroll.psp.adapters.mobile.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSCompany;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSResponse;
import com.intuit.sbd.payroll.psp.adapters.mobile.factories.MobileFactory;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.CompanyEventFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.PayrollRunFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.processes.AuthenticationProcess;
import com.intuit.sbd.payroll.psp.adapters.mobile.utils.DemoData;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyEvent;
import com.intuit.sbd.payroll.psp.domain.PayrollRun;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 @author Jeff Jones
 */

@Path("/company")
public class CompanyWS {

    private static final SpcfLogger logger = PayrollServices.getLogger(CompanyWS.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getCompany(@HeaderParam("username")String pEIN,
                             @HeaderParam("password")String pPIN) {
        RSResponse rsResponse = null;

        try {
            if (pEIN == null || pPIN == null) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            if ("DEMO".equalsIgnoreCase(pEIN) && "DEMO".equalsIgnoreCase(pPIN)) {
                //rsResponse = DemoData.getCompanyResponse();
            } else {
                rsResponse = new RSResponse();

                PayrollServices.beginUnitOfWork();

                AuthenticationProcess authProcess = new AuthenticationProcess(pEIN, pPIN);
                authProcess.execute();

                Company company = authProcess.getCompany();

                DomainEntitySet<PayrollRun> payrollRuns = PayrollRunFinder.findPayrollRuns(company, 15);
                rsResponse.setRecentTransmissionCount(payrollRuns.size());
                DomainEntitySet<CompanyEvent> companyEvents = CompanyEventFinder.findCompanyEvents(company, 15);
                rsResponse.setRecentEventCount(companyEvents.size());

                rsResponse.setCompany(MobileFactory.createRSCompany(authProcess.getCompany()));
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
