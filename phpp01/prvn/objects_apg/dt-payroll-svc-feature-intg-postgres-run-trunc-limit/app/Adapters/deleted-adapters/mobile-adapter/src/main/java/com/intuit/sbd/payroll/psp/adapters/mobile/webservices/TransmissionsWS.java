package com.intuit.sbd.payroll.psp.adapters.mobile.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSResponse;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSTransmission;
import com.intuit.sbd.payroll.psp.adapters.mobile.factories.MobileFactory;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.CompanyEventFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.finders.PayrollRunFinder;
import com.intuit.sbd.payroll.psp.adapters.mobile.processes.AuthenticationProcess;
import com.intuit.sbd.payroll.psp.adapters.mobile.utils.DemoData;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;

/**
 * @author Jeff Jones
 */

@Path("/transmissions")
public class TransmissionsWS {

    private static final SpcfLogger logger = PayrollServices.getLogger(TransmissionsWS.class);

    @GET
    @Path("/{days}")
    @Produces(MediaType.TEXT_PLAIN)
    public String getTransmissions(@HeaderParam("username")String pEIN,
                                   @HeaderParam("password")String pPIN,
                                   @PathParam("days")int pDays) {
        RSResponse rsResponse = new RSResponse();
        try {
            if (pEIN == null || pPIN == null) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }

            if ("DEMO".equalsIgnoreCase(pEIN) && "DEMO".equalsIgnoreCase(pPIN)) {
                rsResponse.getTransmissions().addAll(DemoData.getTransmissions(pDays));
            } else {
                PayrollServices.beginUnitOfWork();

                AuthenticationProcess authProcess = new AuthenticationProcess(pEIN, pPIN);
                authProcess.execute();

                Company company = authProcess.getCompany();

                DomainEntitySet<PayrollRun> payrollRuns = PayrollRunFinder.findPayrollRuns(company, pDays);
                for (PayrollRun payrollRun : payrollRuns) {
                    String financialTransactionIds[] = new String[payrollRun.getFinancialTransactionCollection().size()];
                    for (int i = 0; i < payrollRun.getFinancialTransactionCollection().size(); i++) {
                        financialTransactionIds[i] = payrollRun.getFinancialTransactionCollection().get(i).getId().toString();
                    }

                    RSTransmission rsTransmission = MobileFactory.createRSTransmission(payrollRun);
                    if (rsTransmission != null) {
                        Collection<CompanyEvent> companyEvents = CompanyEventFinder.findCompanyEvents(company,
                                CompanyEventStatus.Active, EventDetailTypeCode.FinancialTransactionId, financialTransactionIds);
                        if (!companyEvents.isEmpty()) {
                            rsTransmission.setAlertCount(companyEvents.size());
                        }

                        rsResponse.getTransmissions().add(rsTransmission);
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

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getTransmissions(@HeaderParam("username")String pEIN,
                                   @HeaderParam("password")String pPIN,
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

                DomainEntitySet<PayrollRun> payrollRuns = PayrollRunFinder.findPayrollRuns(company, pStart, pSize);
                for (PayrollRun payrollRun : payrollRuns) {
                    String financialTransactionIds[] = new String[payrollRun.getFinancialTransactionCollection().size()];
                    for (int i = 0; i < payrollRun.getFinancialTransactionCollection().size(); i++) {
                        financialTransactionIds[i] = payrollRun.getFinancialTransactionCollection().get(i).getId().toString();
                    }

                    RSTransmission rsTransmission = MobileFactory.createRSTransmission(payrollRun);
                    if (rsTransmission != null) {
                        Collection<CompanyEvent> companyEvents = CompanyEventFinder.findCompanyEvents(company,
                                CompanyEventStatus.Active, EventDetailTypeCode.FinancialTransactionId, financialTransactionIds);
                        if (!companyEvents.isEmpty()) {
                            rsTransmission.setAlertCount(companyEvents.size());
                        }

                        rsResponse.getTransmissions().add(rsTransmission);
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
