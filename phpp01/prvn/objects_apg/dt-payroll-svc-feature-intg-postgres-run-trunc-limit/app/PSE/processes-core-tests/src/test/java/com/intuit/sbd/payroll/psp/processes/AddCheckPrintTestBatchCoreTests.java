package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.CheckPrintBatch;
import com.intuit.sbd.payroll.psp.domain.CheckPrintSignature;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyPaycheckBatch;
import com.intuit.sbd.payroll.psp.domain.CompanyService;
import com.intuit.sbd.payroll.psp.domain.OfferingCode;
import com.intuit.sbd.payroll.psp.domain.ServiceCode;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 3, 2010
 * Time: 6:42:19 PM
 */
public class AddCheckPrintTestBatchCoreTests {
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
    public void happyPathTest() throws Exception {
        PayrollServices.beginUnitOfWork();
        ProcessResult<CheckPrintSignature> checkPrintSignatureProcessResult = PayrollServices.companyManager.addOrUpdateCheckPrintSignature(SourceSystemCode.QBDT, psid, readSignatureFile("signature.png"));
        assertTrue("process result", checkPrintSignatureProcessResult.isSuccess());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyPaycheckBatch> processResult = PayrollServices.companyManager.addCheckPrintTestBatch(SourceSystemCode.QBDT, psid);
        assertTrue("process result", processResult.isSuccess());
        PayrollServices.commitUnitOfWork();
        SpcfUniqueId id = processResult.getResult().getId();

        PayrollServices.beginUnitOfWork();
        CheckPrintBatch checkPrintBatch = Application.findById(CheckPrintBatch.class, id);
        assertNotNull("batch", checkPrintBatch);
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testCompanyDNE() {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<CompanyPaycheckBatch> processResult = PayrollServices.companyManager.addCheckPrintTestBatch(SourceSystemCode.QBDT, "125");
            assertFalse("process result", processResult.isSuccess());
            assertEquals("error code", "169", processResult.getMessages().get(0).getMessageCode());
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testCompanyDoesNotHaveSignature() {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<CompanyPaycheckBatch> processResult = PayrollServices.companyManager.addCheckPrintTestBatch(SourceSystemCode.QBDT, psid);
            assertFalse("process result", processResult.isSuccess());
            assertEquals("error code", "315", processResult.getMessages().get(0).getMessageCode());
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    private byte[] readSignatureFile(String pFileName) throws Exception {
        byte imgData[] = null;
        RandomAccessFile rf = null;
        try {
            rf = new RandomAccessFile(Application.findFileOnClassPath("resources/" + pFileName), "r");

            int size = (int) rf.length();
            imgData = new byte[size];
            rf.readFully(imgData);
            rf.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return imgData;
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

        assertSuccess(pr);        
        PayrollServices.commitUnitOfWork();

        // Add Assisted service
        DataLoadServices.addTaxService(company1);        

        // Add CheckDistribution service
        PayrollServices.beginUnitOfWork();
        ServiceInfoDTO companyService = dataloader.getTestCheckDistributionCompanyService(lastPaycheckId);
        ProcessResult<CompanyService> serviceAddProcessResult = PayrollServices.companyManager.addService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), companyService);
        PayrollServices.commitUnitOfWork();

        assertSuccess(serviceAddProcessResult);

        // Assert
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        CompanyService serviceInfo = CompanyService.findCompanyService(foundCompany, ServiceCode.CheckDistribution);

        assertNotNull(serviceInfo);
        Assert.assertEquals("Service code", ServiceCode.CheckDistribution, serviceInfo.getService().getServiceCd());
        PayrollServices.commitUnitOfWork();
    }
}
