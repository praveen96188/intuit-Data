package com.intuit.sbd.payroll.psp.processes;


import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.CheckPrintBatchStatus;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyPaycheckBatch;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 3, 2010
 * Time: 3:46:00 PM
 */
public class UpdateCheckPrintBatchStatusCoreTests {
    private DataLoader dataloader = new DataLoader();
    private String psid = "12345678";
    private long paycheckNumber = 10;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        // add check distribution company
        createCompany(psid, paycheckNumber);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void happyPathTest() {
        try {
            String batchId = createPrintBatch();

            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.batchJobManager.updateCheckPrintBatchStatus(batchId, CheckPrintBatchStatus.Pending);
            assertTrue("process result", processResult.isSuccess());
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            CompanyPaycheckBatch checkPrintBatch = Application.findById(CompanyPaycheckBatch.class, SpcfUniqueId.createInstance(batchId));
            assertNotNull("batch", checkPrintBatch);
            assertEquals("batch status", CheckPrintBatchStatus.Pending, checkPrintBatch.getCheckPrintBatchStatusCode());
            assertNull("batch message", checkPrintBatch.getCheckPrintBatchMessage());
            assertNull("batch print date", checkPrintBatch.getSentToPrinter());
            PayrollServices.rollbackUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testBatchDNE() {
        try {
            String batchId = "f5263743-7cf0-42fc-b164-25ea68315845";

            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.batchJobManager.updateCheckPrintBatchStatus(batchId, CheckPrintBatchStatus.Pending);
            assertFalse("process result", processResult.isSuccess());
            assertEquals("error code", "314", processResult.getMessages().get(0).getMessageCode());
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private void createCompany(String psid, long lastPaycheckId) {
        // Add a company with the right status
        PayrollServices.beginUnitOfWork();
        CompanyDTO companyDTO = dataloader.getTestIntuitCompany();
        companyDTO.setPsId(psid);
        companyDTO.setCompanyId(psid);
        companyDTO.setSourceSystemCd(SourceSystemCode.QBDT);
        ProcessResult<Company> pr = PayrollServices.companyManager.addCompany(companyDTO);
        Company company1 = pr.getResult();

        if (!pr.isSuccess()) {
            throw new RuntimeException("Could not create company: " + pr.getErrorMessages().toString());
        }
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addTaxService(company1);

        DataLoadServices.addCheckDistributionService(company1);
        

        // Assert
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        CompanyService serviceInfo = CompanyService.findCompanyService(foundCompany, ServiceCode.CheckDistribution);

        assertNotNull(serviceInfo);
        Assert.assertEquals("Service code", ServiceCode.CheckDistribution, serviceInfo.getService().getServiceCd());
        PayrollServices.commitUnitOfWork();
    }

    private String createPrintBatch() {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyPaycheckBatch checkPrintBatch = new CompanyPaycheckBatch();
        checkPrintBatch.setCompany(company);
        checkPrintBatch.setPaycheckDate(SpcfCalendar.getNow());
        checkPrintBatch.setCheckPrintBatchStatusCode(CheckPrintBatchStatus.SentToPrinter);
        checkPrintBatch.setCheckPrintBatchMessage("");
        checkPrintBatch = Application.save(checkPrintBatch);
        PayrollServices.commitUnitOfWork();
        return checkPrintBatch.getId().toString();
    }
}
