package com.intuit.sbd.payroll.psp.batchjobs.soxReport.service;

import com.intuit.sbd.payroll.psp.batchjobs.soxReport.domain.SoxDataManager;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.*;
import com.intuit.sbd.payroll.psp.batchjobs.util.BatchUtils;
import com.intuit.sbd.payroll.psp.jss.util.BatchJobConstants;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.junit.Assert;

import java.sql.Timestamp;
import java.util.*;

public class SoxBatchProcessorTest {

    @InjectMocks
    @Spy
    private SoxBatchService batchProcessor;

    private String headerEndPoint;
    private String batchEndPoint;

    public SoxBatchProcessorTest() {}

    @Before
    public void setup(){

        MockitoAnnotations.openMocks(this);
        batchEndPoint = BatchUtils.getConfigString("psp_sox_batch_endpoint");
        headerEndPoint = BatchUtils.getConfigString("psp_sox_header_endpoint");
    }

    @Test
    public void processBatchListReturnsResultMap()
    {
        List<SoxRequestModel> batchCollection = new ArrayList();

        SoxRequestModel batch = CreateMockSoxBatchRequest(5,1, BatchJobConstants.ACCESS_TYPE_APPLICATION, BatchJobConstants.SOX_APP_QUERY, SoxDataManager.DATA_MANAGER_APP.value());

        Map<SoxUserDataBatchModel,Boolean> expectedResultMap = new HashMap();

        for(int i=0; i<3; i++) {
            Mockito.doReturn(true).when(batchProcessor).processBatch(batch.getUserDataBatches().get(i));
            expectedResultMap.put(batch.getUserDataBatches().get(i), true);
        }
        for(int i=3; i<5; i++) {
            Mockito.doReturn(false).when(batchProcessor).processBatch(batch.getUserDataBatches().get(i));
            expectedResultMap.put(batch.getUserDataBatches().get(i), false);
        }

        List<Pair<SoxUserDataBatchModel,Boolean>> results = batchProcessor.processBatches(batch.getUserDataBatches());

        for(int i=0; i<3; i++) {
            Mockito.verify(batchProcessor).processBatch(batch.getUserDataBatches().get(i));
        }
        for(int i=3; i<5; i++) {
            Mockito.verify(batchProcessor).processBatch(batch.getUserDataBatches().get(i));
        }

        Assert.assertEquals(5, results.size());

        for (Map.Entry<SoxUserDataBatchModel, Boolean> batchResult: expectedResultMap.entrySet()) {
            SoxUserDataBatchModel key = batchResult.getKey();
            Boolean expectedValue = batchResult.getValue();

            Boolean found = false;
            for (Pair<SoxUserDataBatchModel, Boolean> pair: results) {
                if(key == pair.getKey() && expectedValue == pair.getValue()){
                    found = true;
                }
            }

            Assert.assertTrue(found);
        }

    }

    @Test
    public void processBatchReturnsResultPairWithSuccess () throws Exception {
        SoxRequestModel batch = CreateMockSoxBatchRequest(1,1, BatchJobConstants.ACCESS_TYPE_APPLICATION, BatchJobConstants.SOX_APP_QUERY, SoxDataManager.DATA_MANAGER_APP.value());

        Boolean expectedResult = true;

        Mockito.doReturn(true).when(batchProcessor).publishRequest(headerEndPoint, batch.getHeader());
        Mockito.doReturn(true).when(batchProcessor).publishRequest(batchEndPoint, batch.getUserDataBatches().get(0).getUserData());

        Boolean resultHeader = batchProcessor.sendBatchHeader(batch.getHeader());
        Boolean resultBatch = batchProcessor.processBatch(batch.getUserDataBatches().get(0));

        Mockito.verify(batchProcessor).publishRequest(headerEndPoint, batch.getHeader());
        Mockito.verify(batchProcessor).publishRequest(batchEndPoint, batch.getUserDataBatches().get(0).getUserData());
        Assert.assertEquals(expectedResult, resultHeader);
        Assert.assertEquals(expectedResult, resultBatch);

    }


