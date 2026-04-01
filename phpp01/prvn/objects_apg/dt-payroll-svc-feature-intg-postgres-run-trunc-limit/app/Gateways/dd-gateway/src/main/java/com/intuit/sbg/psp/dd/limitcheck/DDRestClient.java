package com.intuit.sbg.psp.dd.limitcheck;

import java.net.InetAddress;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.UUID;

import com.intuit.platform.integration.hats.client.request.GetAuthHeaderForSystemOfflineTicketRequest;
import com.intuit.platform.integration.hats.client.request.GetAuthHeaderForSystemOfflineTicketRequestBuilder;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.OfflineTicketGenerator;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.iam.AuthorizationBuilder;

/**
 * 
 * @author dchoudhary1
 * Get and create headers for DD rest call
 *
 */
public class DDRestClient {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String OFFERINGID_HEADER = "intuit_offeringid";
	private static final String INTUIT_TID = "intuit_tid";
	private static final String COUNTRY_HEADER = "intuit_country";

	public static String uRL;
	public static String iamUrl;
	public static String servicePath;
	public static String intuitAppId;
	public static String intuitAppSecret;
	public static String systemUserUsername;
	public static String systemUserPassword;
	public static String desktopApiUrl;
	public static String getSubmissionsPath;
	public static String assestId;
	public static String proxyUrl;
	public static String intuitOfferingId;
	public static int ackRetryCount=0;
	public static int ackRetryIntervalExponential=0;
	public static int postRetryCount=0;
	public static int postRetryIntervalExponential=0;
	public static int proxyPort=0;
	
	private static AuthorizationBuilder authorizationBuilder; 

	 static {
		 DDRestClient.getConfigurations();
	 }
	 
	/**
	 * Set the required headers e.g. Authorization, intuit_offeringid,
	 * intuit_country etc.
	 * 
	 * @return
	 */
	public static LinkedHashMap<String, Object> getHeaders() {
		LinkedHashMap<String, Object> headers = new LinkedHashMap<String, Object>();

		// check if country header is required
		//headers.put(AUTHORIZATION_HEADER, mAuthorization);
		headers.put(OFFERINGID_HEADER, intuitOfferingId);
		headers.put(INTUIT_TID, getTranscationID());
		headers.put(COUNTRY_HEADER, "US");
		headers.put(AUTHORIZATION_HEADER, authorizationBuilder.buildAuthorizationHeaderWithOfflineTicket(getAuthHeaderForSystemOfflineTicketRequest()));

		return headers;
	}

