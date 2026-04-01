package com.intuit.sbg.psp.common.gateway;

import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;

import java.util.HashMap;
import java.util.Map;


public class JSSGateway {


    private static String mURL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_jss_server_url");

    private static final Logger logger = LoggerFactory.getLogger(JSSGateway.class);
    private static HttpServiceClient httpServiceClient;

    static{
        httpServiceClient = PayrollApplicationBeanFactory.getBean(HttpServiceClient.class);
    }


    static private  String executeHttpMethod(HttpMethod pHttpMethod) throws Exception  {
        String responseString = null;

        for (Map.Entry<String, String> header: getHeaders().entrySet()) {
            pHttpMethod.setRequestHeader(header.getKey(), header.getValue());
        }

        int statusCode=0;
        HttpClient httpClient = new HttpClient();
        statusCode = httpClient.executeMethod(pHttpMethod);
        responseString = pHttpMethod.getResponseBodyAsString();

        logger.info( "Response String: " + responseString + " statusCode " + statusCode );
        if (!(statusCode == 200 || statusCode == 204 || statusCode == 201)) {
            throw new Exception( statusCode + " " +responseString );
        }
        pHttpMethod.releaseConnection();
        return responseString;
    }

    public static String scheduleJob(String pjobName, String timerExpression, String... pargs) throws Exception  {
        String jsonInString = null;
        ObjectMapper mapper = new ObjectMapper();
        JSSBatchJobSchedulerRequest inputStream=new JSSBatchJobSchedulerRequest();
        if(timerExpression != null) {
            inputStream.setTimerExpression(timerExpression);
        }
        inputStream.setArg(pargs);
        inputStream.setJobname(pjobName);
        jsonInString = mapper.writeValueAsString(inputStream);
        logger.info( "Rest URL : " + mURL + "RequestBody" + jsonInString );

        boolean useWebServiceClient = FeatureFlags.get().booleanValue(FeatureFlags.Key.USE_WEB_SERVICE_CLIENT, false) ;
        if(useWebServiceClient){
            String response = executePost(jsonInString);
            return response;
        }

        PostMethod postMethod = new PostMethod(mURL);
        postMethod.setRequestBody(jsonInString);

        String response = executeHttpMethod(postMethod);
        return response;
    }

    private static String executePost(String body) throws Exception {
        HttpServiceResponse httpServiceResponse = httpServiceClient.post(mURL, body, getHeaders());

        int statusCode = httpServiceResponse.getStatusCode();
        String responseString = httpServiceResponse.getBody();

        logger.info("Action=ConvertingToHttpClient, url={}, request={}, responseStatus={}, responseBody={}",mURL, body, statusCode, responseString);

        if (!httpServiceResponse.isSuccessful()) {
            throw new Exception( statusCode + " " +responseString );
        }
        return responseString;
    }

    private static Map<String, String> getHeaders(){
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");

        return headers;
    }

}
