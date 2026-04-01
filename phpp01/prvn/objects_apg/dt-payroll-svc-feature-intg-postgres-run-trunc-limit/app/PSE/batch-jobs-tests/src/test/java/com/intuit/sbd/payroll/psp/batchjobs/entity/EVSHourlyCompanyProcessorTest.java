package com.intuit.sbd.payroll.psp.batchjobs.entity;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbg.shared.batchjob.BatchJobManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EVSHourlyCompanyProcessorTest {

    @AfterClass
    public static void afterClass() {

    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testEVSCompanyProcessor_Publish() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
        String batchStartTime = "2021-02-01T01:02:03.123Z";
        Assert.assertNotNull(company);

        String[] args = {"run", "EVSCompanyProcessor", "-batchSize", "50", "-chunkSize", "10", "-targetedService", "EVS", "-publishStatusMode", "0",
                "-startTime", batchStartTime,
                "-topicName", "sbseg-dtpayroll-entity-internal-test", "-psIds", psid};

        //Run test
        BatchJobManager.executeCommand(args);

        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        String updatedLastProcessedTime = SystemParameter.findStringValue(SystemParameter.Code.EVS_LAST_PROCESSED_TIME);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(updatedLastProcessedTime, batchStartTime);
    }

    @Test
    public void testEVSCompanyProcessor_Publish_10_Company() throws Exception {
        //Setup
        List<String> psIdList = new ArrayList();
        String batchStartTime = "2021-02-01T01:02:03.123Z";
        for (int i=0;i<10;i++) {
            String psid = UUID.randomUUID().toString();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
            psIdList.add(psid);
            Assert.assertNotNull(company);
        }
        String listOfPsId = psIdList.toString().replace("[","").replace("]","").replaceAll("\\s", "");

        Assert.assertEquals(psIdList.size(),10);

        String[] args = {"run", "EVSCompanyProcessor", "-batchSize", "10", "-chunkSize", "5", "-targetedService", "EVS", "-publishStatusMode", "0",
                "-startTime", batchStartTime,
                "-topicName", "sbseg-dtpayroll-entity-internal-test","-psIds", listOfPsId};

        //Run test
        BatchJobManager.executeCommand(args);
        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        String updatedLastProcessedTime = SystemParameter.findStringValue(SystemParameter.Code.EVS_LAST_PROCESSED_TIME);
        PayrollServices.commitUnitOfWork();
        Assert.assertEquals(updatedLastProcessedTime, batchStartTime);

    }

    @Test
    public void testEVSCompanyProcessor_wrong_targetedService() throws Exception {
        //Setup
        List<String> companyPsIdList = new ArrayList();
        String batchStartTime = "2021-02-01T01:02:03.123Z";
        for (int i=0;i<10;i++) {
            String psid = UUID.randomUUID().toString();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
            companyPsIdList.add(psid);
            Assert.assertNotNull(company);
        }
        String listOfPsId = companyPsIdList.toString().replace("[","").replace("]","").replaceAll("\\s", "");

        Assert.assertEquals(companyPsIdList.size(),10);

        String[] args = {"run", "EVSCompanyProcessor", "-batchSize", "10", "-chunkSize", "2", "-targetedService", "ABC", "-publishStatusMode", "0",
                "-startTime", batchStartTime,
                "-topicName", "sbseg-dtpayroll-entity-internal-test","-psIds", listOfPsId};

        //Run test
        BatchJobManager.executeCommand(args);
        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        String updatedLastProcessedTime = SystemParameter.findStringValue(SystemParameter.Code.EVS_LAST_PROCESSED_TIME);
        PayrollServices.commitUnitOfWork();
        Assert.assertNotSame(updatedLastProcessedTime, batchStartTime);
    }

    @Test
    public void testEVSCompanyProcessor_wrong_topicName() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
        String batchStartTime = "2021-02-01T01:02:03.123Z";
        Assert.assertNotNull(company);

        String[] args = {"run", "EVSCompanyProcessor", "-batchSize", "50", "-chunkSize", "10", "-targetedService", "EVS", "-publishStatusMode", "0",
                "-startTime", batchStartTime,
                "-topicName", "sbseg-dtpayroll-entity-internal-testtest", "-psIds", psid};

        //Run test
        BatchJobManager.executeCommand(args);

        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        String updatedLastProcessedTime = SystemParameter.findStringValue(SystemParameter.Code.EVS_LAST_PROCESSED_TIME);
        PayrollServices.commitUnitOfWork();
        Assert.assertNotSame(updatedLastProcessedTime, batchStartTime);
    }


    @Test
    public void testEVSCompanyProcessor_chunkSize_alphanumeric() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
        String batchStartTime = "2021-02-01T01:02:03.123Z";
        Assert.assertNotNull(company);

        String[] args = {"run", "EVSCompanyProcessor", "-batchSize", "50", "-chunkSize", "abc123", "-targetedService", "EVS", "-publishStatusMode", "0",
                "-startTime", batchStartTime,
                "-topicName", "sbseg-dtpayroll-entity-internal-test", "-psIds", psid};

        //Run test
        BatchJobManager.executeCommand(args);

        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        String updatedLastProcessedTime = SystemParameter.findStringValue(SystemParameter.Code.EVS_LAST_PROCESSED_TIME);
        PayrollServices.commitUnitOfWork();
        Assert.assertNotSame(updatedLastProcessedTime, batchStartTime);
    }

    @Test
    public void testEVSCompanyProcessor_chunkSize_negativeValue() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
        String batchStartTime = "2021-02-01T01:02:03.123Z";
        Assert.assertNotNull(company);

        String[] args = {"run", "EVSCompanyProcessor", "-batchSize", "50", "-chunkSize", "-10", "-targetedService", "EVS", "-publishStatusMode", "0",
                "-startTime", batchStartTime,
                "-topicName", "sbseg-dtpayroll-entity-internal-test", "-psIds", psid};

        //Run test
        BatchJobManager.executeCommand(args);

        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        String updatedLastProcessedTime = SystemParameter.findStringValue(SystemParameter.Code.EVS_LAST_PROCESSED_TIME);
        PayrollServices.commitUnitOfWork();
        Assert.assertNotSame(updatedLastProcessedTime, batchStartTime);
    }

    @Test
    public void testEDSCompanyProcessor_wrong_time_format() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
        String batchStartTime = "2021-02-01T01:02:03.12";
        Assert.assertNotNull(company);

        String[] args = {"run", "EVSCompanyProcessor", "-batchSize", "50", "-chunkSize", "-10", "-targetedService", "EVS", "-publishStatusMode", "0",
                "-startTime", batchStartTime,
                "-topicName", "sbseg-dtpayroll-entity-internal-dev", "-psIds", psid};

        //Run test
        BatchJobManager.executeCommand(args);

        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        String updatedLastProcessedTime = SystemParameter.findStringValue(SystemParameter.Code.EVS_LAST_PROCESSED_TIME);
        PayrollServices.commitUnitOfWork();
        Assert.assertNotSame(updatedLastProcessedTime, batchStartTime);
    }

    @Test
    public void testEVSCompanyProcessor_Publish_10_Company_NoParameters() throws Exception {
        //Setup
        List<String> psIdList = new ArrayList();
        String batchStartTime = "2021-02-01T01:02:03.123Z";
        for (int i=0;i<10;i++) {
            String psid = UUID.randomUUID().toString();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
            psIdList.add(psid);
            Assert.assertNotNull(company);
        }
        String listOfPsId = psIdList.toString().replace("[","").replace("]","").replaceAll("\\s", "");

        Assert.assertEquals(psIdList.size(),10);

        String[] args = {"run", "EVSCompanyProcessor", "-topicName", "sbseg-dtpayroll-entity-internal-test","-psIds", listOfPsId};

        //Run test
        BatchJobManager.executeCommand(args);
        PayrollServices.beginUnitOfWork();
        //Set system parameter to enable controller
        String updatedLastProcessedTime = SystemParameter.findStringValue(SystemParameter.Code.EVS_LAST_PROCESSED_TIME);
        PayrollServices.commitUnitOfWork();
        Assert.assertNotNull(updatedLastProcessedTime);
    }
}
