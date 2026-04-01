package com.intuit.sbd.payroll.psp.batchjobs.offload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.OFXRequestGenerator;
import com.intuit.sbd.payroll.psp.adapters.qbdt.QBDTTestHelper;
import com.intuit.sbd.payroll.psp.adapters.qbdt.billing.UsageBillingTestsBase;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.MockSocketManager;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.PrimaryValidations.TestPrimaryValidations;
import com.intuit.sbd.payroll.psp.batchjobs.ACHReturns.TransactionReturnHandlers.ACHReturnsDataLoader;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.NACHAFileStatus;
import com.intuit.sbd.payroll.psp.domain.NACHAFileType;
import com.intuit.sbd.payroll.psp.domain.OffloadBatchStatus;
import com.intuit.sbd.payroll.psp.domain.PayrollStatus;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransactionStateCode;
import com.intuit.sbd.payroll.psp.domain.TransactionTypeCode;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

/**
 * Created by cmehta1 on 8/8/18.
 */
public class TestPrimarySymphony extends UsageBillingTestsBase {

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.truncateTables();
        PayrollServicesTest.beforeEachTest();
    }

    @Test
    public void verifyPrimaryJobForMigratedSymphonyCompany() throws Throwable{
        String sourceCompanyId = "8574536";
        String sourcePayrollRunId = "BatchTest09";
        Company company;
        PayrollRun payrollRun;
        SpcfUniqueId spcfUniqueId = ACHReturnsDataLoader.loadQBDTPayrollOffload(DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(OffloadGroup.Codes.PSPOFFLOADS));
        Application.beginUnitOfWork();
        OffloadBatch createdBatch = Application.findById(OffloadBatch.class, spcfUniqueId);
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.PSPOFFLOADS, createdBatch.getOffloadGroup().getOffloadGroupCd());
        SpcfUniqueId batchId = createdBatch.getId();
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        NACHAFile ccdFile = nachaFiles.get(0);
        NACHAFile ppdFile = nachaFiles.get(1);
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payrollRun.getPayrollRunStatus());
        Assert.assertEquals("Number of files:", 2, nachaFiles.size());
        SpcfMoney expectedTotalCredits = new SpcfMoney("777.77");
        SpcfMoney expectedTotalDebits = new SpcfMoney("777.77");
        NACHAFile createdFilePPD = nachaFiles.get(1);
        TestPrimaryValidations.validateNACHAFileFinalized(createdFilePPD, NACHAFileType.PPD, expectedTotalCredits, expectedTotalDebits);
        NACHAFile createdFileCCD = nachaFiles.get(0);
        TestPrimaryValidations.validateNACHAFileFinalized(createdFileCCD, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);

        DomainEntitySet<FinancialTransaction> erDdDebits =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> eeDdCredits =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        junit.framework.Assert.assertEquals("Number of C2 EmployerDdDebit EX txns", 1, erDdDebits.size());
        junit.framework.Assert.assertEquals("Number of C2 EmployeeDdCredit EX txns", 2, eeDdCredits.size());
        TestPrimaryValidations.verifyMMTxnAndTraceNums(erDdDebits, createdBatch);
        TestPrimaryValidations.verifyMMTxnAndTraceNums(eeDdCredits, createdBatch);
        TestPrimaryValidations.validateFileAndTraceNumbers(
                ccdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testCCD_Symphony.ach"), NACHAFileType.CCD);
        TestPrimaryValidations.validateFileAndTraceNumbers(
                ppdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testPPD_Symphony.ach"), NACHAFileType.PPD);

        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void verifyPrimaryJobForNonMigratedSymphonyCompany() throws Throwable{
        String sourceCompanyId = "8574536";
        String sourcePayrollRunId = "BatchTest09";
        Company company;
        PayrollRun payrollRun;
        SpcfUniqueId spcfUniqueId = ACHReturnsDataLoader.loadQBDTPayrollOffload(DataLoadServices.AssetItemNumber.DIY_USAGE_BILLING_MONTHLY, OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD));
        Application.beginUnitOfWork();
        OffloadBatch createdBatch = Application.findById(OffloadBatch.class, spcfUniqueId);
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        SpcfUniqueId batchId = createdBatch.getId();
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        NACHAFile ccdFile = nachaFiles.get(0);
        NACHAFile ppdFile = nachaFiles.get(1);
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        payrollRun = PayrollRun.findPayrollRun(company, sourcePayrollRunId);
        assertEquals("PayrollRunStatus", PayrollStatus.OffloadedAll, payrollRun.getPayrollRunStatus());
        Assert.assertEquals("Number of files:", 2, nachaFiles.size());
        SpcfMoney expectedTotalCredits = new SpcfMoney("777.77");
        SpcfMoney expectedTotalDebits = new SpcfMoney("777.77");
        NACHAFile createdFilePPD = nachaFiles.get(1);
        TestPrimaryValidations.validateNACHAFileFinalized(createdFilePPD, NACHAFileType.PPD, expectedTotalCredits, expectedTotalDebits);
        NACHAFile createdFileCCD = nachaFiles.get(0);
        TestPrimaryValidations.validateNACHAFileFinalized(createdFileCCD, NACHAFileType.CCD, expectedTotalCredits, expectedTotalDebits);
        DomainEntitySet<FinancialTransaction> erDdDebits =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> eeDdCredits =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "8574536",
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        junit.framework.Assert.assertEquals("Number of C2 EmployerDdDebit EX txns", 1, erDdDebits.size());
        junit.framework.Assert.assertEquals("Number of C2 EmployeeDdCredit EX txns", 2, eeDdCredits.size());
        TestPrimaryValidations.verifyMMTxnAndTraceNums(erDdDebits, createdBatch);
        TestPrimaryValidations.verifyMMTxnAndTraceNums(eeDdCredits, createdBatch);
        TestPrimaryValidations.validateFileAndTraceNumbers(
                ccdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testCCD_NonMigrated.ach"), NACHAFileType.CCD);
        TestPrimaryValidations.validateFileAndTraceNumbers(
                ppdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/testPPD_Symphony_NonMigrated.ach"), NACHAFileType.PPD);

        PayrollServices.commitUnitOfWork();
    }
    @Test
    public void test_DD_Assisted_payroll() {

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        OFXRequestGenerator.reset();
        MockSocketManager.reset();

        DataLoadServices.resetAllPaymentTemplateSupportDates();
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.DirectDeposit,ServiceCode.Tax);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1");

        List<Employee> emps = DataLoadServices.addEEs(company, 2, true, true);

        HashMap<String, String> lawAmounts = new HashMap<String, String>();
        lawAmounts.put("61", "6.1");
        lawAmounts.put("62", "6.2");
        lawAmounts.put("63", "6.3");
        lawAmounts.put("64", "6.4");
        lawAmounts.put("66", "6.6");
        lawAmounts.put("1", "10");
        DataLoadServices.setPSPDate(2012, 1, 1);
        PayrollServices.beginUnitOfWork();
        TestOffloadACHTransactions testOffloadACHTransactions = new TestOffloadACHTransactions();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        //DataLoadServices.changeOffloadGroup(company,OffloadGroup.Codes.STANDARD);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);

        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(2012, 1, 15), emps, lawAmounts);
        String paycheckId = null;
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
            if(paycheckId == null) {
                paycheckId = paycheckDTO.getPaycheckId();
            } else {
                paycheckDTO.getDdTransactions().clear();
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitPayroll(company, payrollRunDTO, false);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 15, SpcfTimeZone.getLocalTimeZone()))));
        assertEquals(payrollRun.getCompany().getOffloadGroup().getName(), OffloadGroup.findOffloadGroup(OffloadGroup.Codes.STANDARD).getName());
        PayrollServices.rollbackUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, SpcfCalendar.createInstance(2012, 1, 12, SpcfTimeZone.getLocalTimeZone()));
        /***********************Persistence testing*********************/
        PayrollServices.beginUnitOfWork();
        OffloadBatch createdBatch = offloader.getOffloadBatch();
        assertNotNull(createdBatch);
        assertEquals("Offload group code", OffloadGroup.Codes.STANDARD,
                createdBatch.getOffloadGroup().getOffloadGroupCd());
        assertEquals("Status code", OffloadBatchStatus.Completed, createdBatch.getStatusCd());
        TestPrimaryValidations.assertYearMonthDayEquals(createdBatch.getStatusEffeciveDate().toLocal(), PSPDate.getPSPTime());
        SpcfCalendar spcfCalendar = PSPDate.getPSPTime();
        CalendarUtils.clearTime(spcfCalendar);
        DomainEntitySet<NACHAFile> nachaFiles = createdBatch.getNACHAFilesForOffloadBatch(NACHAFileStatus.Finalized);
        NACHAFile ccdFile = nachaFiles.get(0);
        NACHAFile ppdFile = nachaFiles.get(1);
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of files:", 2, nachaFiles.size());
        SpcfMoney expectedTotalCredits = new SpcfMoney("1.00");
        SpcfMoney expectedTotalDebits = new SpcfMoney("167.89");
        NACHAFile createdFilePPD = nachaFiles.get(1);
        Application.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> erDdDebits =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "123456789",
                        TransactionTypeCode.EmployerDdDebit, TransactionStateCode.Executed);
        DomainEntitySet<FinancialTransaction> eeDdCredits =
                FinancialTransaction.findFinancialTransactions(SourceSystemCode.QBDT, "123456789",
                        TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Executed);

        junit.framework.Assert.assertEquals("Number of C2 EmployerDdDebit EX txns", 1, erDdDebits.size());
        junit.framework.Assert.assertEquals("Number of C2 EmployeeDdCredit EX txns", 1, eeDdCredits.size());
        TestPrimaryValidations.verifyMMTxnAndTraceNums(erDdDebits, createdBatch);
        TestPrimaryValidations.verifyMMTxnAndTraceNums(eeDdCredits, createdBatch);
        TestPrimaryValidations.validateFileAndTraceNumbers(
                ccdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/test_CCD_Assisted.ach"), NACHAFileType.CCD);
        TestPrimaryValidations.validateFileAndTraceNumbers(
                ppdFile.getFileName(),
                Application.findFileOnClassPath("offload/expected/test_PPD_Assisted.ach"), NACHAFileType.PPD);


    }



}
