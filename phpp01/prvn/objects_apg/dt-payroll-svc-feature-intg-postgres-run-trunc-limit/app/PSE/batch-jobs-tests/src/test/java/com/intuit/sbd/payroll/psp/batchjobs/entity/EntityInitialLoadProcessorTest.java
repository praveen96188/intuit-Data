package com.intuit.sbd.payroll.psp.batchjobs.entity;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.workflows.publishstatus.PublishStatusWorkflowState;
import com.intuit.sbg.shared.batchjob.BatchJobManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import junit.framework.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EntityInitialLoadProcessorTest {

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
    public void testEntityIntialLoadProcessor_PublishToEMS_PSIDS() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","10", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test","-psIds", psid, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.PUBLISHED_INTERNAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }

    @Test
    public void testEntityIntialLoadProcessor_PublishToEMS() throws Exception {
        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid,true,ServiceCode.Cloud,ServiceCode.ViewMyPaycheck);
        DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);
        PayrollServices.beginUnitOfWork();
        Company tempCompany = Company.findCompany(psid,SourceSystemCode.QBDT);
        tempCompany.setIAMRealmId("3674367");
        Application.save(tempCompany);
        PayrollServices.commitUnitOfWork();

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","10", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.PUBLISHED_INTERNAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }

    @Test
    public void testEntityIntialLoadProcessor_PublishToEMS_10_Company() throws Exception {

        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";
        //Setup
        List<String> psIdList = new ArrayList();
        for (int i=0;i<10;i++) {
            String psid = UUID.randomUUID().toString();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);
            psIdList.add(psid);
            PayrollServices.beginUnitOfWork();
            Company tempCompany = Company.findCompany(psid, SourceSystemCode.QBDT);
            tempCompany.setIAMRealmId("3674367");
            Application.save(tempCompany);
            PayrollServices.commitUnitOfWork();
        }

        Assert.assertEquals(psIdList.size(),10);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","10", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        for (String psId : psIdList) {
            Company company1 = DataLoadServices.getCompanyNoEagerLoad(psId);
            String publishStatus = company1.getPublishStatus();
            Assert.assertEquals(PublishStatusWorkflowState.PUBLISHED_INTERNAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
        }
    }

    @Test
    public void testEntityIntialLoadProcessor_PublishToEMS_10_Company_NoParameter() throws Exception {

        String licenseNumber = "12345678901234567890";
        String eoc = "09876543210987654321";
        //Setup
        List<String> psIdList = new ArrayList();
        for (int i=0;i<10;i++) {
            String psid = UUID.randomUUID().toString();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.Cloud, ServiceCode.ViewMyPaycheck);
            DataLoadServices.addEntitlementUnit(company, licenseNumber, eoc);
            psIdList.add(psid);
            PayrollServices.beginUnitOfWork();
            Company tempCompany = Company.findCompany(psid, SourceSystemCode.QBDT);
            tempCompany.setIAMRealmId("3674367");
            Application.save(tempCompany);
            PayrollServices.commitUnitOfWork();
        }

        Assert.assertEquals(psIdList.size(),10);

        String[] args = {"run","EntityInitialLoadProcessor","-topicName","sbseg-dtpayroll-entity-internal-test", "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        for (String psId : psIdList) {
            Company company1 = DataLoadServices.getCompanyNoEagerLoad(psId);
            String publishStatus = company1.getPublishStatus();
            Assert.assertEquals(PublishStatusWorkflowState.PUBLISHED_INTERNAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(1))));
        }
    }

    @Test
    public void testEntityIntialLoadProcessor_PublishToEMS_10_PSIDS() throws Exception {
        //Setup
        List<String> psIdList = new ArrayList();
        for (int i=0;i<10;i++){
            String psid = UUID.randomUUID().toString();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
            psIdList.add(psid);
        }
        String listOfPsId = psIdList.toString().replace("[","").replace("]","").replaceAll("\\s", "");

        Assert.assertEquals(psIdList.size(),10);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","10","-chunkSize","5", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test","-psIds", listOfPsId, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        for (String psIds : psIdList){
            Company company1 = DataLoadServices.getCompanyNoEagerLoad(psIds);
            String publishStatus = company1.getPublishStatus();
            Assert.assertEquals(PublishStatusWorkflowState.PUBLISHED_INTERNAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
        }
    }

    @Test
    public void testEntityIntialLoadProcessor_PublishToEMS_10_PSIDS_NoParameters() throws Exception {
        //Setup
        List<String> psIdList = new ArrayList();
        for (int i=0;i<10;i++){
            String psid = UUID.randomUUID().toString();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
            psIdList.add(psid);
        }
        String listOfPsId = psIdList.toString().replace("[","").replace("]","").replaceAll("\\s", "");

        Assert.assertEquals(psIdList.size(),10);

        String[] args = {"run","EntityInitialLoadProcessor", "-topicName","sbseg-dtpayroll-entity-internal-test","-psIds", listOfPsId, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        for (String psIds : psIdList){
            Company company1 = DataLoadServices.getCompanyNoEagerLoad(psIds);
            String publishStatus = company1.getPublishStatus();
            Assert.assertEquals(PublishStatusWorkflowState.PUBLISHED_INTERNAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(1))));
        }

    }

    @Test
    public void testEntityIntialLoadProcessor_Failure_PublishToEMS() throws Exception {
        //Setup
        List<String> psIdList = new ArrayList();
        for (int i=0;i<10;i++){
            String psid = UUID.randomUUID().toString();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid,true,ServiceCode.Cloud);
            psIdList.add(psid);
        }

        String listOfPsId = psIdList.toString().replace("[","").replace("]","").replaceAll("\\s", "");

        Assert.assertEquals(psIdList.size(),10);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","10","-chunkSize","5", "-targetedService", "EMS", "-publishStatusMode", "1",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        for (String psId : psIdList){
            Company company1 = DataLoadServices.getCompanyNoEagerLoad(psId);
            String publishStatus = company1.getPublishStatus();
            Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
        }

    }

    @Test
    public void testEntityIntialLoadProcessor_chunkSize_negativeValue() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","-10", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-psIds", psid, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }

    @Test
    public void testEntityIntialLoadProcessor_chunkSize_alphanumeric() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","abc12", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-psIds", psid, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }

    @Test
    public void testEntityIntialLoadProcessor_batchSize_negativeValue() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","abc67","-chunkSize","10", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }

    @Test
    public void testEntityIntialLoadProcessor_batchSize_alphanumeric() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","-50","-chunkSize","10", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }

    @Test
    public void testEntityIntialLoadProcessor_wrong_topicName() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","10", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-testtest", "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }

    @Test
    public void testEntityIntialLoadProcessor_targetedService_notMatched() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","10", "-targetedService", "ABC", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-psIds", psid, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }

    @Test
    public void testEntityIntialLoadProcessor_lastProcessedTime_notMatched() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","10", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-psIds", psid, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }


    @Test
    public void testEntityIntialLoadProcessor_PublishToEVS() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","10", "-targetedService", "EVS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-psIds", psid, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.PUBLISHED_INTERNAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(1))));
    }

    @Test
    public void testEntityIntialLoadProcessor_PublishToEVS_10_Company() throws Exception {
        //Setup
        List<String> psIdList = new ArrayList();
        for (int i=0;i<10;i++) {
            String psid = UUID.randomUUID().toString();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
            psIdList.add(psid);
        }
        String listOfPsId = psIdList.toString().replace("[","").replace("]","").replaceAll("\\s", "");

        Assert.assertEquals(psIdList.size(),10);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","10","-chunkSize","2", "-targetedService", "EVS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test","-psIds",listOfPsId, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        for (String psId : psIdList) {
            Company company1 = DataLoadServices.getCompanyNoEagerLoad(psId);
            String publishStatus = company1.getPublishStatus();
            Assert.assertEquals(PublishStatusWorkflowState.PUBLISHED_INTERNAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(1))));
        }
    }

    @Test
    public void testEntityIntialLoadProcessor_PublishToEVSHourly() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","10", "-targetedService", "EVS_HOURLY", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-psIds", psid, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.PUBLISHED_INTERNAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(2))));
    }

    @Test
    public void testEntityIntialLoadProcessor_Failure_PublishToEVS() throws Exception {
        //Setup
        List<String> psIdList = new ArrayList();
        for (int i=0;i<10;i++){
            String psid = UUID.randomUUID().toString();
            Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);
            psIdList.add(psid);
        }

        String listOfPsId = psIdList.toString().replace("[","").replace("]","").replaceAll("\\s", "");

        Assert.assertEquals(psIdList.size(),10);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","10","-chunkSize","5", "-targetedService", "EVS", "-publishStatusMode", "1",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test","-psIds",listOfPsId, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        for (String psId : psIdList){
            Company company1 = DataLoadServices.getCompanyNoEagerLoad(psId);
            String publishStatus = company1.getPublishStatus();
            Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
        }

    }

    @Test
    public void testEntityIntialLoadProcessor_chunkSize_negativeValue_EVS() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","-10", "-targetedService", "EVS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-psIds", psid, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }

    @Test
    public void testEntityIntialLoadProcessor_batchSize_negativeValue_EVS() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","-50","-chunkSize","10", "-targetedService", "EVS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01T01:02:03.123Z",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-psIds", psid, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }


    @Test
    public void testEntityIntialLoadProcessor_lastProcessedTime_notMatched_EVS() throws Exception {
        //Setup
        String psid = UUID.randomUUID().toString();
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        Assert.assertNotNull(company);

        String[] args = {"run","EntityInitialLoadProcessor","-batchSize","50","-chunkSize","10", "-targetedService", "EMS", "-publishStatusMode", "0",
                "-startTime", "2021-02-01T01:02:03.123Z", "-lastProcessedTime", "2021-03-01",
                "-topicName","sbseg-dtpayroll-entity-internal-test", "-psIds", psid, "-namedQuery", "FIND_COMPANIES_FOR_INITIAL_LOAD"};

        //Run test
        BatchJobManager.executeCommand(args);

        //Verify
        Company company1 = DataLoadServices.getCompanyNoEagerLoad(psid);
        String publishStatus = company1.getPublishStatus();
        Assert.assertEquals(PublishStatusWorkflowState.INITIAL.getValue(), Integer.parseInt(String.valueOf(publishStatus.charAt(0))));
    }

}