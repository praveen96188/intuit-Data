package com.intuit.sbd.payroll.psp.iam;

import com.intuit.platform.integration.hats.client.request.GetAuthHeaderForSystemOfflineTicketRequest;
import com.intuit.platform.integration.iam.client.ClientConfiguration;
import com.intuit.platform.integration.iam.client.auth.BasicIAMServiceCredentials;
import com.intuit.platform.integration.iam.client.auth.IAMServiceCredentialsProvider;
import com.intuit.platform.integration.iam.client.auth.StaticIAMServiceCredentialsProvider;
import com.intuit.platform.integration.iam.client.http.HttpClientFactory;
import com.intuit.platform.integration.iamticket.client.IAMOfflineTicketClient;
import com.intuit.platform.integration.iamticket.client.IAMOfflineTicketServiceConfiguration;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import java.util.Objects;

/**
 * Build Authorization headers for accessing services via Gateway
 * 
 * @author dchoudhary1
 *
 */
public class AuthorizationBuilder {
	private static final SpcfLogger logger = SpcfLogManager.getLogger(AuthorizationBuilder.class);
	private IAMOfflineTicketClient offlineTicketClient;

	public AuthorizationBuilder(String iamURL, String appId, String appSecret) {
		IAMServiceCredentialsProvider serviceCredentialsProvider
				= new StaticIAMServiceCredentialsProvider(new BasicIAMServiceCredentials(appId, appSecret));

		offlineTicketClient = new IAMOfflineTicketClient(
				new IAMOfflineTicketServiceConfiguration(iamURL, serviceCredentialsProvider),
				httpClient());
	}

	public String buildAuthorizationHeaderWithOfflineTicket(GetAuthHeaderForSystemOfflineTicketRequest request) {

		try {
			Objects.requireNonNull(request,
					"GetAuthHeaderForSystemOfflineTicketRequest cannot be null");

			return offlineTicketClient.getAuthHeaderForSystemOfflineTicket(request).getAuthorizationHeader();
		} catch (Exception e) {
			throw new RuntimeException("Error getting offline ticket", e);
		}
	}

	private HttpRequestRetryHandler retryHandler(){
		return (exception, executionCount, context) -> {

			if (executionCount >= 3) {
				logger.warn("Maximum tries reached for client http pool ");
				return false;
			}
			if (exception instanceof org.apache.http.NoHttpResponseException) {
				logger.warn("No response from server on " + executionCount + " call");
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