    @Test
    public void testHeaderFailure() throws Exception {
        SoxRequestModel batch = CreateMockSoxBatchRequest(1,1, BatchJobConstants.ACCESS_TYPE_APPLICATION, BatchJobConstants.SOX_APP_QUERY, SoxDataManager.DATA_MANAGER_APP.value());

        Boolean expectedResult = false;

        Mockito.doReturn(false).when(batchProcessor).publishRequest(headerEndPoint, batch.getHeader());

        Boolean result = batchProcessor.sendBatchHeader(batch.getHeader());

        Mockito.verify(batchProcessor, Mockito.times(1)).publishRequest(headerEndPoint, batch.getHeader());
        Assert.assertEquals(expectedResult, result);

    }

    @Test
    public void processBatchReturnsResultPairWithBatchFailure() throws Exception {
        SoxRequestModel batch = CreateMockSoxBatchRequest(1,1, BatchJobConstants.ACCESS_TYPE_APPLICATION, BatchJobConstants.SOX_APP_QUERY, SoxDataManager.DATA_MANAGER_APP.value());

        Boolean expectedResultBatch = false;
        Boolean expectedResultHeader = true;

        Mockito.doReturn(true).when(batchProcessor).publishRequest(headerEndPoint, batch.getHeader());
        Mockito.doReturn(false).when(batchProcessor).publishRequest(batchEndPoint, batch.getUserDataBatches().get(0).getUserData());

        Boolean resultHeader = batchProcessor.sendBatchHeader(batch.getHeader());
        Boolean resultBatch = batchProcessor.processBatch(batch.getUserDataBatches().get(0));

        Mockito.verify(batchProcessor).publishRequest(headerEndPoint, batch.getHeader());
        Mockito.verify(batchProcessor).publishRequest(batchEndPoint, batch.getUserDataBatches().get(0).getUserData());
        Assert.assertEquals(expectedResultHeader, resultHeader);
        Assert.assertEquals(expectedResultBatch, resultBatch);

    }


    public static SoxRequestModel CreateMockSoxBatchRequest(int batchCount, int recordCount, String accessType, String soxQuery, String dataManager)
    {
        SoxRequestModel soxBatchModel = new SoxRequestModel();

        String batchId = UUID.randomUUID().toString();
        SoxUserHeaderModel header = new SoxUserHeaderModel();
        header.setBatchId(batchId);
        header.setCreatedDate(new Timestamp(System.currentTimeMillis()));
        header.setDataManagerName(dataManager);
        header.setTotalNoOfRecords(String.valueOf(recordCount));
        header.setDataQuery(soxQuery);

        soxBatchModel.setHeader(header);

        List<SoxUserDataBatchModel> userDataBatches = new ArrayList();
        soxBatchModel.setUserDataBatches(userDataBatches);

        for (int j=0;j<batchCount;j++) {

            List<SoxUserDataModel> userDataList = new ArrayList();
            for (int i = 0; i < recordCount; i++) {
                SoxUserDataModel userData = new SoxUserDataModel();

                userData.setDatabaseName("PSP");
                userData.setBatchId(batchId);
                userData.setAccessType(accessType);
                userData.setCreatedDate(new Timestamp(System.currentTimeMillis()));
                userData.setDataManagerName(dataManager);
                userData.setTransactionId(UUID.randomUUID().toString());
                userData.setUsername("user" + String.valueOf(i));

                SoxUserAdditionalPropertiesModel additionalProperties = new SoxUserAdditionalPropertiesModel();
                additionalProperties.setAccess("access" + String.valueOf(i));
                additionalProperties.setProfile("profile" + String.valueOf(i));
                additionalProperties.setStatus("status" + String.valueOf(i));

                userData.setSoxUserAdditionalPropertiesModel(additionalProperties);

                userDataList.add(userData);
            }
            SoxUserDataBatchModel batch = new SoxUserDataBatchModel(userDataList,batchId);

            soxBatchModel.getUserDataBatches().add(batch);
        }

        return soxBatchModel;
    }

}
