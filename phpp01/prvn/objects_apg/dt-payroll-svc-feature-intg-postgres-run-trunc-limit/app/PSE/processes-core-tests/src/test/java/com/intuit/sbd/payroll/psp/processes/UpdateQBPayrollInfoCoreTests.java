package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.QBDTPaycheckInfoDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.messages.Message;
import com.intuit.sbd.payroll.psp.processes.messages.MessageInfo;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.junit.PSP_PRAssert.assertSuccess;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Nov 2, 2010
 * Time: 2:19:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateQBPayrollInfoCoreTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testHappyPath() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDto = DataLoadServices.createPayrollRunWithQBInfo(company, new DateDTO("2010-11-02"), emps);
        ProcessResult procResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        assertSuccess("Initial payroll run success", procResult);
        PayrollServices.commitUnitOfWork();
        Collection<PaycheckDTO> paycheckCol = payrollRunDto.getPaychecks();
        Iterator<PaycheckDTO> iterator = paycheckCol.iterator();
        PaycheckDTO paycheckDto = iterator.next();
        QBDTPaycheckInfoDTO qbdtpaycheckInfo = paycheckDto.getQBDTPaycheckInfoDTO();
        qbdtpaycheckInfo.setAccountName("Test-1");
        qbdtpaycheckInfo.setCheckNumber("1111");
        qbdtpaycheckInfo.setCleared("T");
        qbdtpaycheckInfo.setMemo("Test-Memo");
        qbdtpaycheckInfo.setOnService(false);
        qbdtpaycheckInfo.setProrate(true);
        qbdtpaycheckInfo.setTrackingClass("Test-Tracking");
        qbdtpaycheckInfo.setSickHoursAccrued(1.1234);
        qbdtpaycheckInfo.setVacationHoursAccrued(2.1234);

        PayrollServices.beginUnitOfWork();
        procResult = PayrollServices.payrollManager.updateQBPayrollInfo(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        PayrollServices.commitUnitOfWork();

        assertSuccess("After updating qbdt paycheck Info", procResult);
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDto.getPayrollTXBatchId());
        Paycheck paycheck = assertOne(payrollRun.getPaycheckCollection().find(Paycheck.SourcePaycheckId().equalTo(paycheckDto.getPaycheckId())));
        assertEquals("Test-1", paycheck.getQbdtPaycheckInfo().getAccountName());
        assertEquals("1111", paycheck.getQbdtPaycheckInfo().getCheckNumber());
        assertEquals("T", paycheck.getQbdtPaycheckInfo().getCleared());
        assertEquals("Test-Memo",paycheck.getQbdtPaycheckInfo().getMemo());
        assertFalse("Service flag set is failed.",paycheck.getQbdtPaycheckInfo().getOnService());
        assertTrue(paycheck.getQbdtPaycheckInfo().getProrate());
        assertEquals("Test-Tracking",paycheck.getQbdtPaycheckInfo().getTrackingClass());
        assertEquals(1.1234, paycheck.getQbdtPaycheckInfo().getSickHoursAccrued());
        assertEquals(2.1234, paycheck.getQbdtPaycheckInfo().getVacationHoursAccrued());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testPayRunDtoNotSpecified() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        PayrollServices.beginUnitOfWork();
        ProcessResult pr = PayrollServices.payrollManager.updateQBPayrollInfo(company.getSourceSystemCd(), company.getSourceCompanyId(), null);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "5001", message.getMessageCode());
        assertEquals("Error Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "PayrollRunDTO has invalid value", message.getMessage());

    }

    @Test
    public void testSourceSysCdNotSpecified() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDto = DataLoadServices.createPayrollRunWithQBInfo(company, new DateDTO("2010-11-02"), emps);
        PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        ProcessResult pr = PayrollServices.payrollManager.updateQBPayrollInfo(null, company.getSourceCompanyId(), payrollRunDto);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "137", message.getMessageCode());
        assertEquals("Error Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Source System Code is not specified.", message.getMessage());

    }

    @Test
    public void testSourceComIdNotSpecified() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDto = DataLoadServices.createPayrollRunWithQBInfo(company, new DateDTO("2010-11-02"), emps);
        PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        ProcessResult pr = PayrollServices.payrollManager.updateQBPayrollInfo(company.getSourceSystemCd(), null, payrollRunDto);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "138", message.getMessageCode());
        assertEquals("Error Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Source Company ID is not specified.", message.getMessage());
    }

    @Test
    public void testCompanyDoesNotExists() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDto = DataLoadServices.createPayrollRunWithQBInfo(company, new DateDTO("2010-11-02"), emps);
        PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        ProcessResult pr = PayrollServices.payrollManager.updateQBPayrollInfo(SourceSystemCode.CRIS, "COMP_COM_ID", payrollRunDto);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "169", message.getMessageCode());
        assertEquals("Error Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Company CRIS:COMP_COM_ID does not exist.", message.getMessage());
    }

    @Test
    public void testPayrollRunDoesNotExists() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDto = DataLoadServices.createPayrollRunWithQBInfo(company, new DateDTO("2010-11-02"), emps);
        PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        //updating payroll Id
        payrollRunDto.setPayrollTXBatchId("TEST12333");
        ProcessResult pr = PayrollServices.payrollManager.updateQBPayrollInfo(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "194", message.getMessageCode());
        assertEquals("Error Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Payroll Run with DDTxBatchID TEST12333 does not exist for company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId() + ".", message.getMessage());
    }

    @Test
    public void testPaychecksDoesNotExists() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.Tax);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRunDto = DataLoadServices.createPayrollRunWithQBInfo(company, new DateDTO("2010-11-02"), emps);
        ProcessResult procResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        assertSuccess("Submit payroll", procResult);
        //updating paychecks
        Collection<PaycheckDTO> paycheckDTOs = payrollRunDto.getPaychecks();
        Iterator<PaycheckDTO> iterator = paycheckDTOs.iterator();
        PaycheckDTO paycheckDto = iterator.next();
        paycheckDto.setPaycheckId("TEST_PAYCHECK123");

        ProcessResult pr = PayrollServices.payrollManager.updateQBPayrollInfo(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRunDto);
        PayrollServices.commitUnitOfWork();
        // validate error count
        assertEquals("Number of Errors:", 1, pr.getMessages().size());

        // vaildate error code
        Message message = pr.getMessages().get(0);
        assertEquals("Error Code:", "299", message.getMessageCode());
        assertEquals("Error Level:", MessageInfo.MessageLevel.ERROR, message.getLevel());
        assertEquals("Error Message:", "Paycheck TEST_PAYCHECK123 for company " + company.getSourceSystemCd() + ":" + company.getSourceCompanyId() + " does not exist.", message.getMessage());
    }


}
