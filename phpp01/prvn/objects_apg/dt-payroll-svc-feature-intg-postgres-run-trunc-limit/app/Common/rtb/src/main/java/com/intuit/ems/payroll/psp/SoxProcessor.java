package com.intuit.ems.payroll.psp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.intuit.ems.payroll.psp.factory.TaxTransactionQueryBuilder;
import com.intuit.ems.payroll.psp.model.QueryModel;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbg.payroll.application.context.PayrollApplicationBeanFactory;
import com.intuit.sbg.psp.events.publisher.kafka.KafkaHTTPPublisher;
import com.intuit.sbg.psp.events.publisher.kafka.requests.HttpPublishRequest;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceClient;
import com.intuit.sbg.psp.webserviceclient.rest.HttpServiceResponse;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.apache.commons.httpclient.HttpStatus;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoxProcessor {
    private static final SpcfLogger LOGGER = Application.getLogger(SoxProcessor.class);
    private static final String headerEndPoint = "https://eventbus-e2e.intuit.com/v2/t4i-fact-cfe-sua-header";
    private static final String batchEndPoint = "https://eventbus-e2e.intuit.com/v2/t4i-fact-cfe-sua/batch";
    private static final KafkaHTTPPublisher kafkaPublisher = PayrollApplicationBeanFactory.getBean(KafkaHTTPPublisher.class);


    public static void main(String[] args) {

        LOGGER.info("SoxProcessor: Starting SOX batch Processor RTB");
        new SoxProcessor().processAppUserBatch();

        new SoxProcessor().processDBUserBatch();
        LOGGER.info("SoxProcessor: Completed SOX batch Processor RTB");
    }

    private void processAppUserBatch() {
        String headerPayload = "{\"data_manager_name\":\"PSP\",\"access_type\":\"APPLICATION\",\"batch_id\":\"test_app_psp_123\",\"total_no_of_records\":\"3\",\"data_query\":\"SELECT corp_id, rol.name, null account_status, null profile, usr.created_date FROM PSP_AUTH_USER usr JOIN PSP_AUTH_USER_AUTH_ROLE__ASSOC ara ON USR.AUTH_USER_SEQ=ara.AUTH_USER_FK JOIN PSP_AUTH_ROLE rol ON ara.AUTH_ROLE_FK=rol.AUTH_ROLE_SEQ order by corp_id\",\"created_date\":1637917725488}";
        boolean isHeaderSuccess = false;

        LOGGER.info("SoxProcessor: Publishing Header Request, Application User data");
        isHeaderSuccess = publishRequest(headerEndPoint, headerPayload);

        if(isHeaderSuccess){

            LOGGER.info("SoxProcessor: Header Request Success, proceed to Application User data");

            List<String> userDataBatchesPayload = populateUserDataApplication();
            publishUserDataBatches(userDataBatchesPayload);

            LOGGER.info("SoxProcessor: Completed Application user data processing");

        } else {
            LOGGER.error("SoxProcessor: Error in publishing header request for Application user data");
        }
    }

    private void processDBUserBatch() {
        //new header
        String headerPayload = "{\"data_manager_name\":\"PSP\",\"access_type\":\"DATABASE\",\"batch_id\":\"test_db_psp_123\",\"total_no_of_records\":\"3\",\"data_query\":\"SELECT username ,null role, account_status, profile, created FROM dba_users\",\"created_date\":1637917724557}";
        boolean isHeaderSuccess = false;

        LOGGER.info("SoxProcessor: Publishing Headerr Request, DB User data");

        isHeaderSuccess = publishRequest(headerEndPoint, headerPayload);

        if(isHeaderSuccess){

            LOGGER.info("SoxProcessor: Header Request Success, proceed to DB User data");

            List<String> userDataBatchesPayload = populateUserDataDB();
            publishUserDataBatches(userDataBatchesPayload);

            LOGGER.info("SoxProcessor: Completed DB user data processing");

        } else {
            LOGGER.error("SoxProcessor: Error in publishing header request for DB user data");
        }
    }

    private boolean publishRequest(String endPoint, String payload) {
        boolean isSuccess = false;
        try {
            HttpPublishRequest appReq = createRequest(endPoint, payload);
            HttpServiceResponse response = kafkaPublisher.publishSync(appReq);

            isSuccess = HttpStatus.SC_ACCEPTED == response.getStatusCode();

            LOGGER.info("SoxProcessor: Response: %s" + response.toDetailedString());

        }catch (Exception e){
            LOGGER.error("SoxProcessor: Exception in publishing request");
        }
        return isSuccess;
    }

    private void publishUserDataBatches(List<String> userDataBatchesPayload){
        int batchCount = 1;

        for(String batch: userDataBatchesPayload) {

            boolean isBatchSuccess = publishRequest(batchEndPoint,batch);

            LOGGER.info(String.format("SoxProcessor: Published batch %d,  Status: %b", batchCount, isBatchSuccess));

            batchCount++;
        }
    }

    private List<String> populateUserDataApplication() {
        int batchSize = 2;

        List<String> batches = new ArrayList<>();
        String batch1 = "[" +
                "{\"data_manager_name\":\"PSP\",\"batch_id\":\"test_app_psp_123\",\"access_type\":\"APPLICATION\",\"message_id\":\"1fac0386-a11d-4b0d-a504-b5415c88ab7a\",\"user_name\":\"AL_taxcreditsrep\",\"database_name\":\"PSPSYS01_A\",\"created_date\":1342572591645,\"additional_properties\":{\"access\":\"WOTC Tax Rep\",\"status\":null,\"profile\":null}}," +
                "{\"data_manager_name\":\"PSP\",\"batch_id\":\"test_app_psp_123\",\"access_type\":\"APPLICATION\",\"message_id\":\"47c2042a-5dc6-4e35-a4f0-89a0fab9c747\",\"user_name\":\"automatedtaxforms\",\"database_name\":\"PSPSYS01_A\",\"created_date\":1343937992178,\"additional_properties\":{\"access\":\"Admin\",\"status\":null,\"profile\":null}}" +
                "]";
        batches.add(batch1);

        String batch2 = "[" +
                "{\"data_manager_name\":\"PSP\",\"batch_id\":\"test_app_psp_123\",\"access_type\":\"APPLICATION\",\"message_id\":\"9f357202-7770-4e94-b624-230c29266a24\",\"user_name\":\"c360pspsvcnonprd\",\"database_name\":\"PSPSYS01_A\",\"created_date\":1353085898542,\"additional_properties\":{\"access\":\"DIS Agent\",\"status\":null,\"profile\":null}}" +
                "]";
        batches.add(batch2);

        LOGGER.info("SoxProcessor: Batches created");

        return batches;
    }

    private List<String> populateUserDataDB() {
        int batchSize = 2;

        List<String> batches = new ArrayList<>();
        String batch1 = "[" +
                "{\"data_manager_name\":\"PSP\",\"batch_id\":\"test_db_psp_123\",\"access_type\":\"DATABASE\",\"message_id\":\"91c4611f-28e5-49bf-83f0-7ac35065539c\",\"user_name\":\"SBAUBLYS\",\"database_name\":\"PSPSYSIB_A\",\"created_date\":1601486927000,\"additional_properties\":{\"access\":null,\"status\":\"OPEN\",\"profile\":\"INDIVIDUAL_PROFILE\"}}," +
                "{\"data_manager_name\":\"PSP\",\"batch_id\":\"test_db_psp_123\",\"access_type\":\"DATABASE\",\"message_id\":\"71268a7d-c08e-486e-ac3c-3e0de5292e3b\",\"user_name\":\"XS$NULL\",\"database_name\":\"PSPSYSIB_A\",\"created_date\":1537797239000,\"additional_properties\":{\"access\":null,\"status\":\"EXPIRED & LOCKED\",\"profile\":\"DEFAULT\"}}" +
                "]";
        batches.add(batch1);

        String batch2 = "[" +
                "{\"data_manager_name\":\"PSP\",\"batch_id\":\"test_db_psp_123\",\"access_type\":\"DATABASE\",\"message_id\":\"43345e09-fe84-4c71-8bf2-3d42ffe9bdc6\",\"user_name\":\"RDSADMIN\",\"database_name\":\"PSPSYSIB_A\",\"created_date\":1537797813000,\"additional_properties\":{\"access\":null,\"status\":\"OPEN\",\"profile\":\"RDSADMIN\"}}" +
                "]";
        batches.add(batch2);

        LOGGER.info("SoxProcessor: Batches created");

        return batches;
    }


    protected HttpPublishRequest createRequest(String url, String data) {

        Map<String,String> headers = new HashMap<>();
        headers.put(HttpHeaders.CACHE_CONTROL,"no-cache");
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);

        HttpPublishRequest req = new HttpPublishRequest(url, data , headers, true);
        return req;
    }

}
