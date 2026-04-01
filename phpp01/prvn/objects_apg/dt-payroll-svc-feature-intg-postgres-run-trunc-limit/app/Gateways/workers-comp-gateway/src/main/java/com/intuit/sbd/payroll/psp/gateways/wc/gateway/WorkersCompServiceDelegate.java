package com.intuit.sbd.payroll.psp.gateways.wc.gateway;

import com.google.gson.Gson;
import com.intuit.bp.wc.common.schema.Payroll;
import com.intuit.bp.wc.common.schema.WorkersCompPayrollUploadResponse;
import com.intuit.bp.wc.common.schema.WorkersCompSubscriptions;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.WCChangeEventDTO;
import com.intuit.sbd.payroll.psp.gateways.wc.dto.WCChangeEventResponseDTO;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBElement;
import java.util.List;

import static com.intuit.sbd.payroll.psp.gateways.wc.util.WCUtil.getWCResource;
import static com.intuit.sbd.payroll.psp.gateways.wc.util.WorkersCompProperty.WorkersCompURLEnum.*;

/**
 * Author: Sriram Nutakki
 * Date created: 10/31/12
 */
public class WorkersCompServiceDelegate {

    private static final SpcfLogger logger = SpcfLogManager.getLogger(WorkersCompServiceDelegate.class);

    public static WorkersCompSubscriptions getSubscriptionChanges() {
        logDebugInfo("Getting subscription changes...");

        WorkersCompSubscriptions subscriptions = getWCResource(
                WC_SERVICE_GET_SUBSCRIPTIONS_URL,
                MediaType.APPLICATION_XML
        )
                .get(new GenericType<JAXBElement<WorkersCompSubscriptions>>() {})
                .getValue();

        logDebugInfo("Finished getting subscription changes");

        return subscriptions;
    }

    public static void postSubscriptionConfirmation(WorkersCompSubscriptions subscriptions) {
        logDebugInfo("Posting subscription changes...");

        getWCResource(
                WC_SERVICE_POST_SUBSCRIPTIONS_CONFIRMATION_URL,
                MediaType.APPLICATION_XML
        )
                .entity(new GenericType<JAXBElement<WorkersCompSubscriptions>>() {}, MediaType.APPLICATION_XML)
                .post(subscriptions);

        logDebugInfo("Finished posting subscription changes");
    }

    public static WorkersCompPayrollUploadResponse uploadPayroll(Payroll payroll) {
        logDebugInfo("Uploading payroll...");

        WorkersCompPayrollUploadResponse response = getWCResource(
                WC_SERVICE_POST_PAYROLL_URL,
                MediaType.APPLICATION_XML
        )
                .entity(new GenericType<JAXBElement<Payroll>>() {}, MediaType.APPLICATION_XML)
                .post(new GenericType<JAXBElement<WorkersCompPayrollUploadResponse>>() {}, payroll)
                .getValue();


        logDebugInfo("Finished uploading payroll");
        return response;
    }

    public static String getDisplayDataForHelpDesk(String sourceSystemCd, String sourceCompanyId) {
        logDebugInfo("Getting display data for helpdesk...");

        String data =
                getWCResource(
                        WC_SERVICE_GET_DISPLAY_DATA_FOR_HELPDESK_URL,
                        MediaType.APPLICATION_XML, sourceSystemCd, sourceCompanyId
                )
                .get(String.class);

        logDebugInfo("Finished getting display data for helpdesk");

        return data;
    }

    public static WCChangeEventResponseDTO postChangeEvents(List<WCChangeEventDTO> events) throws Exception {
        logDebugInfo("Sending change events to WC service: " + events.size());

        // build json array from list of company events
        Gson gson = new Gson();
        String eventsInJSONFormat = gson.toJson(events);

        // send events to WC service
        String responseStr = getWCResource(
                WC_SERVICE_POST_CHANGE_EVENTS_URL,
                MediaType.APPLICATION_JSON
        )
                .post(ClientResponse.class, eventsInJSONFormat)
                .getEntity(String.class);

        WCChangeEventResponseDTO responseDTO = gson.fromJson(responseStr, WCChangeEventResponseDTO.class);

        logDebugInfo("Finished sending change events to WC service");

        return responseDTO;
    }

    private static void logDebugInfo(String info) {
        logger.debug(info);
    }
}