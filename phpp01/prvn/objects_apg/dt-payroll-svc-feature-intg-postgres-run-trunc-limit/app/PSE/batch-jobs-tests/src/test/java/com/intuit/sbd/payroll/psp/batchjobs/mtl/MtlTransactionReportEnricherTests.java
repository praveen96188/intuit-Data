package com.intuit.sbd.payroll.psp.batchjobs.mtl;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.NotNull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.intuit.sbd.payroll.psp.batchjobs.mtl.MtlTransactionReportFileUtils.getAbsolutePath;
import static org.junit.Assert.assertEquals;

public class MtlTransactionReportEnricherTests {

    private static final String RAW = "raw";
    private static final String ENRICHED_EXPECTED = "enriched_expected";
    private static final String ENRICHED_ACTUAL = "enriched_actual";


    private MtlTransactionReportGenerator mtlTransactionReportGenerator;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        mtlTransactionReportGenerator = new MtlTransactionReportGenerator();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testEnrichSingleCompanyMultipleTxnsReport() throws Exception {
        // Run DD Payroll
        runDDPayrollForSingleCompany();

        // Generate the raw and excepted Mtl reports
        String mtlRawReportName = "mtl_single_company_raw.csv";

        PayrollServices.beginUnitOfWork();
        mtlTransactionReportGenerator.generateMtlTransactionReports(mtlRawReportName, getExpectedEnrichedReportName(mtlRawReportName));
        PayrollServices.rollbackUnitOfWork();

        // Enrich the raw report
        Path rawReportPath = getAbsolutePath(mtlRawReportName);
        Path actualEnrichedReportPath = getAbsolutePath(getActualEnrichedReportName(mtlRawReportName));
        enrichMtlReport(rawReportPath, actualEnrichedReportPath);

        //Validate the enriched report
        Path expectedEnrichedReportPath = getAbsolutePath(getExpectedEnrichedReportName(mtlRawReportName));
        assertEquals("Actual Mtl enriched report is  not matching with expected", Files.readAllLines(expectedEnrichedReportPath), Files.readAllLines(actualEnrichedReportPath));
    }


    private void runDDPayrollForSingleCompany() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);

        List<Employee> employeeList = DataLoadServices.addEEs(company, 1, true, false);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar checkDate = PSPDate.getPSPTime();
        checkDate.addDays(2);

        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(checkDate), employeeList);

        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                payrollRunDTO);
        PSP_PRAssert.assertSuccess("submit payroll", processResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());

        DataLoadServices.assertPayrollsEqual(payrollRunDTO, payrollRun);

        PayrollServices.rollbackUnitOfWork();
    }

    private void enrichMtlReport(Path rawReportPath, Path actualEnrichedReportPath) throws Exception {
        MtlTransactionReportEnricher mtlTransactionReportEnricher = new MtlTransactionReportEnricher(rawReportPath, actualEnrichedReportPath);
        mtlTransactionReportEnricher.enrichReport();
    }

    private String getExpectedEnrichedReportName(@NotNull String rawReportName) {
        return rawReportName.replace(RAW, ENRICHED_EXPECTED);
    }

    private String getActualEnrichedReportName(@NotNull String rawReportName) {
        return rawReportName.replace(RAW, ENRICHED_ACTUAL);
    }
}
