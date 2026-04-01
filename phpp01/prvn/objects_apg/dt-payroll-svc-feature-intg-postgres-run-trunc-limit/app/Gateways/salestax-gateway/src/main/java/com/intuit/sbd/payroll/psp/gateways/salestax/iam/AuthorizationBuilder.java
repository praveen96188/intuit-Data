package com.intuit.sbd.payroll.psp.gateways.salestax.iam;

import java.util.Objects;

import com.intuit.platform.integration.iam.client.http.HttpClientFactory;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpCoreContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.intuit.platform.integration.hats.client.request.GetAuthHeaderForSystemOfflineTicketRequest;
import com.intuit.platform.integration.hats.client.request.GetAuthHeaderForSystemOfflineTicketRequestBuilder;
import com.intuit.platform.integration.iam.client.ClientConfiguration;
import com.intuit.platform.integration.iam.client.auth.BasicIAMServiceCredentials;
import com.intuit.platform.integration.iam.client.auth.IAMServiceCredentialsProvider;
import com.intuit.platform.integration.iam.client.auth.StaticIAMServiceCredentialsProvider;
import com.intuit.platform.integration.iamticket.client.IAMOfflineTicketClient;
import com.intuit.platform.integration.iamticket.client.IAMOfflineTicketServiceConfiguration;
import com.intuit.spc.foundations.portability.SpcfUniqueId;


/**
 * This class is duplicated to avoid cyclic dependency utils -> payroll-services-api -> domain -> salestax-gateway -> utils. This class is not to be
 * used by other modules. Please use the original AuthorizationBuilder present under Common utils.
 */
public class AuthorizationBuilder {

    public static Logger LOGGER = LoggerFactory.getLogger(AuthorizationBuilder.class);

    /**
     * IAM URL will change for every application environment but won't change
     * for every accessing offline application
     */
    public static final String IAM_URL = IAMOfflineTicketServiceConfiguration.E2E_IAMOFFLINETICKET_SERVICE_ENDPOINT;

    private String iamURL;

    private GetAuthHeaderForSystemOfflineTicketRequest systemOfflineTicketRequest;

    private IAMOfflineTicketClient offlineTicketClient;

    public AuthorizationBuilder(GetAuthHeaderForSystemOfflineTicketRequest systemOfflineTicketRequest) {
        this.iamURL = IAM_URL;
        this.systemOfflineTicketRequest = systemOfflineTicketRequest;
    }

    public AuthorizationBuilder(String iamURL, GetAuthHeaderForSystemOfflineTicketRequest systemOfflineTicketRequest) {
        this.iamURL = iamURL;
        this.systemOfflineTicketRequest = systemOfflineTicketRequest;
    }

    public String getIamURL() {
        return iamURL;
    }

    public GetAuthHeaderForSystemOfflineTicketRequest getSystemOfflineTicketRequest() {
        return systemOfflineTicketRequest;
    }

    private void initializeOfflineTicket() {

        if (offlineTicketClient != null) {
            return;
        }

        Objects.requireNonNull(getSystemOfflineTicketRequest(),
                "GetAuthHeaderForSystemOfflineTicketRequest cannot be null");

        IAMServiceCredentialsProvider serviceCredentialsProvider = new StaticIAMServiceCredentialsProvider(
                new BasicIAMServiceCredentials(getSystemOfflineTicketRequest().getAppId(),
                        getSystemOfflineTicketRequest().getAppSecret()));

        offlineTicketClient = new IAMOfflineTicketClient(
                new IAMOfflineTicketServiceConfiguration(getIamURL(), serviceCredentialsProvider),
                httpClient());
    }

    public String buildAuthorizationHeaderWithOfflineTicket() {

        try {
            initializeOfflineTicket();

            GetAuthHeaderForSystemOfflineTicketRequest request = new GetAuthHeaderForSystemOfflineTicketRequestBuilder()
                    .setClone(getSystemOfflineTicketRequest())
                    .setTransactionId(SpcfUniqueId.generateRandomUniqueIdString()).build();

            return offlineTicketClient.getAuthHeaderForSystemOfflineTicket(request).getAuthorizationHeader();
        } catch (Exception e) {
            throw new RuntimeException("Error getting offline ticket", e);
        }
    }

    private HttpRequestRetryHandler retryHandler(){
        return (exception, executionCount, context) -> {
            String requestURL=((HttpCoreContext)context).getRequest().getRequestLine().getUri();
            LOGGER.info("request url for retry==%s", requestURL);
            if (executionCount >= 3) {
                LOGGER.warn("Maximum tries reached for client http pool ");
                return false;
            }
            if (exception instanceof org.apache.http.NoHttpResponseException) {
                LOGGER.warn("No response from server on " + executionCount + " call");
                return true;
            }
            return false;
        };
    }

    private HttpClient httpClient(){
        HttpClient httpClient = HttpClientFactory.createHttpClient(new ClientConfiguration());
        DefaultHttpClient defaultHttpClient = (DefaultHttpClient) httpClient;
        defaultHttpClient.setHttpRequestRetryHandler(retryHandler());
        return defaultHttpClient;
    }
}