	/**
	 * Returns unique transaction id which is java UUID format.
	 * 
	 * @return
	 */
	private static String getTranscationID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * Get configuration
	 */
	public static void getConfigurations() {
		if(uRL==null){
			uRL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_server_url");
		}
		if(iamUrl==null){
			iamUrl = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_offlineticket_external_endpoint");
		}
		if(intuitAppId==null){
			intuitAppId = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_app_id");
		}
		if(intuitAppSecret==null){
			intuitAppSecret = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_app_secret");	
		}
		if(intuitOfferingId==null){
			intuitOfferingId = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_offeringid");	
		}
		if(systemUserUsername==null){
			systemUserUsername = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_system_userId");
		}
		if(systemUserPassword==null){
			systemUserPassword = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_system_password");	
		}
		if(assestId==null){
			assestId = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_assetId");
		}
		if(servicePath==null){
			servicePath = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_risk_path");
		}
		if(desktopApiUrl==null){
			desktopApiUrl = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_desktop_api_url");
		}
		if(getSubmissionsPath==null){
			getSubmissionsPath = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_get_submissions_path");
		}
		if(ackRetryCount==0){
			ackRetryCount = Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_ackretry_count"));
		}
		if(ackRetryIntervalExponential==0){
			ackRetryIntervalExponential = Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_ackretry_intervalexp"));
		}
		if(postRetryCount==0){
			postRetryCount =  Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_retry_count"));
		}
		if(postRetryIntervalExponential==0){
			postRetryIntervalExponential = Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_retry_intervalexp"));
		}
		if(proxyUrl==null){
			proxyUrl = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_proxy_url");
		}
		if(proxyPort==0){
			proxyPort = Integer.parseInt(ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_dd_proxy_port"));
		}
		if(authorizationBuilder==null){
			authorizationBuilder=new AuthorizationBuilder(iamUrl, intuitAppId, intuitAppSecret);
		}
	}

	/**
	 * Getter setters for static member variables
	 * @return
	 */

	public static int getAckRetryCount() {
		return ackRetryCount;
	}

	public static String getuRL() {
		return uRL;
	}

	public static void setuRL(String uRL) {
		DDRestClient.uRL = uRL;
	}

	public static String getServicePath() {
		return servicePath;
	}

	public static void setServicePath(String servicePath) {
		DDRestClient.servicePath = servicePath;
	}

	public static String getDesktopApiUrl() {
		return desktopApiUrl;
	}

	public static void setDesktopApiUrl(String desktopApiUrl) {
		DDRestClient.desktopApiUrl = desktopApiUrl;
	}

	public static String getGetSubmissionsPath() {
		return getSubmissionsPath;
	}

	public static void setGetSubmissionsPath(String getSubmissionsPath) {
		DDRestClient.getSubmissionsPath = getSubmissionsPath;
	}

	public static String getIntuitAppId() {
		return intuitAppId;
	}

	public static void setIntuitAppId(String intuitAppId) {
		DDRestClient.intuitAppId = intuitAppId;
	}

	public static String getIntuitAppSecret() {
		return intuitAppSecret;
	}

	public static void setIntuitAppSecret(String intuitAppSecret) {
		DDRestClient.intuitAppSecret = intuitAppSecret;
	}

	public static String getSystemUserUsername() {
		return systemUserUsername;
	}

	public static void setSystemUserUsername(String systemUserUsername) {
		DDRestClient.systemUserUsername = systemUserUsername;
	}

	public static String getSystemUserPassword() {
		return systemUserPassword;
	}

	public static void setSystemUserPassword(String systemUserPassword) {
		DDRestClient.systemUserPassword = systemUserPassword;
	}

	public static String getAssestId() {
		return assestId;
	}

	public static void setAssestId(String assestId) {
		DDRestClient.assestId = assestId;
	}

	public static AuthorizationBuilder getAuthorizationBuilder() {
		return authorizationBuilder;
	}

	public static void setAuthorizationBuilder(AuthorizationBuilder authorizationBuilder) {
		DDRestClient.authorizationBuilder = authorizationBuilder;
	}

	public static void setAckRetryCount(int ackRetryCount) {
		DDRestClient.ackRetryCount = ackRetryCount;
	}

	public static int getAckRetryIntervalExponential() {
		return ackRetryIntervalExponential;
	}

	public static void setAckRetryIntervalExponential(int ackRetryIntervalExponential) {
		DDRestClient.ackRetryIntervalExponential = ackRetryIntervalExponential;
	}

	public static int getPostRetryCount() {
		return postRetryCount;
	}

	public static void setPostRetryCount(int postRetryCount) {
		DDRestClient.postRetryCount = postRetryCount;
	}

	public static int getPostRetryIntervalExponential() {
		return postRetryIntervalExponential;
	}

	public static void setPostRetryIntervalExponential(int postRetryIntervalExponential) {
		DDRestClient.postRetryIntervalExponential = postRetryIntervalExponential;
	}

	public static String getProxyUrl() {
		return proxyUrl;
	}

	public static void setProxyUrl(String proxyUrl) {
		DDRestClient.proxyUrl = proxyUrl;
	}

	public static int getProxyPort() {
		return proxyPort;
	}

	public static void setProxyPort(int proxyPort) {
		DDRestClient.proxyPort = proxyPort;
	}

	private static GetAuthHeaderForSystemOfflineTicketRequest getAuthHeaderForSystemOfflineTicketRequest() {
		// TODO Add setMaxLifeSeconds
		GetAuthHeaderForSystemOfflineTicketRequest request = new GetAuthHeaderForSystemOfflineTicketRequestBuilder()
				.setAppId(intuitAppId)
				.setOfferingId(intuitAppId)
				.setAppSecret(intuitAppSecret)
				.setUsername(systemUserUsername)
				.setPassword(systemUserPassword).setAssetId(assestId)
				.setAudiences(Collections.singletonList(assestId)).setIp(OfflineTicketGenerator.getIpAddress()).build();
		return request;
	}

	}

