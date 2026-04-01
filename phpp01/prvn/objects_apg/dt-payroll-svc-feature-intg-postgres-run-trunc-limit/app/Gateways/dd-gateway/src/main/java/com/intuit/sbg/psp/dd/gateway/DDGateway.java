package com.intuit.sbg.psp.dd.gateway;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intuit.dd.client.model.Submission;
import com.intuit.dd.client.model.Submissions;
import com.intuit.pmo.client.model.PayrollCheckResponse;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.dd.limitcheck.DDRestClient;
import com.intuit.sbg.psp.dd.util.DateTimeTypeAdapter;
import com.intuit.sbg.psp.dd.util.DateTypeAdapter;
import com.intuit.sbg.psp.dd.util.GsonFieldNameStrategy;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
/**
 * @author dchoudhary1
 * Class to connect to DD server
 */
public class DDGateway implements IDDGateway{

	private final static Logger LOGGER = Logger.getLogger(DDGateway.class.getName());
	private final static String GET_SUBMISSION_PARAMS="?ownerId=%s&detailTransactionTypes=%s&sourceSubmissionId=%s";
	public static Gson gson = null;
	private HttpServiceClient httpServiceClient;

	public DDGateway(){
		httpServiceClient = PayrollApplicationBeanFactory.getBean(HttpServiceClient.class);
	}

	static {
		// Prepare GSON
		 gson = new GsonBuilder().setPrettyPrinting().setFieldNamingStrategy(new GsonFieldNameStrategy())
				.registerTypeAdapter(LocalDate.class, new DateTypeAdapter())
				.registerTypeAdapter(DateTime.class, new DateTimeTypeAdapter()).
				create();
	}

	/**
	 * Replace any double occurances of slashes with single slash
	 *
	 * @param s
	 * @return Normalized String
	 */
	private String normalizeURL(String s) {
		return s.replaceAll("(//)(?<!(\\w+\\://))", "/");
	}

	/**
	 * limitCheckRequestBuilder is used to build request for the Limit check
	 *
	 * @param riskCheckRequest
	 * @return
	 * @throws Exception
	 */
	private String limitCheckRequestBuilder(Object riskCheckRequest) throws Exception {

		String jsonInString = gson.toJson(riskCheckRequest);
        LOGGER.info("limitCheckRequestBuilder:The request is  riskcheckrequest"+jsonInString);

		String urlStr = normalizeURL( DDRestClient.getuRL() + "/" + DDRestClient.getServicePath() );

		Map<String, String> headerMap = new HashMap<String, String>();
		LinkedHashMap<String, Object> headers = DDRestClient.getHeaders();
		headers.forEach((key, value) -> {
			//postMethod.setRequestHeader(key, value.toString());
			headerMap.put(key, value.toString());
		});

		HttpServiceResponse response = httpServiceClient.post(urlStr, jsonInString, headerMap);
		LOGGER.info(String.format("Action=ConvertingToHttpClient, url=%s, responseStatus=%s, responseBody=%s",urlStr, response.getStatusCode(), EncryptionUtils.probabilisticEncrypt(Application.APPLICATION_LOGGING_KEY_NAME, response.getBody(), "dd_gateway")));
		return response.getBody();
	}

	/**
	 * Build the api call to get submissions
	 *
	 * @param ownerId
	 * @param detailTransactionTypes
	 * @param sourceSubmissionId
	 * @return
	 * @throws Exception
	 */
	private String submissionsRequestBuilder(String ownerId, String detailTransactionTypes, String sourceSubmissionId) throws Exception {

		DDRestClient.getConfigurations();
		String urlStr = normalizeURL( DDRestClient.getDesktopApiUrl() + "/" + DDRestClient.getGetSubmissionsPath()+String.format(GET_SUBMISSION_PARAMS, ownerId, detailTransactionTypes, sourceSubmissionId));

		Map<String, String> headerMap = new HashMap<>();
		LinkedHashMap<String, Object> headers = DDRestClient.getHeaders();
		headers.forEach((key, value) -> {
			//getMethod.setRequestHeader(key, value.toString());
			headerMap.put(key, value.toString());
		});

		HttpServiceResponse response = httpServiceClient.get(urlStr, headerMap);
		LOGGER.info(String.format("Action=ConvertingToHttpClient, url=%s, responseStatus=%s, responseBody=%s",urlStr, response.getStatusCode(), EncryptionUtils.probabilisticEncrypt(Application.APPLICATION_LOGGING_KEY_NAME, response.getBody(), ownerId)));
		return response.getBody();
	}

	/**
	 * CheckLimit
	 *
	 * @param riskCheckRequest
	 * @return
	 * @throws Exception
	 */
	@Override
	public PayrollCheckResponse checkLimit(Object riskCheckRequest) throws Exception {
		try {
			String riskResponse = limitCheckRequestBuilder(riskCheckRequest);

			if (riskResponse != null && !riskResponse.isEmpty()) {
				PayrollCheckResponse riskCheckResponse = (PayrollCheckResponse) gson.fromJson(riskResponse,
						PayrollCheckResponse.class);
				LOGGER.info("The PayrollCheckResponse:"+ riskCheckResponse.toString());
				String limitCheck = riskCheckResponse.getLimitCheck();
				if (StringUtils.isNotEmpty(limitCheck)
						&& (limitCheck.equalsIgnoreCase("pass") || limitCheck.equalsIgnoreCase("fail"))) {
					LOGGER.info("Successful DD Limit Check Call - HTTP 200 received");
				}
				return riskCheckResponse;
			}
		} catch (Exception e) {
			LOGGER.severe("DDGateway:checkLimit failed with exception:{}" + e.getMessage());
			throw e;
		}
		return null;
	}


	/**
	 * Get submission Id from payrollRunId
	 *
	 * @param ownerId
	 * @param detailTransactionTypes
	 * @return
	 * @throws Exception
	 */
	@Override
	public String getSubmissionId(String ownerId, String detailTransactionTypes, String payrollRunId) throws Exception {
		try {
			String submissionResponse = submissionsRequestBuilder(ownerId, detailTransactionTypes, payrollRunId);

			if (submissionResponse != null && !submissionResponse.isEmpty()) {
				Submissions submissionsResponse = (Submissions) gson.fromJson(submissionResponse, Submissions.class);
				for(Submission submission: submissionsResponse.getSubmissions()) {
					if(submission.getSourceSubmissionId()!=null && submission.getSourceSubmissionId().equals(payrollRunId)) {
						LOGGER.info("Successful DD Submissions Call - HTTP 200 received");
						return submission.getSubmissionId();
					}
				}
			}
			else {
				LOGGER.severe("DD Submissions Call found no object - HTTP 404 received");
			}
		} catch (Exception e) {
			LOGGER.severe("DDGateway:getSubmissionId failed with exception:{}" + e.getMessage());
			throw e;
		}
		return null;
	}

}
