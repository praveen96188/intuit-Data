package com.intuit.sbd.payroll.psp.common.utils;

import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.OfflineTicketGenerator;
import com.intuit.sbd.payroll.psp.emailsender.filter.RequestFilter;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.sun.jersey.api.client.WebResource.Builder;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
@Slf4j
public class AddHeaderRequestFilter implements RequestFilter {

    @Override
    public void filter(Builder builder) {
        if (FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDENTITY2_ENABLED_FOR_REQUEST_FILTER, false)) {
            builder.header(HttpHeaders.AUTHORIZATION, PayrollApplicationBeanFactory.getBean(OfflineTicketClient.class).getOfflineTicket())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            log.info("AuthN: Identity 2 headers built for AddHeaderRequestFilter");
        } else {
            builder.header(HttpHeaders.AUTHORIZATION, OfflineTicketGenerator.getInstance().getOfflineTicket(ConfigType.PSP))
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
            log.info("AuthN: Identity 1 headers built for AddHeaderRequestFilter");
        }
    }
}
