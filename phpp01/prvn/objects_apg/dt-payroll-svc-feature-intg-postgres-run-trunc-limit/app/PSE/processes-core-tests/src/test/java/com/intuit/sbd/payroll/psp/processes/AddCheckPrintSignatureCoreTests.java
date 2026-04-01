package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.OfferingInfoDTO;
import com.intuit.sbd.payroll.psp.api.dtos.ServiceInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 3, 2010
 * Time: 3:14:09 PM
 */
public class AddCheckPrintSignatureCoreTests {
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
            PayrollServices.beginUnitOfWork();
            ProcessResult<CheckPrintSignature> processResult = PayrollServices.companyManager.addOrUpdateCheckPrintSignature(SourceSystemCode.QBDT, psid, readSignatureFile("signature.png"));
            assertTrue("process result", processResult.isSuccess());
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
            CheckPrintSignature checkPrintSignature = CheckPrintSignature.findCheckPrintSignature(company);
            assertNotNull("chek signature", checkPrintSignature.getSignatureImage());
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testInvalidHeight() {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<CheckPrintSignature> processResult = PayrollServices.companyManager.addOrUpdateCheckPrintSignature(SourceSystemCode.QBDT, psid, readSignatureFile("signature_invalid_height.png"));
            assertFalse("process result", processResult.isSuccess());
            assertEquals("error code", "311", processResult.getMessages().get(0).getMessageCode());
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testInvalidWidth() {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<CheckPrintSignature> processResult = PayrollServices.companyManager.addOrUpdateCheckPrintSignature(SourceSystemCode.QBDT, psid, readSignatureFile("signature_invalid_width.png"));
            assertFalse("process result", processResult.isSuccess());
            assertEquals("error code", "311", processResult.getMessages().get(0).getMessageCode());
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testInvalidResolution() {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<CheckPrintSignature> processResult = PayrollServices.companyManager.addOrUpdateCheckPrintSignature(SourceSystemCode.QBDT, psid, readSignatureFile("signature_invalid_resolution.png"));
            assertFalse("process result", processResult.isSuccess());
            assertEquals("error code", "312", processResult.getMessages().get(0).getMessageCode());
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testInvalidType() {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<CheckPrintSignature> processResult = PayrollServices.companyManager.addOrUpdateCheckPrintSignature(SourceSystemCode.QBDT, psid, readSignatureFile("signature_invalid_type.bmp"));
            assertFalse("process result", processResult.isSuccess());
            assertEquals("error code", "310", processResult.getMessages().get(0).getMessageCode());
            PayrollServices.commitUnitOfWork();
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            PayrollServices.rollbackUnitOfWork();
        }
    }

    @Test
    public void testInvalidReadError() {
        try {
            PayrollServices.beginUnitOfWork();
            ProcessResult<CheckPrintSignature> processResult = PayrollServices.companyManager.addOrUpdateCheckPrintSignature(SourceSystemCode.QBDT, psid, readSignatureFile("signature_bad_file.png"));
            assertFalse("process result", processResult.isSuccess());
            assertEquals("error code", "313", processResult.getMessages().get(0).getMessageCode());
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
        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid);

        DataLoadServices.addTaxService(company1);

        // Add CheckDistribution service
        PayrollServices.beginUnitOfWork();

        ServiceInfoDTO companyService = dataloader.getTestCheckDistributionCompanyService(lastPaycheckId);
        ProcessResult serviceAddProcessResult = PayrollServices.companyManager.addService(company1.getSourceSystemCd(), company1.getSourceCompanyId(), companyService);
        PayrollServices.commitUnitOfWork();

        if (!serviceAddProcessResult.isSuccess()) {
            throw new RuntimeException("Could not create service: " + serviceAddProcessResult.getErrorMessages().toString());
        }

        // Assert
        PayrollServices.beginUnitOfWork();
        Company foundCompany = Company.findCompany(company1.getSourceCompanyId(), company1.getSourceSystemCd());
        CompanyService serviceInfo = CompanyService.findCompanyService(foundCompany, ServiceCode.CheckDistribution);

        assertNotNull(serviceInfo);
        Assert.assertEquals("Service code", ServiceCode.CheckDistribution, serviceInfo.getService().getServiceCd());
        PayrollServices.commitUnitOfWork();
    }
}
