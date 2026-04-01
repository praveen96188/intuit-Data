package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LedgerOperationDTO;
import com.intuit.sbd.payroll.psp.api.dtos.LedgerOperationJobDTO;
import com.intuit.sbd.payroll.psp.domain.LedgerOperationJob;
import com.intuit.sbd.payroll.psp.domain.LedgerOperationJobStatus;
import com.intuit.sbd.payroll.psp.domain.LedgerOperationJobType;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

/**
 * User: dweinberg
 * Date: 11/9/12
 * Time: 1:56 PM
 */
public class QueueLedgerOperationJobTests {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHappyPath() {
        LedgerOperationJobDTO jobDTO = new LedgerOperationJobDTO();
        jobDTO.setOriginalFile("Blah");
        jobDTO.setType(LedgerOperationJobType.BulkDebit);
        LedgerOperationDTO operationDTO = new LedgerOperationDTO();
        operationDTO.setAmount(new SpcfMoney("1.00"));
        operationDTO.setCheckDate(new DateDTO("2012-12-31"));
        operationDTO.setLawId("66");
        operationDTO.setMemo("Memo");
        operationDTO.setOriginalLegalName("My Legal Name");
        operationDTO.setSourceCompanyId("123456789");
        operationDTO.setSourceSystemCd(SourceSystemCode.QBDT);
        jobDTO.getLedgerOperations().add(operationDTO);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.batchJobManager.addLedgerOperationJob(jobDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        LedgerOperationJob job = assertOne(Application.find(LedgerOperationJob.class));
        assertEquals(LedgerOperationJobStatus.Created, job.getStatus());
        assertSuccess(PayrollServices.batchJobManager.queueLedgerOperationJob(job.getId()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(job);
        assertEquals(LedgerOperationJobStatus.Queued, job.getStatus());
        PayrollServices.rollbackUnitOfWork();

    }

}
