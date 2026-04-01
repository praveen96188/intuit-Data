package com.intuit.sbd.payroll.psp.batchjobs.soxReport.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.dao.SoxQueryFactory;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.dao.SoxUserDAO;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.domain.SoxDataManager;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxRequestModel;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserDataBatchModel;
import com.intuit.sbd.payroll.psp.batchjobs.soxReport.models.SoxUserDataModel;
import com.intuit.sbd.payroll.psp.configuration.Database;
import com.intuit.sbd.payroll.psp.jss.util.BatchJobConstants;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;

import java.util.*;

public class SoxDbUserServiceTest {
    @InjectMocks
    @Spy
    private SoxDBUserService dbUserService;
    @Mock(name = "processor")
    private SoxBatchService mockProcessor;
    @Mock(name = "soxUserDAO")
    private SoxUserDAO mockDAO;
    @Mock(name = "soxQueryFactory")
    private SoxQueryFactory soxQueryFactory;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void executeServiceWhenNoFailedRecords() throws JsonProcessingException {

        List<SoxUserDataBatchModel> failedBatches = new ArrayList<>();
        List<Pair<SoxUserDataBatchModel, Boolean>> batchResults = new ArrayList<>();

        boolean isHeaderSuccess = true;

        SoxRequestModel batches = SoxBatchProcessorTest.CreateMockSoxBatchRequest(5,BatchJobConstants.BATCH_SIZE, dbUserService.accessType, BatchJobConstants.SOX_DB_QUERY_ORACLE, SoxDataManager.DATA_MANAGER_DB_MONOLITH.value());
        System.out.println(new ObjectMapper().writeValueAsString(batches.getUserDataBatches().get(0)));
        Mockito.doReturn(batches).when(dbUserService).createSoxRequestModel(SoxDataManager.DATA_MANAGER_DB_MONOLITH);
        Mockito.doReturn(isHeaderSuccess).when(mockProcessor).sendBatchHeader(batches.getHeader());
        Mockito.doReturn(failedBatches).when(dbUserService).processBatches(batches.getUserDataBatches());

        dbUserService.execute(BatchJobConstants.SOX_RETRY_COUNT, SoxDataManager.DATA_MANAGER_DB_MONOLITH);

        Mockito.verify(dbUserService).createSoxRequestModel(SoxDataManager.DATA_MANAGER_DB_MONOLITH);
        Mockito.verify(mockProcessor).sendBatchHeader(batches.getHeader());
        Mockito.verify(dbUserService).processBatches(batches.getUserDataBatches());

    }

    @Test
    public void executeServiceWhenRetryFailedRecords(){

        List<SoxUserDataBatchModel> failedBatches = new ArrayList<>();
        SoxRequestModel batches = SoxBatchProcessorTest.CreateMockSoxBatchRequest(5,BatchJobConstants.BATCH_SIZE, dbUserService.accessType, BatchJobConstants.SOX_DB_QUERY_ORACLE, SoxDataManager.DATA_MANAGER_DB_MONOLITH.value());

        boolean isHeaderSuccess = true;

        for(int i=0; i<5; i+=2) {
            failedBatches.add(batches.getUserDataBatches().get(i));
        }

        Mockito.doReturn(batches).when(dbUserService).createSoxRequestModel(SoxDataManager.DATA_MANAGER_DB_MONOLITH);
        Mockito.doReturn(isHeaderSuccess).when(mockProcessor).sendBatchHeader(batches.getHeader());
        Mockito.doReturn(failedBatches).when(dbUserService).processBatches(batches.getUserDataBatches());
        Mockito.doReturn(failedBatches).when(dbUserService).processBatches(failedBatches);

        dbUserService.execute(BatchJobConstants.SOX_RETRY_COUNT,SoxDataManager.DATA_MANAGER_DB_MONOLITH);

        Mockito.verify(dbUserService).createSoxRequestModel(SoxDataManager.DATA_MANAGER_DB_MONOLITH);
        Mockito.verify(mockProcessor).sendBatchHeader(batches.getHeader());
        Mockito.verify(dbUserService).processBatches(batches.getUserDataBatches());
        Mockito.verify(dbUserService, Mockito.times(BatchJobConstants.SOX_RETRY_COUNT)).processBatches(failedBatches);

    }

