package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTPaycheckInfoDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.IEMP;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 1/21/13
 * Time: 9:05 AM
 */
public class BalanceFileProcessingTests {
    @BeforeClass
    public static void beforeClass() {
    }

    @AfterClass
    public static void afterClass() {
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2012, 11, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testPaycheckModsIncludedInBALF() {
        DataLoadServices.setPSPDate(2012, 11, 1);
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.DirectDeposit);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addCompanyPIN(company, null);
        List<Employee> employees = DataLoadServices.addEEs(company, 2, true, false);

        OFX balf = OFXRequestGenerator.generateBalanceFile(psid, true);
        List<String> paycheckIds = new ArrayList<String>();
        for (IPAYROLLRUN ipayrollrun : balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                paycheckIds.add(ipaychk.getIPAYCHKID());
            }
        }

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 11, 10), employees);
        PayrollServices.rollbackUnitOfWork();

        int count = 0;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(paycheckIds.get(count++));
        }
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addTaxService(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        for (IPAYROLLRUN ipayrollrun : balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (Iterator<IPAYCHK> paycheckIterator = ipayrollrun.getIPAYCHK().iterator(); paycheckIterator.hasNext(); ) {
                IPAYCHK next = paycheckIterator.next();
                paycheckIterator.remove();
                ipayrollrun.getIPAYCHKMOD().add(next);
            }
        }
        company = DataLoadServices.refreshCompany(company);
        balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequestStringResponse(balf);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        assertEquals(6, payrollRuns.size());
        boolean foundMigratedPayroll = false;
        for (PayrollRun payrollRun : payrollRuns) {
            if(payrollRun.getPaycheckCollection().size() == 2) {
                foundMigratedPayroll = true;
                for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                    assertTrue("Expected migrated paycheck", paycheck.getSourcePaycheckId().contains("-"));
                }
            }
        }
        assertTrue("Migrated payroll not found", foundMigratedPayroll);
        OFXAssert.assertPayrolls(balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNewPaycheckIdSameListIdIncludedInBALF() {
        DataLoadServices.setPSPDate(2012, 11, 1);
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.DirectDeposit);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addCompanyPIN(company, null);
        List<Employee> employees = DataLoadServices.addEEs(company, 2, true, false, true);

        OFX balf = OFXRequestGenerator.generateBalanceFile(psid, true);
        int count = 0;
        Map<String, String> changedEmployeeIds = new HashMap<String, String>();
        for (Employee employee : employees) {
            IEMP iemp = balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(count++);
            changedEmployeeIds.put(iemp.getIEMPID(), employee.getSourceEmployeeId());
            iemp.setIEMPID(employee.getSourceEmployeeId());
        }

        List<String> paycheckIds = new ArrayList<String>();
        for (IPAYROLLRUN ipayrollrun : balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                paycheckIds.add(ipaychk.getIPAYCHKID());
                ipaychk.setIQBUNIQUEID(ipaychk.getIPAYCHKID());
                if(changedEmployeeIds.keySet().contains(ipaychk.getIEMPID())) {
                    ipaychk.setIEMPID(changedEmployeeIds.get(ipaychk.getIEMPID()));
                }
            }
        }

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 11, 10), employees);
        PayrollServices.rollbackUnitOfWork();

        count = 0;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(paycheckIds.get(count++));
            paycheckDTO.setQBDTPaycheckInfoDTO(new QBDTPaycheckInfoDTO());
            paycheckDTO.getQBDTPaycheckInfoDTO().setListId(paycheckDTO.getPaycheckId());
        }
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addTaxService(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        for (IPAYROLLRUN ipayrollrun : balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                ipaychk.setIPAYCHKID(OFXRequestGenerator.getNextPaycheckId() + "");
            }
        }
        company = DataLoadServices.refreshCompany(company);
        balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequestStringResponse(balf);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        assertEquals(6, payrollRuns.size());
        boolean foundMigratedPayroll = false;
        for (PayrollRun payrollRun : payrollRuns) {
            if(payrollRun.getPaycheckCollection().size() == 2) {
                foundMigratedPayroll = true;
                for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                    assertTrue("Expected migrated paycheck", paycheck.getSourcePaycheckId().contains("-"));
                }
            }
        }
        assertTrue("Migrated payroll not found", foundMigratedPayroll);
        OFXAssert.assertPayrolls(balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testNewPaycheckIdAndEmployeeIdsSameListIdIncludedInBALF() {
        DataLoadServices.setPSPDate(2012, 11, 1);
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.DirectDeposit);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.addCompanyPIN(company, null);
        List<Employee> employees = DataLoadServices.addEEs(company, 2, true, false, true);

        OFX balf = OFXRequestGenerator.generateBalanceFile(psid, true);
        int count = 0;
        Map<String, String> changedEmployeeIds = new HashMap<String, String>();
        for (Employee employee : employees) {
            IEMP iemp = balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(count++);
            String newEmpId = iemp.getIEMPID() + "9999";
            changedEmployeeIds.put(iemp.getIEMPID(), newEmpId);
            iemp.setIEMPID(newEmpId);
            iemp.setIQBUNIQUEID(employee.getSourceEmployeeId());
        }

        List<String> paycheckIds = new ArrayList<String>();
        for (IPAYROLLRUN ipayrollrun : balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                paycheckIds.add(ipaychk.getIPAYCHKID());
                ipaychk.setIQBUNIQUEID(ipaychk.getIPAYCHKID());
                if(changedEmployeeIds.keySet().contains(ipaychk.getIEMPID())) {
                    ipaychk.setIEMPID(changedEmployeeIds.get(ipaychk.getIEMPID()));
                }
            }
        }

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 11, 10), employees);
        PayrollServices.rollbackUnitOfWork();

        count = 0;
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(paycheckIds.get(count++));
            paycheckDTO.setQBDTPaycheckInfoDTO(new QBDTPaycheckInfoDTO());
            paycheckDTO.getQBDTPaycheckInfoDTO().setListId(paycheckDTO.getPaycheckId());
        }
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDTO));
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addTaxService(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        for (IPAYROLLRUN ipayrollrun : balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN()) {
            for (IPAYCHK ipaychk : ipayrollrun.getIPAYCHK()) {
                ipaychk.setIPAYCHKID(OFXRequestGenerator.getNextPaycheckId() + "");
            }
        }
        company = DataLoadServices.refreshCompany(company);
        balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().setTOKEN(company.getCurrentToken() + "");
        QBDTTestHelper.submitQBDTRequestStringResponse(balf);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        assertEquals(6, payrollRuns.size());
        boolean foundMigratedPayroll = false;
        for (PayrollRun payrollRun : payrollRuns) {
            if(payrollRun.getPaycheckCollection().size() == 2) {
                foundMigratedPayroll = true;
                for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
                    assertTrue("Expected migrated paycheck", paycheck.getSourcePaycheckId().contains("-"));
                }
            }
        }
        assertTrue("Migrated payroll not found", foundMigratedPayroll);
        OFXAssert.assertPayrolls(balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        PayrollServices.rollbackUnitOfWork();
    }
}
