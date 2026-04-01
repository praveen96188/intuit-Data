package com.intuit.sbd.payroll.psp.adapters.dis.v1_7;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests.QueryCompanyEmployeesWihPaycheckCountRequestDISDTO;
import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses.QueryCompanyEmployeesWihPaycheckCountResponseDISDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/PSPQueryCompanyWagedEmployeeCountTests.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
public class PSPQueryCompanyWagedEmployeeCountTests {
    private String psid = "123456789";
    private PayrollRun payrollRun1 = null;
    private PayrollRun payrollRun2 = null;

    @Before
    public void loadDataHappyPath() {
        PayrollServicesTest.beforeEachTest();
        Application.truncateTables();
        ApplicationSecondary.truncateTables();
    }

    @Test
    public void testCompanyMultiplePayrolls() {
        loadCompanyWithPayrolls();
        EventTypeCode testEventTypeCode = EventTypeCode.TransmissionError;
        int specificTypeEventsCnt = 0;
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyEmployeesWihPaycheckCountRequestDISDTO queryCompanyWagedEmployeeCountRequestDISDTO = new QueryCompanyEmployeesWihPaycheckCountRequestDISDTO();
            queryCompanyWagedEmployeeCountRequestDISDTO.setSourceCompanyId(psid);
            queryCompanyWagedEmployeeCountRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));
            queryCompanyWagedEmployeeCountRequestDISDTO.setYear(payrollRun1.getPaycheckDate().getYear());

            QueryCompanyEmployeesWihPaycheckCountResponseDISDTO queryCompanyWagedEmployeeCountResponseDISDTO = disAdapter.Query_QueryCompanyEmployeesWihPaycheckCount(queryCompanyWagedEmployeeCountRequestDISDTO);
            TestHelper.verifySuccess(queryCompanyWagedEmployeeCountResponseDISDTO.getDisResponse());
            Assert.assertEquals(2,queryCompanyWagedEmployeeCountResponseDISDTO.getEmployeeCount());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testCompanyNoPayrollsYearSpecified() {
        loadCompanyWithPayrolls();
        EventTypeCode testEventTypeCode = EventTypeCode.TransmissionError;
        int specificTypeEventsCnt = 0;
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyEmployeesWihPaycheckCountRequestDISDTO queryCompanyWagedEmployeeCountRequestDISDTO = new QueryCompanyEmployeesWihPaycheckCountRequestDISDTO();
            queryCompanyWagedEmployeeCountRequestDISDTO.setSourceCompanyId(psid);
            queryCompanyWagedEmployeeCountRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));
            queryCompanyWagedEmployeeCountRequestDISDTO.setYear(1972);

            QueryCompanyEmployeesWihPaycheckCountResponseDISDTO queryCompanyWagedEmployeeCountResponseDISDTO = disAdapter.Query_QueryCompanyEmployeesWihPaycheckCount(queryCompanyWagedEmployeeCountRequestDISDTO);
            TestHelper.verifySuccess(queryCompanyWagedEmployeeCountResponseDISDTO.getDisResponse());
            Assert.assertEquals(0,queryCompanyWagedEmployeeCountResponseDISDTO.getEmployeeCount());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testCompanyNeverPayrolls() {
        loadCompanyWithoutPayrolls();
        EventTypeCode testEventTypeCode = EventTypeCode.TransmissionError;
        int specificTypeEventsCnt = 0;
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyEmployeesWihPaycheckCountRequestDISDTO queryCompanyWagedEmployeeCountRequestDISDTO = new QueryCompanyEmployeesWihPaycheckCountRequestDISDTO();
            queryCompanyWagedEmployeeCountRequestDISDTO.setSourceCompanyId(psid);
            queryCompanyWagedEmployeeCountRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));
            queryCompanyWagedEmployeeCountRequestDISDTO.setYear(2012);

            QueryCompanyEmployeesWihPaycheckCountResponseDISDTO queryCompanyWagedEmployeeCountResponseDISDTO = disAdapter.Query_QueryCompanyEmployeesWihPaycheckCount(queryCompanyWagedEmployeeCountRequestDISDTO);
            TestHelper.verifySuccess(queryCompanyWagedEmployeeCountResponseDISDTO.getDisResponse());
            Assert.assertEquals(0, queryCompanyWagedEmployeeCountResponseDISDTO.getEmployeeCount());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testMigratedCompanyWithDDAndAssistedPayrolls() {
        psid = DISCompanyDataloader.setupMigratedCompanyWithDDPayrollAndAssistedPayroll().getSourceCompanyId();
        try {
            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyEmployeesWihPaycheckCountRequestDISDTO queryCompanyWagedEmployeeCountRequestDISDTO = new QueryCompanyEmployeesWihPaycheckCountRequestDISDTO();
            queryCompanyWagedEmployeeCountRequestDISDTO.setSourceCompanyId(psid);

            queryCompanyWagedEmployeeCountRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));
            queryCompanyWagedEmployeeCountRequestDISDTO.setYear(2011);

            QueryCompanyEmployeesWihPaycheckCountResponseDISDTO queryCompanyWagedEmployeeCountResponseDISDTO = disAdapter.Query_QueryCompanyEmployeesWihPaycheckCount(queryCompanyWagedEmployeeCountRequestDISDTO);
            TestHelper.verifySuccess(queryCompanyWagedEmployeeCountResponseDISDTO.getDisResponse());
            Assert.assertEquals(0, queryCompanyWagedEmployeeCountResponseDISDTO.getEmployeeCount());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    @Test
    public void testNoCompanyFound() {
        try {
            String sourceCoIdDNE = "companyDNE";

            DISAdapter disAdapter = new DISAdapter();
            QueryCompanyEmployeesWihPaycheckCountRequestDISDTO queryCompanyWagedEmployeeCountRequestDISDTO = new QueryCompanyEmployeesWihPaycheckCountRequestDISDTO();
            queryCompanyWagedEmployeeCountRequestDISDTO.setSourceCompanyId(sourceCoIdDNE);

            queryCompanyWagedEmployeeCountRequestDISDTO.setSourceSystem(PSPDISTranslator.translateSourceSystemCd(SourceSystemCode.QBDT));
            queryCompanyWagedEmployeeCountRequestDISDTO.setYear(2012);

            QueryCompanyEmployeesWihPaycheckCountResponseDISDTO response = disAdapter.Query_QueryCompanyEmployeesWihPaycheckCount(queryCompanyWagedEmployeeCountRequestDISDTO);
            TestHelper.verifyDISResponse(DISMessages.companyDoesNotExist(sourceCoIdDNE), response.getDisResponse());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            TestCase.fail(e.getMessage());
        }
    }

    ///////////////////////////////////////////
    // Helper Methods
    ///////////////////////////////////////////
    private void loadCompanyWithPayrolls() {
        // Copied from testEmployerTaxNSFPayRunStatus()
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110101000000");
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};

        DataLoadServices.addFederalTaxCompanyLaws(company);
        List<Employee> employeeList = DataLoadServices.addEEs(company, 2, false, false);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        {
            PayrollServices.beginUnitOfWork();
            SpcfCalendar checkDate = PSPDate.getPSPTime();
            PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-11"), employeeList, lawIds, amounts);
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);

            PayrollServices.commitUnitOfWork();
            PSP_PRAssert.assertSuccess("submit payroll", processResult);
            payrollRun1 = (PayrollRun) processResult.getResult();
        }
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20110102000000");
        PayrollServices.commitUnitOfWork();
        {
            PayrollServices.beginUnitOfWork();
            SpcfCalendar checkDate = PSPDate.getPSPTime();
            PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);
            DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
            PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2011-01-10"), employeeList, lawIds, amounts);
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);

            PayrollServices.commitUnitOfWork();
            PSP_PRAssert.assertSuccess("submit payroll", processResult);
            payrollRun2 = (PayrollRun) processResult.getResult();
        }
    }

    ///////////////////////////////////////////
    // Helper Methods
    ///////////////////////////////////////////
    private void loadCompanyWithoutPayrolls() {
        DataLoadServices.setupCompany(psid);
    }

}
