package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.SUICreditsJobDTO;
import com.intuit.sbd.payroll.psp.domain.SUICreditsJob;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

/**
 * User: dweinberg
 * Date: 9/26/13
 * Time: 2:24 PM
 */
public class CreateSUICreditsJobTests {
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
        Application.beginUnitOfWork();
        assertSuccess(PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, "MA-1700HI-PAYMENT")));
        Application.commitUnitOfWork();
        Application.beginUnitOfWork();
        SUICreditsJob suiCreditsJob = assertOne(Application.find(SUICreditsJob.class));
        assertEquals(2013, suiCreditsJob.getYear());
        assertEquals(1, suiCreditsJob.getQuarter());
        assertEquals("MA-1700HI-PAYMENT", suiCreditsJob.getPaymentTemplate().getPaymentTemplateCd());
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testBadDates() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(1, 1, "MA-1700HI-PAYMENT"));
        PSP_PRAssert.assertContains(6002, MessageInfo.MessageLevel.ERROR, processResult);

        processResult = PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 0, "MA-1700HI-PAYMENT"));
        PSP_PRAssert.assertContains(6002, MessageInfo.MessageLevel.ERROR, processResult);

        processResult = PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 5, "MA-1700HI-PAYMENT"));
        PSP_PRAssert.assertContains(6002, MessageInfo.MessageLevel.ERROR, processResult);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void badTemplate() {
        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, "MA-SPOON-IS-TOO-BIG"));
        PSP_PRAssert.assertContains(10112, MessageInfo.MessageLevel.ERROR, processResult);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testAlreadyExistingAllTemplates() {
        Application.beginUnitOfWork();
        assertSuccess(PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, null)));
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, "MA-1700HI-PAYMENT"));
        PSP_PRAssert.assertContains(5000, MessageInfo.MessageLevel.ERROR, processResult);
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        processResult = PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, "MA-1700HI-PAYMENT"));
        PSP_PRAssert.assertContains(5000, MessageInfo.MessageLevel.ERROR, processResult);
        Application.rollbackUnitOfWork();
    }

    @Test
    public void testAlreadyExistingSpecificTemplate() {
        Application.beginUnitOfWork();
        assertSuccess(PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, "MA-1700HI-PAYMENT")));
        Application.commitUnitOfWork();

        Application.beginUnitOfWork();
        ProcessResult processResult = PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, "MA-1700HI-PAYMENT"));
        PSP_PRAssert.assertContains(5000, MessageInfo.MessageLevel.ERROR, processResult);
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, null));
        PSP_PRAssert.assertContains(5000, MessageInfo.MessageLevel.ERROR, processResult);
        Application.rollbackUnitOfWork();

        Application.beginUnitOfWork();
        assertSuccess(PayrollServices.batchJobManager.createSUICreditsJob(new SUICreditsJobDTO(2013, 1, "NV-NUCS4072-PAYMENT")));
        Application.commitUnitOfWork();
    }
}
