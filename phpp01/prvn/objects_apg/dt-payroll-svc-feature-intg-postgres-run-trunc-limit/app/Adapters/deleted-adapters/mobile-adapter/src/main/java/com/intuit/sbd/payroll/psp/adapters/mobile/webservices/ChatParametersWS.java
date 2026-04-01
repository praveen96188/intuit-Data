package com.intuit.sbd.payroll.psp.adapters.mobile.webservices;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.sbd.payroll.psp.adapters.mobile.dtos.RSChatParameterCode;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Jeff Jones
 */

@Path("/chatparameters")
public class ChatParametersWS {

    private static final SpcfLogger logger = PayrollServices.getLogger(AuthenticationWS.class);

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String authenticateUser() throws Exception {
        Map<RSChatParameterCode, String> responseMap = new HashMap<RSChatParameterCode, String>();
        try {
            PayrollServices.beginUnitOfWork();

            responseMap.put(RSChatParameterCode.Host, SystemParameter.findValue(SystemParameter.Code.MOBILE_LIVE_PERSON_HOST));
            responseMap.put(RSChatParameterCode.Version, SystemParameter.findValue(SystemParameter.Code.MOBILE_LIVE_PERSON_HOST_VERSION));
            responseMap.put(RSChatParameterCode.AppKey, SystemParameter.findValue(SystemParameter.Code.MOBILE_LIVE_PERSON_APPLICATION_KEY));

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
        return gson.toJson(responseMap);
    }


}
