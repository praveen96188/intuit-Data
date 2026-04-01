package com.intuit.sbd.payroll.psp.batchjobs;

import com.intuit.qbdt.identity.authN.offlineticket.OfflineTicketClient;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.ConfigType;
import com.intuit.sbd.payroll.psp.common.utils.offlineticket.OfflineTicketGenerator;
import com.intuit.sbd.payroll.psp.common.utils.ManagedShutdownHook;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationManager;
import com.intuit.sbd.payroll.psp.configuration.ConfigurationModule;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

@Slf4j
@Component
public class BatchJobsShutdownHook implements ManagedShutdownHook {

    private static String cleanupApiPath;
    //reason for not using a bean for httpClient - want to create shutdown hook as defensively as possible
    private static HttpClient httpClient;

    @Autowired
    public BatchJobsShutdownHook(@Value("${cleanupPodEndpoint}") String cleanupPodEndpoint) {
        httpClient = HttpClients.custom().build();
        String cleanupApiBaseUrl = ConfigurationManager.getSettingValue(ConfigurationModule.BatchJobs, "psp_jss_base_application_url");
        cleanupApiPath = cleanupApiBaseUrl + cleanupPodEndpoint;
        log.info("podCleanupApiPath={}",cleanupApiPath);
    }



    @Override
    public void execute() {
        gracefulShutdown();
    }

    /***
     * Will make a call to cleanupApi with the podName
     */
    public void gracefulShutdown()  {
        try {
            // we will call the cleanupApi with this path
            log.info("BatchJobsShutdownHook called");
            String url = cleanupApiPath + InetAddress.getLocalHost().getHostName();
            HttpDelete httpDelete = new HttpDelete(url);
            // Generate the headers required for the request
            Header headers[] = getRequestHeaders();
            httpDelete.setHeaders(headers);

            log.info("calling the cleanup api");
            HttpResponse httpResponse = httpClient.execute(httpDelete);
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            log.info("response code received from cleanupApi="+responseCode);
            log.info("batch-job graceful shutdown hook complete");
        } catch (Exception e) {
            log.error("error caught while attempting to perform cleanup activities");
        }
    }

    private Header[] getRequestHeaders() {
        String offlineTicket;
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_IDENTITY2_ENABLED_FOR_BATCHJOB_HOOK,true)){
            offlineTicket = PayrollApplicationBeanFactory.getBean(OfflineTicketClient.class).getOfflineTicket();
            log.info("AuthN: Identity 2 BatchJob Offline ticket received.");
        }
        else {
            offlineTicket = OfflineTicketGenerator.getInstance().getOfflineTicket(ConfigType.PSP);
            log.info("AuthN: Identity 1 BatchJob Offline ticket received.");
        }
        Header headers[] = {
                new BasicHeader("Authorization", offlineTicket),
                new BasicHeader("intuit-tid", SpcfUniqueId.generateRandomUniqueIdString())
        };
        return headers;
    }



}
