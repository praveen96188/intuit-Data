package com.intuit.sbd.payroll.psp.batchjobs.soxReport.service;

import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserDataBatchModel;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserHeaderModel;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.common.utils.threads.MultithreadService;
import com.intuit.sbd.payroll.psp.jss.util.BatchJobConstants;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Getter
@Setter
@Service
public class SoxBatchService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private SoxRequestPublisher requestPublisher;

    //TODO: Remove once Prod data validated
    //temporary addition
    private final String PSP_SOX_HEADER_E2E_ENDPOINT = "https://eventbus-e2e.intuit.com/v2/t4i-fact-cfe-sua-header";
    private final String PSP_SOX_BATCH_E2E_ENDPOINT = "https://eventbus-e2e.intuit.com/v2/t4i-fact-cfe-sua/batch";
    
    @Autowired
    public SoxBatchService(SoxRequestPublisher requestPublisher)
    {
        this.requestPublisher = requestPublisher;
    }

    public boolean sendBatchHeader(SoxUserHeaderModel header) {
        boolean isSuccess = false;
        String headerEndpoint = BatchUtils.getConfigString(BatchJobConstants.PSP_SOX_HEADER_ENDPOINT);

        //TODO: Remove once Prod data validated
        boolean isSOXProdEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_SOX_PROCESSOR_ENDPOINT_PROD,true);

        //TODO: Remove once Prod data validated
        if(!isSOXProdEnabled)
        {
            logger.info("Event=SoxReportBatchJob SubEvent=sending_header_data endpoint=pre-prod");
            headerEndpoint = PSP_SOX_HEADER_E2E_ENDPOINT;
        }

        try {
            isSuccess = publishRequest(headerEndpoint, header);
        } catch (Exception ex) {
            logger.error("Event=SoxReportBatchJobError: Exception occurred while trying to send batch header. Message:", ex);
        }
        return isSuccess;
    }

    public List<Pair<SoxUserDataBatchModel, Boolean>> processBatches(Collection<SoxUserDataBatchModel> soxBatches) {
        List<Pair<SoxUserDataBatchModel, Boolean>> batchResultList;

        MultithreadService<SoxUserDataBatchModel, Boolean> threadProcessor = new MultithreadService<SoxUserDataBatchModel, Boolean>(soxBatches, this::processBatch);
        batchResultList = threadProcessor.execute();
        return  batchResultList;
    }

    public Boolean processBatch(SoxUserDataBatchModel batch)  {
        boolean isCompleted = false;

        String batchEndpoint = BatchUtils.getConfigString(BatchJobConstants.PSP_SOX_BATCH_ENDPOINT);

        //TODO: Remove once Prod data validated
        boolean isSOXProdEnabled = FeatureFlags.get().booleanValue(FeatureFlags.Key.ENABLE_SOX_PROCESSOR_ENDPOINT_PROD,true);

        //TODO: Remove once Prod data validated
        if(!isSOXProdEnabled)
        {
            logger.info("SoxReportBatchJob: sending batch userdata to pre-prod endpoint");
            batchEndpoint = PSP_SOX_BATCH_E2E_ENDPOINT;
        }

        try{
            isCompleted = publishRequest(batchEndpoint, batch.getUserData());
            if (!isCompleted) {
                logger.info("Event=SoxReportBatchJob: SubEvent=batch_sent_failed batchId={}" , batch.getBatchId());
            }else {
                logger.info("Event=SoxReportBatchJob: SubEvent=batch_sent batchSize={} batchId={}", batch.getUserData().size(), batch.getBatchId());
            }
        }catch (Exception e){
            logger.error("Event=SoxReportBatchJobError: Exception in processing batch, batchId={}", batch.getBatchId(), e);
        }
        return isCompleted;
    }

    protected Boolean publishRequest(String url, Object data) {
        boolean isSuccess = false;
        try {
            isSuccess = requestPublisher.publish(url, data);
        } catch (Exception e){
            logger.error("SEvent=oxReportBatchJobError: Exception in publishing request");
        }
        return isSuccess;
    }

}