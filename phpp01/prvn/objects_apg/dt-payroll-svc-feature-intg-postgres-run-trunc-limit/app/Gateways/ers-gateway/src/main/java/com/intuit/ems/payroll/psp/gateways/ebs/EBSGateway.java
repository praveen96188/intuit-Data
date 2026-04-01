package com.intuit.ems.payroll.psp.gateways.ebs;

import com.intuit.ebs.entitledOffering.xsd.EntitledOfferingsType;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.common.utils.OfflineTicketHeader;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.intuit.v4.common.FeatureFlag;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;

/**
 * Created with IntelliJ IDEA.
 * User: ssaxena2
 * Date: 3/14/17
 * Time: 8:17 PM
 * To change this template use File | Settings | File Templates.
 * We have created EBSGateway in ERSGateway with a future perspective that EBS Rest apis will be used in future and ERS apis will be deprecated
 */
public class EBSGateway implements IEBSGateway {

    private Unmarshaller mUnmarshaller;
    private JAXBContext jaxbContext;
    private String mURL;
    private static final SpcfLogger logger = PayrollServices.getLogger(EBSGateway.class);
    private HttpServiceClient httpServiceClient;

    public EBSGateway() throws Throwable {
        mURL = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_ebs_server_url");
        logger.info("URL used for EBSGateway "+ mURL);
        jaxbContext = JAXBContext.newInstance("com.intuit.ebs.entitledOffering.xsd");
        mUnmarshaller = jaxbContext.createUnmarshaller();
        httpServiceClient = PayrollApplicationBeanFactory.getBean(HttpServiceClient.class);
    }

    @Override
    public void disableEntitlement(String pLicenseNumber, String pEOC) throws Exception {
        String entitledOfferingId = getEntitlementOfferingId(pLicenseNumber, pEOC);
        if (entitledOfferingId != null) {
            cancelEntitlement(entitledOfferingId);
        } else {
            throw new Exception("EntitledOfferingId attached is null");
        }
    }

    private String getEntitlementOfferingId(String pLicenseNumber, String pEOC) throws Exception {
        String response = getEntitlementDetails(pLicenseNumber, pEOC);
        JAXBElement<?> jaxbElement = (JAXBElement<?>) mUnmarshaller.unmarshal(new StringReader(response));
        EntitledOfferingsType entitledOfferingsType = (EntitledOfferingsType) jaxbElement.getValue();
        if (entitledOfferingsType.getEntitledOffering().size() != 1) {
            throw new Exception("Exception: Number of EntitledOfferingId attached is " + entitledOfferingsType.getEntitledOffering().size());
        }
        return entitledOfferingsType.getEntitledOffering().get(0).getEntitledOfferingId();
    }

    private String getEntitlementDetails(String pLicenseNumber, String pEOC) throws Exception {

        String request = "licenseId=%LICENSEID%&entitlementId=%ENTITLEMENTID%";

        request = request.replaceFirst("%LICENSEID%", Matcher.quoteReplacement(pLicenseNumber)).
                replaceFirst("%ENTITLEMENTID%", Matcher.quoteReplacement(pEOC));

        Boolean useWebServiceClient = FeatureFlags.get().booleanValue(FeatureFlags.Key.USE_WEB_SERVICE_CLIENT, false);
        if(useWebServiceClient){
            String response = executeGet(String.join("?", mURL,request));
            return response;
        }

        GetMethod getMethod = new GetMethod(mURL);
        getMethod.setQueryString(request);
        String response = executeHttpMethod(getMethod);
        return  response;
    }

    private String cancelEntitlement(String pEntitledOfferingId) throws Exception {

        PostMethod postMethod = new PostMethod(mURL);
        String request = "entitledOfferingId=%ENTITLEDOFFERINFID%&operation=cancelNow";
        request = request.replaceFirst("%ENTITLEDOFFERINFID%", Matcher.quoteReplacement(pEntitledOfferingId));
        postMethod.setQueryString(request);
        return executeHttpMethod(postMethod);
    }

    private String executeGet(String url) throws Exception {
        HttpServiceResponse response = httpServiceClient.get(url, getHeaders());

        logger.info(String.format("Action=ConvertingToHttpClient, url=%s, responseStatus=%s",url, response.getStatusCode()));

        if(!response.isSuccessful()){
            throw new Exception("Unexpected response from HTTP API (statusCode: " + response.getStatusCode() + ") Response: " + response);
        }

        return response.getBody();
    }

    private String executeHttpMethod(HttpMethod pHttpMethod) throws Exception {
        int statusCode = 0;
        String response = null;

        Map<String, List<String>> headers = new OfflineTicketHeader().getHeader(ConfigType.PSP, MediaType.APPLICATION_XML);
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            Optional<String> optionalHeader =  header.getValue().stream().findFirst();
            if(!optionalHeader.isPresent()){
                continue;
            }
            pHttpMethod.setRequestHeader(header.getKey(), optionalHeader.get());
        }
        HttpClient httpClient = new HttpClient();
        try {
            statusCode = httpClient.executeMethod(pHttpMethod);
            response = pHttpMethod.getResponseBodyAsString();
            logger.info(String.format("Action=UsingCommonHttpClient, url=%s, responseStatus=%s",pHttpMethod.getURI(), statusCode));

        } catch(Exception e){
            throw new Exception(e.getClass().getName()+" occurred while making call to EBS, check logs for details",e);
        }finally {
            // Release current connection to the connection pool once you are done
            pHttpMethod.releaseConnection();
        }
        if (!(statusCode == 200 || statusCode == 204 || statusCode == 201)) {
            throw new Exception("Unexpected response from HTTP API (statusCode: " + statusCode + ") Response: " + response);
        }

        return response;
    }

    private Map<String, String> getHeaders(){
        Map<String, String> headerMap = new HashMap<>();
        Map<String, List<String>> headers = new OfflineTicketHeader().getHeader(ConfigType.PSP, MediaType.APPLICATION_XML);
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            Optional<String> optionalHeader =  header.getValue().stream().findFirst();
            if(!optionalHeader.isPresent()){
                continue;
            }
            headerMap.put(header.getKey(), optionalHeader.get());
        }
        return headerMap;
    }

}
