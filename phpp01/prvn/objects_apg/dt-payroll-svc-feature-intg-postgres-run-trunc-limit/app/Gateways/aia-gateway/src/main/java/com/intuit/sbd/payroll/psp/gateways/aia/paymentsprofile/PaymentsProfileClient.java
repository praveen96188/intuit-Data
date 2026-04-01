package com.intuit.sbd.payroll.psp.gateways.aia.paymentsprofile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.cto.general.io.utils.http.IntuitCommonHeaders;
import com.intuit.platform.services.ebpi.billing.v2.SearchPaymentProfileResponse;
import com.intuit.platform.services.ebpi.billing.v2.SearchPaymentProfileResponseImpl;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import com.intuit.sbg.psp.webserviceclient.support.json.JsonConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
@Component
public class PaymentsProfileClient {
    private static Logger logger = LoggerFactory.getLogger(PaymentsProfileClient.class);
    private PaymentsProfileConfig paymentsProfileConfig;
    private HttpServiceClient httpServiceClient;
    private JsonConverter jsonConverter;

    @Autowired
    public PaymentsProfileClient(PaymentsProfileConfig paymentsProfileConfig, HttpServiceClient httpServiceClient, JsonConverter jsonConverter) {
        this.paymentsProfileConfig = paymentsProfileConfig;
        this.httpServiceClient = httpServiceClient;
        this.jsonConverter = jsonConverter;
    }

    public SearchPaymentProfileResponse getPaymentProfileDetailsFromEOCLIC(String eoc, String lic, String can){
        String fullUrl = this.paymentsProfileConfig.getSearchPaymentsProfileResponseEocLicCan();
        fullUrl = fullUrl.replace("<eoc>", eoc);
        fullUrl = fullUrl.replace("<lic>", lic);
        fullUrl = fullUrl.replace("<can>", can);
        HttpServiceResponse httpServiceResponse = this.httpServiceClient.get(
                fullUrl,
                this.getSpecificHeaders());
        try {
            if (httpServiceResponse.isSuccessful()) {
                logger.info("Successfully search payment profile");
                // With JsonConvertor we are getting "Registering an InstanceCreator with Gson"
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.readValue(httpServiceResponse.getBody(), SearchPaymentProfileResponseImpl.class);
            } else {
                logger.error("Error in searching payments profile to URL="+this.paymentsProfileConfig.getSearchPaymentsProfileResponseEocLicCan()+" responseBody="+httpServiceResponse.getBody());
                throw new PaymentsProfileClientException("Error in searching payments profile StatusCode="+httpServiceResponse.getStatusCode(), httpServiceResponse);
            }
        } catch (IOException e) {
            throw new PaymentsProfileClientException("Error in Converting Response StatusCode="+httpServiceResponse.getStatusCode(), httpServiceResponse);
        }
    }

    private Map<String, String> getSpecificHeaders() {
        try {
            return Collections.singletonMap(IntuitCommonHeaders.INTUIT_HEADER_ORIGINATINGIP, InetAddress.getLocalHost().getHostAddress());
        } catch (Exception e) {
            logger.error("Error in setting the "+IntuitCommonHeaders.INTUIT_HEADER_ORIGINATINGIP);
        }
        return Collections.singletonMap(IntuitCommonHeaders.INTUIT_HEADER_ORIGINATINGIP, "127.0.0.1");
    }
}
