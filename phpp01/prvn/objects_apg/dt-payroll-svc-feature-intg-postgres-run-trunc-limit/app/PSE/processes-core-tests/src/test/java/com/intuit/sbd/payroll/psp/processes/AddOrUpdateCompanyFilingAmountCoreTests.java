package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyFilingAmountDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.domain.AdditionalFilingAmount;
import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.CompanyFilingAmount;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * User: TimothyD698
 * Date: Mar 12, 2013
 */
public class AddOrUpdateCompanyFilingAmountCoreTests {

    private final static SpcfLogger logger = Application.getLogger(AddOrUpdateCompanyFilingAmountCoreTests.class);

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.setPSPDate(2013, 4, 4);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testInvalidParameters() {

        ProcessResult processResult;

        DataLoadServices.setupAssistedCompanyForCA("606001967", 1, true);

        // Company not specified.
        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT, null, null);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "138", "Source Company ID is not specified.");

        // DTO not specified.
        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT, "606001967", null);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "11", "Invalid argument: DTO");

        CompanyFilingAmountDTO dto =  new CompanyFilingAmountDTO();

        // DTO without a filing amount name.
        Application.beginUnitOfWork();
        processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT, "606001967", dto);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "11", "Invalid argument: Name");

        // DTO without an effective date.
        Application.beginUnitOfWork();
        dto.setName("MA SUI Credit");
        processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT, "606001967", dto);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "11", "Invalid argument: EffectiveDate");

        // DTO with an invalid effective date.
        Application.beginUnitOfWork();
        dto.setEffectiveDate(new DateDTO(2013,4,4));
        processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT, "606001967", dto);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "11", "Invalid argument: EffectiveDate");

        // Unable to find a CAPT.
        Application.beginUnitOfWork();
        dto.setEffectiveDate(new DateDTO(2013,4,1));
        processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT, "606001967", dto);
        Application.rollbackUnitOfWork();
        verifyInvalidParameterResult(processResult, "11", "Invalid argument: Payment Template");

    }

    private static void verifyInvalidParameterResult(ProcessResult processResult, String messageCode, String message) {
        Message errorMessage;

        assertFalse(processResult.isSuccess());
        assertEquals("Messages size", 1, processResult.getMessages().size());
        errorMessage = processResult.getMessages().get(0);
        assertEquals("Error message code", messageCode, errorMessage.getMessageCode());
        assertEquals("Error message", message, errorMessage.getMessage());
    }

    public void testFilingAmountAddAndUpdate(String additionalFilingAmountName) {

        final double DEFAULT_AMOUNT_VALUE_Q1=120.0;
        final double DEFAULT_AMOUNT_VALUE_Q2=125.0;
        final double UPDATED_AMOUNT_VALUE_Q1=122.5;
        final double UPDATED_AMOUNT_VALUE_Q2=130.0;
        final double DEFAULT_RATE_VALUE_Q1=0.020;
        final double DEFAULT_RATE_VALUE_Q2=0.036;
        final double UPDATED_RATE_VALUE_Q1=0.020;
        final double UPDATED_RATE_VALUE_Q2=0.036;

        //Clear data from tables
        PayrollServicesTest.truncateTables();
        logger.info("Starting add & update filing amount test for "+additionalFilingAmountName);

        //Getting details for the given additional filing amount
        Application.beginUnitOfWork();
        AdditionalFilingAmount additionalFilingAmount = AdditionalFilingAmount.findByName(additionalFilingAmountName);
        assertNotNull("Filing amount "+additionalFilingAmountName+" not found", additionalFilingAmount);
        String paymentTemplateName = additionalFilingAmount.getPaymentTemplate().getPaymentTemplateCd();
        boolean isRateValue = additionalFilingAmount.getRate();
        String stateCode = additionalFilingAmount.getPaymentTemplate().getAgency().getJurisdiction().getStateID();
        Application.rollbackUnitOfWork();

        double defaultValueQ1=DEFAULT_AMOUNT_VALUE_Q1;
        double defaultValueQ2=DEFAULT_AMOUNT_VALUE_Q2;
        double updatedValueQ1=UPDATED_AMOUNT_VALUE_Q1;
        double updatedValueQ2=UPDATED_AMOUNT_VALUE_Q2;

        if(isRateValue){
            defaultValueQ1=DEFAULT_RATE_VALUE_Q1;
            defaultValueQ2=DEFAULT_RATE_VALUE_Q2;
            updatedValueQ1=UPDATED_RATE_VALUE_Q1;
            updatedValueQ2=UPDATED_RATE_VALUE_Q2;
        }

        ProcessResult processResult;
        CompanyFilingAmountDTO dto;

        Company company = DataLoadServices.setupAssistedCompanyForCA("606001967", 1, true);

        DataLoadServices.addCompanyLawsWithAgencyId("123456", company, stateCode);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly("606001967", paymentTemplateName);
        DataLoadServices.updatePaymentTemplateSupportedDate(paymentTemplateName, PSPDate.getPSPTime());

        SpcfCalendar supportStartDate = SpcfCalendar.createInstance(2013, 4, 1, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(supportStartDate);
        PayrollServices.commitUnitOfWork();

        // Create Q1 amount
        Application.beginUnitOfWork();
        dto = createCompanyFilingAmountDTO(additionalFilingAmountName, defaultValueQ1, 2013, 1);
        processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT, "606001967", dto);
        assertTrue(processResult.isSuccess());
        Application.commitUnitOfWork();

        // Create Q2 amount
        Application.beginUnitOfWork();
        dto = createCompanyFilingAmountDTO(additionalFilingAmountName, defaultValueQ2, 2013, 4);
        processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT, "606001967", dto);
        assertTrue(processResult.isSuccess());
        Application.commitUnitOfWork();

        // Update Q1 amount to make sure Q2 amount is unaffected.
        Application.beginUnitOfWork();
        dto = createCompanyFilingAmountDTO(additionalFilingAmountName, updatedValueQ1, 2013, 1);
        processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT, "606001967", dto);
        assertTrue(processResult.isSuccess());
        Application.commitUnitOfWork();

        // Update Q2 amount
        Application.beginUnitOfWork();
        dto = createCompanyFilingAmountDTO(additionalFilingAmountName, updatedValueQ2, 2013, 4);
        processResult = PayrollServices.companyManager.addOrUpdateCompanyFilingAmount(SourceSystemCode.QBDT, "606001967", dto);
        assertTrue(processResult.isSuccess());
        Application.commitUnitOfWork();

        // Verify the contents
        Application.beginUnitOfWork();
        DomainEntitySet<CompanyFilingAmount> cfas = Application.find(CompanyFilingAmount.class, new Query<CompanyFilingAmount>()
                .Where(CompanyFilingAmount.Name().like(stateCode+" %")
                                          .And(CompanyFilingAmount.InvalidDate().isNull()))
                .OrderBy(CompanyFilingAmount.EffectiveDate()));
        Assert.assertEquals("Number of Records", isRateValue?2:3, cfas.size());

        Assert.assertEquals("Name", additionalFilingAmountName, cfas.get(0).getName());
        Assert.assertEquals("Effective Date", SpcfCalendar.createInstance(2013, 1, 1, 8, 0, 0, 0), cfas.get(0).getEffectiveDate());
        Assert.assertEquals("Amount", updatedValueQ1, cfas.get(0).getAmount());

        Assert.assertEquals("Name", additionalFilingAmountName, cfas.get(1).getName());
        Assert.assertEquals("Effective Date", SpcfCalendar.createInstance(2013, 4, 1, 7, 0, 0, 0), cfas.get(1).getEffectiveDate());
        Assert.assertEquals("Amount", updatedValueQ2, cfas.get(1).getAmount());
        if(!isRateValue) {
            Assert.assertEquals("Name", additionalFilingAmountName, cfas.get(2).getName());
            Assert.assertEquals("Effective Date", SpcfCalendar.createInstance(2013, 7, 1, 7, 0, 0, 0), cfas.get(2).getEffectiveDate());
            Assert.assertEquals("Amount", 0.0, cfas.get(2).getAmount());
        }
        Application.rollbackUnitOfWork();
        logger.info("Successfully tested add & update filing amount for "+additionalFilingAmountName);

    }

    @Test
    public void testMultipleFilingAmountsForAddAndUpdate(){
        final List<String> filingAmountToTestNameList=new ArrayList<String>();
        //NV
        filingAmountToTestNameList.add("NV SUI Credit");
        filingAmountToTestNameList.add("NV Bond Contribution Rate");
        //MA
        filingAmountToTestNameList.add("MA SUI Credit");
        filingAmountToTestNameList.add("MA Er Medical Assistance Contribution");
        filingAmountToTestNameList.add("MA Unemployment Health Insurance Rate");
        filingAmountToTestNameList.add("WY SUI Credit");
        //Running Tests
        for (String filingAmountName: filingAmountToTestNameList) {
            testFilingAmountAddAndUpdate(filingAmountName);
        }
    }

    private CompanyFilingAmountDTO createCompanyFilingAmountDTO(String name, double amount, int year, int month) {
        CompanyFilingAmountDTO dto =  new CompanyFilingAmountDTO();
        dto.setName(name);
        dto.setAmount(amount);
        dto.setEffectiveDate(new DateDTO(year,month,1));
        return dto;
    }
}