    @Test
    public void createSoxBatchModelReturnsBatch() {
        List<SoxUserDataModel> userData = new ArrayList<>();
        SoxRequestModel results;

        SoxRequestModel batches = SoxBatchProcessorTest.CreateMockSoxBatchRequest(5,BatchJobConstants.BATCH_SIZE, dbUserService.accessType, BatchJobConstants.SOX_DB_QUERY_ORACLE, SoxDataManager.DATA_MANAGER_DB_MONOLITH.value());

        for (SoxUserDataBatchModel batch: batches.getUserDataBatches()) {
            for (SoxUserDataModel data : batch.getUserData()) {
                userData.add(data);
            }
        }

        Mockito.doReturn(BatchJobConstants.SOX_DB_QUERY_ORACLE).when(soxQueryFactory).getSoxDataQuery(SoxDataManager.DATA_MANAGER_DB_MONOLITH);
        Mockito.doReturn(BatchJobConstants.SOX_DB_QUERY_ORACLE).when(soxQueryFactory).getDBNameQuery(Database.MONOLITH);

        Mockito.doReturn(userData).when(mockDAO).queryDatabase(SoxDataManager.DATA_MANAGER_DB_MONOLITH, dbUserService.accessType);

        results = dbUserService.createSoxRequestModel(SoxDataManager.DATA_MANAGER_DB_MONOLITH);

        Mockito.verify(mockDAO).queryDatabase(SoxDataManager.DATA_MANAGER_DB_MONOLITH, dbUserService.accessType);

        Assert.assertEquals(5, results.getUserDataBatches().size());

        int recordsCount = 0;
        for (SoxUserDataBatchModel batch: results.getUserDataBatches()) {
            for (SoxUserDataModel data: batch.getUserData()) {
                Assert.assertNotNull(data.getBatchId());
                recordsCount++;
            }
        }

        Assert.assertEquals(5*BatchJobConstants.BATCH_SIZE,recordsCount);

    }

    @Test
    public void processBatchesReturnsFailedRecords(){
        SoxRequestModel expectedFailedList = new SoxRequestModel();
        List<SoxUserDataBatchModel> actualFailedList;
        List<Pair<SoxUserDataBatchModel, Boolean>> mockBatchResults = new ArrayList();

        SoxRequestModel batches = SoxBatchProcessorTest.CreateMockSoxBatchRequest(5,BatchJobConstants.BATCH_SIZE, dbUserService.accessType, BatchJobConstants.SOX_DB_QUERY_ORACLE, SoxDataManager.DATA_MANAGER_DB_MONOLITH.value());

        expectedFailedList.setHeader(batches.getHeader());
        expectedFailedList.setUserDataBatches(new ArrayList<>());

        for(int i=0; i<5; i++) {
            Pair<SoxUserDataBatchModel, Boolean> pair;
            if(i%2 == 0){
                pair = new Pair<>(batches.getUserDataBatches().get(i), true);
            }else{
                pair = new Pair<>(batches.getUserDataBatches().get(i), false);
                expectedFailedList.getUserDataBatches().add(batches.getUserDataBatches().get(i));
            }
            mockBatchResults.add(pair);
        }

        Mockito.doReturn(mockBatchResults).when(mockProcessor).processBatches(batches.getUserDataBatches());

        actualFailedList = dbUserService.processBatches(batches.getUserDataBatches());

        Mockito.verify(mockProcessor).processBatches(batches.getUserDataBatches());

        Assert.assertEquals(expectedFailedList.getUserDataBatches().size(), actualFailedList.size());

    }
}

