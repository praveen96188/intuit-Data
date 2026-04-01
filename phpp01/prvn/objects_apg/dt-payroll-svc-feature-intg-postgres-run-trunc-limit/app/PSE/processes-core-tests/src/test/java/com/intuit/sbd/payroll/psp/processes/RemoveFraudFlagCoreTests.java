package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import static junit.framework.Assert.assertEquals;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Jul 15, 2008
 * Time: 2:06:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class RemoveFraudFlagCoreTests {
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
    public void removeFraudFlagCoreNullCompanyId() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.removeFraudFlag(SourceSystemCode.QBOE, null);
        PayrollServices.commitUnitOfWork();
        assertFalse(result.isSuccess());

        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "138", errorMessage.getMessageCode());
        assertEquals("Error message", "Source Company ID is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void removeFraudFlagCoreNullSourceSystemCd() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.removeFraudFlag(null, "1234567");
        PayrollServices.commitUnitOfWork();
        assertFalse(result.isSuccess());

        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "137", errorMessage.getMessageCode());
        assertEquals("Error message", "Source System Code is not specified.",
                errorMessage.getMessage());
    }

    @Test
    public void removeFraudFlagCoreCompanyDNE() {
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.removeFraudFlag(SourceSystemCode.QBOE, "1234567");
        PayrollServices.commitUnitOfWork();

        assertFalse(result.isSuccess());
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "169", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:1234567 does not exist.",
                errorMessage.getMessage());
    }

    @Test
    public void fraudFlagNotSet() {
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> addCompanyResult = DataLoader.addCompany(company1);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Add Company", addCompanyResult);

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.removeFraudFlag(SourceSystemCode.QBOE, company1.getCompanyId());
        PayrollServices.commitUnitOfWork();

        assertFalse(result.isSuccess());
        assertEquals("Messages size", 1, result.getMessages().size());
        Message errorMessage = result.getMessages().get(0);
        assertEquals("Error message code", "300", errorMessage.getMessageCode());
        assertEquals("Error message", "Company QBOE:123456 does not currently have its fraud flag set.",
                errorMessage.getMessage());
    }

    @Test
    public void removeFraudFlagCoreSuccess() {
        PayrollServices.beginUnitOfWork();
        DataLoader dataloader = new DataLoader();
        CompanyDTO company1 = dataloader.getTestIntuitCompany();
        ProcessResult<Company> addCompanyResult = DataLoader.addCompany(company1);
        PayrollServices.commitUnitOfWork();

        assertSuccess("Add Company", addCompanyResult);

        PayrollServices.beginUnitOfWork();
        Company company = addCompanyResult.getResult();
        company = PayrollServices.entityFinder.findById(Company.class, company.getId());
        company.setFraudFlag();
        PayrollServices.commitUnitOfWork();
        
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> result = PayrollServices.companyManager.removeFraudFlag(SourceSystemCode.QBOE, company.getSourceCompanyId());
        PayrollServices.commitUnitOfWork();

        assertSuccess("removeFraudFlag", result);

        assertEquals("FraudFlag", false, result.getResult().getIsFlaggedForFraud());
    }    
}
