package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.cto.general.io.utils.http.IntuitCommonHeaders;
import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.OfflineTicketGenerator;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.sun.jersey.api.client.WebResource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.util.*;
@Slf4j
public class OfflineTicketHeader {


    //method to get the header with offline token
    public static Map<String, List<String>> getHeader(ConfigType configType, String sContentType, String tid) {
        log.info("AuthN: Get Identity 1.0 headers");
        Map<String, List<String>> headers = new HashMap<String, List<String>>();

        String offlineTicket = OfflineTicketGenerator.getInstance().getOfflineTicket(configType);
        if (null != offlineTicket) {
            headers.put("Authorization", Arrays.asList(offlineTicket));
            headers.put("Content-Type", Collections.singletonList(sContentType));
            headers.put("intuit_tid", Collections.singletonList(tid));
        }
        log.info("AuthN: Identity1 Headers built successfully");
        return headers;
    }

    public static Map<String, List<String>> getAuthNHeader(String sContentType, String tid) {
        log.info("AuthN: Get Identity 2.0 headers");
        Map<String, List<String>> headers = new HashMap<String, List<String>>();

        String offlineTicket = PayrollApplicationBeanFactory.getBean(OfflineTicketClient.class).getOfflineTicket();
        if(offlineTicket==null){
            throw new RuntimeException("Offline Ticket is null");
        }
            headers.put("Authorization", Arrays.asList(offlineTicket));
            headers.put("Content-Type", Collections.singletonList(sContentType));
            headers.put("intuit_tid", Collections.singletonList(tid));
             log.info("AuthN: Identity2.0 Headers built successfully");
        return headers;
    }

    public static Map<String, List<String>> getAuthNHeaderWithImpersonationJobId(String sContentType, String tid, String realmId) {
        log.info("AuthN: Get Identity 2.0 headers");
        Map<String, List<String>> headers = new HashMap<String, List<String>>();

        String offlineTicket = PayrollApplicationBeanFactory.getBean(OfflineTicketClient.class).getOfflineTicket(realmId);
        if(offlineTicket==null){
            throw new RuntimeException("Offline Ticket is null");
        }
        headers.put("Authorization", Arrays.asList(offlineTicket));
        headers.put("Content-Type", Collections.singletonList(sContentType));
        headers.put("intuit_tid", Collections.singletonList(tid));
        log.info("AuthN: Identity2.0 Headers built successfully");
        return headers;
    }

    public static Map<String, List<String>> getHeader(ConfigType configType, String sContentType) {
        String tid = generateTid();
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_ID2_ENABLED, true)) {
            return getAuthNHeader(sContentType, tid);
        } else {
            return getHeader(configType, sContentType, tid);
        }

    }

    //method to get the webresourcebuilder with offline header
    public static WebResource.Builder getOfflineHeaderViaJersey(WebResource pWebResource, ConfigType configType, String pMediaType){
        WebResource.Builder webResourceBuilder = pWebResource.accept(pMediaType);
        log.info("AuthN: Get Identity 2.0 headers");
        Map<String, List<String>> offlineHeaders = OfflineTicketHeader.getAuthNHeaderWithImpersonationJobId(pMediaType, generateTid(), null);
        for (String key: offlineHeaders.keySet()) {
            if (offlineHeaders.get(key) != null && !offlineHeaders.get(key).isEmpty())
                webResourceBuilder = webResourceBuilder.header(key, offlineHeaders.get(key).get(0));
        }
        return webResourceBuilder;
    }

    public static String generateTid() {
        String tid = MDC.get(IntuitCommonHeaders.INTUIT_HEADER_TID);
        if(StringUtils.isEmpty(tid))
            return SpcfUniqueId.generateRandomUniqueIdString().replaceAll("-", "");
        return tid.replaceAll("-", "");
    }

}
