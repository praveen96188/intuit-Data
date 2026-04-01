package com.intuit.ems.payroll.psp.gateways.tfs;

import com.intuit.ems.tfs.messages.v1.BillingDataResponse;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.common.utils.StopWatch;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import static com.intuit.ems.payroll.psp.gateways.tfs.util.HTTPHelper.executeGet;

/**
 * Created by IntelliJ IDEA.
 * User: jjones1
 * Date: 11/27/12
 * Time: 11:23 AM
 */
public class TFSGateway implements ITFSGateway{

    private static SpcfLogger logger = Application.getLogger(TFSGateway.class);

    private String mAWSServerHost;
    private String mAWSServerPath;

    public TFSGateway() {
        getPreferences();
    }

    public Map<String, Integer> getW2PageCountsByCompany(int pW2Year) {
        StopWatch sw = new StopWatch().start();
        Map<String, Integer> responseMap = new HashMap<String, Integer>();
        try {
            sw.start();
            Client client = Client.create();
            WebResource webResource;
            ClientResponse response ;
            String mAWSUrl= mAWSServerHost + mAWSServerPath;

            webResource = client.resource(mAWSUrl + String.valueOf(pW2Year));
            logger.info("Packaging time:" + sw.getElapsedTimeString());
            sw.reset();
            sw.start();
            response = executeGet(webResource, MediaType.TEXT_XML);
            isSuccessful(response);

            logger.info ("Call time:" + sw.getElapsedTimeString());
            sw.reset();
            sw.start();

            BillingDataResponse billingDataResponse = response.getEntity(BillingDataResponse.class);
            if (billingDataResponse != null) {
                for (BillingDataResponse.CompanyW2BillingData companyW2BillingData : billingDataResponse.getCompanyW2BillingData()) {
                    responseMap.put(companyW2BillingData.getCompanyID(), companyW2BillingData.getEmployeeW2PageCount());
                }
            }

            logger.info ("Process response time:" + sw.getElapsedTimeString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return responseMap;
    }

    private void isSuccessful(ClientResponse pClientResponse) {
        if (pClientResponse.getStatus() != HttpURLConnection.HTTP_OK)  {
            throw new RuntimeException("Failed : " + pClientResponse.toString());
        }
    }

    private void getPreferences() {
        mAWSServerHost = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_tfs_server_awshost");
        mAWSServerPath = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_tfs_server_awsbillingpath");
    }

}
