package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * User: mvillani
 * Date: Nov 15, 2007
 * Time: 11:52:07 AM
 */
public class UpdateCompanyFundingModelCoreTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 10, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }


    @Test
    public void testSucessfulUpdateFundingModel() {
        PayrollServices.beginUnitOfWork();

        // Load company data
        Company1Dataloader company1DL = new Company1Dataloader();
        company1DL.persistCompany1();

        // Update Funding Model to a Two Day Funding Model  - Default funding model is Five Day
        FundingModel fundingModel = Application.findById(FundingModel.class, FundingModel.Codes.TWO_DAY);

        ProcessResult<Company> processResult = PayrollServices.companyManager.updateCompanyFundingModel(
                SourceSystemCode.valueOf(company1DL.getCompany1().getSourceSystemCd().toString()),
                company1DL.getCompany1().getCompanyId(), fundingModel);
        PayrollServices.commitUnitOfWork();

        assertSuccess("updateCompanyFundingModel", processResult);

        PayrollServices.beginUnitOfWork();

        //Retrieve the updated company
        Company company = PayrollServices.entityFinder.findById(Company.class, processResult.getResult().getId());

        // Verify that the funding model has been updated
        assertEquals("Funding Model:", company.getFundingModel(), fundingModel);

        // Update Funding Model back to a Five Day funding model
        fundingModel = PayrollServices.entityFinder.findById(FundingModel.class, FundingModel.Codes.FIVE_DAY);

        processResult = PayrollServices.companyManager.updateCompanyFundingModel(
                SourceSystemCode.valueOf(company1DL.getCompany1().getSourceSystemCd().toString()),
                company1DL.getCompany1().getCompanyId(), fundingModel);
        PayrollServices.commitUnitOfWork();

        out.println(processResult);

        // validate error count
        assertTrue("Number of Errors:", processResult.getMessages().size() == 0);

        //Retrieve the updated company
        company = processResult.getResult();

        // Verify that the funding model has been updated
        assertEquals("Funding Model:", company.getFundingModel(), fundingModel);
        assertEquals("Number of Funding Model Changed events", 2, CompanyEvent.getEventCountByType(company, EventTypeCode.CompanyFundingModelChanged));

    }


    /**
     * Test error message 169 - Company Does Not Exist
     */
    @Test
    public void testCompanyDoesNotExist() {
        // Load company data
        PayrollServices.beginUnitOfWork();
        Company1Dataloader company1DL = new Company1Dataloader();
        company1DL.persistCompany1();
        PayrollServices.commitUnitOfWork();

        // Call process with invalid company
        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> processResult = PayrollServices.companyManager.updateCompanyFundingModel(
                SourceSystemCode.valueOf(company1DL.getCompany1().getSourceSystemCd().toString()),
                "InvalidCompanyId", null);
        PayrollServices.commitUnitOfWork();

        out.println(processResult);

        // Verify error count
        assertEquals("Number of Errors: ", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());

        // Verify that the correct message string has returned
        assertEquals("Error Message", "Company QBOE:InvalidCompanyId does not exist.", message.getMessage());
    }


    @Test
    public void testInvalidCompanyParameters() {

        PayrollServices.beginUnitOfWork();
        ProcessResult<Company> processResult = PayrollServices.companyManager.updateCompanyFundingModel(null, "CompanyId", null);
        PayrollServices.commitUnitOfWork();
        out.println(processResult);

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        Message message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());

        // Verify that the correct message string has returned
        String messageText = "Source System Code is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

        PayrollServices.beginUnitOfWork();        
        processResult = PayrollServices.companyManager.updateCompanyFundingModel(SourceSystemCode.QBOE, null, null);
        PayrollServices.commitUnitOfWork();

        // validate error count
        assertEquals("Number of Errors:", 1, processResult.getMessages().size());

        // validate error code
        message = processResult.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());

        // Verify that the correct message string has returned
        messageText = "Source Company ID is not specified.";
        assertEquals("Error Message", messageText, message.getMessage());

    }
}
