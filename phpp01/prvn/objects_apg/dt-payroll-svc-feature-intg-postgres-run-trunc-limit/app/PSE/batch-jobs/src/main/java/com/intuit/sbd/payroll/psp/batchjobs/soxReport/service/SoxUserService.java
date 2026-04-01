package com.intuit.sbd.payroll.psp.batchjobs.soxReport.service;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.dao.SoxQueryFactory;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.dao.SoxUserDAO;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.domain.SoxDataManager;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxRequestModel;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserDataBatchModel;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserDataModel;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserHeaderModel;
import com.intuit.sbd.payroll.psp.domain.util.EncryptionUtils;
import javafx.util.Pair;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.*;

import com.intuit.sbd.payroll.psp.jss.util.BatchJobConstants;
import org.springframework.stereotype.Service;

@Service
public abstract class SoxUserService {
    protected SoxBatchService processor;
    protected String accessType;
    protected SoxUserDAO soxUserDAO;
    protected SoxQueryFactory soxQueryFactory;

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SoxUserService(String accessType, SoxBatchService processor, SoxUserDAO soxUserDAO, SoxQueryFactory soxQueryFactory) {
        this.accessType = accessType;
        this.processor = processor;
        this.soxUserDAO = soxUserDAO;
        this.soxQueryFactory = soxQueryFactory;
    }

    public void execute(int retryCount, SoxDataManager dataManager) {

        SoxRequestModel reqToProcess = createSoxRequestModel(dataManager);

        List<SoxUserDataBatchModel> failedBatchReq;

        if(Integer.parseInt(reqToProcess.getHeader().getTotalNoOfRecords())>0) {
            boolean isHeaderSuccess = false;
            //Step1: Send Header Request
            do {
                isHeaderSuccess = processor.sendBatchHeader(reqToProcess.getHeader());
                if (isHeaderSuccess) {
                    logger.info("Event=SoxReportBatchJob SubEvent=batch_header_send_successful!! Header={} \nProceed to send userdata in batches", ReflectionToStringBuilder.toString(reqToProcess.getHeader()) );
                    break;
                }
                retryCount--;
            } while (retryCount >= 0);

            //Step2: Send UserData batch wise if header sent successfully
            if(!isHeaderSuccess)
            {
                logger.error("Event=SoxReportBatchJobError: Batch Header Request failed to send: {}" , ReflectionToStringBuilder.toString(reqToProcess.getHeader()));
                return;
            }

            //reset retry count
            retryCount = BatchJobConstants.SOX_RETRY_COUNT;
            List<SoxUserDataBatchModel> userDataBatches = reqToProcess.getUserDataBatches();

            do {
                    failedBatchReq = processBatches(userDataBatches);
                    userDataBatches = failedBatchReq;
                    retryCount--;
            } while (CollectionUtils.isNotEmpty(userDataBatches) && retryCount >= 0);


            if (CollectionUtils.isNotEmpty(userDataBatches)) {
                String failedBatchesData = ReflectionToStringBuilder.toString(userDataBatches.toArray());
                String encryptedData = EncryptionUtils.probabilisticEncrypt(Application.APPLICATION_LOGGING_KEY_NAME,failedBatchesData,reqToProcess.getHeader().getBatchId());
                logger.error("Event=SoxReportBatchJobError: following records could not be sent: \n{}", encryptedData);
            }
        }
    }

    protected List<SoxUserDataBatchModel> processBatches(List<SoxUserDataBatchModel> batchesListToProcess) {

        List<Pair<SoxUserDataBatchModel, Boolean>> batchResults = processor.processBatches(batchesListToProcess);

        List<SoxUserDataBatchModel> failedBatches = new ArrayList<>();

        for (Pair<SoxUserDataBatchModel, Boolean> result : batchResults) {
            if (!result.getValue()) {
                failedBatches.add(result.getKey());
            }
        }

        logger.info("Event=SoxReportBatchJob: SubEvent=batches_sent  successful={} total_batches={} ",batchesListToProcess.size() - failedBatches.size(), batchesListToProcess.size());

        return failedBatches;
    }

    protected SoxRequestModel createSoxRequestModel(SoxDataManager dataManager) {
        List<SoxUserDataModel> userDataList = getUserData(dataManager);

        String batchID = UUID.randomUUID().toString();
        SoxUserHeaderModel header = getUserHeader(userDataList, batchID, dataManager);

        //split list of userdata to batches
        List<List<SoxUserDataModel>> userDataBatches = ListUtils.partition(userDataList, BatchJobConstants.BATCH_SIZE > BatchJobConstants.MAX_BATCH_SIZE_LIMIT? BatchJobConstants.MAX_BATCH_SIZE_LIMIT: BatchJobConstants.BATCH_SIZE);
        List<SoxUserDataBatchModel> batches = new ArrayList();

        //populate the userDataBatches field in SoxUserDataModel
        for (List<SoxUserDataModel> batch: userDataBatches) {
            batches.add(new SoxUserDataBatchModel(batch,batchID));
            for (SoxUserDataModel userData: batch) {
                userData.setBatchId(batchID);
            }
        }

        //build SoxUserDataModel
        SoxRequestModel soxBatch = new SoxRequestModel();
        soxBatch.setHeader(header);
        soxBatch.setUserDataBatches(batches);

        return soxBatch;
    }

    protected SoxUserHeaderModel getUserHeader(List<SoxUserDataModel> userDataList, String batchID, SoxDataManager dataManager) {
        SoxUserHeaderModel header = new SoxUserHeaderModel();

        header.setBatchId(batchID);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        header.setCreatedDate(timestamp);
        header.setDataManagerName(dataManager.value());
        header.setTotalNoOfRecords(String.valueOf(userDataList.size()));
        header.setAccessType(accessType);
        header.setDataQuery(soxQueryFactory.getSoxDataQuery(dataManager));
        return header;
    }

    protected abstract List<SoxUserDataModel> getUserData(SoxDataManager dataManager) ;

}
