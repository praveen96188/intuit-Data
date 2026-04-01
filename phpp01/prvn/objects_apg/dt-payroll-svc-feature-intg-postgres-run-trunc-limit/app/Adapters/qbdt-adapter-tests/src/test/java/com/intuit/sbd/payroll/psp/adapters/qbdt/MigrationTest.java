package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE;
import com.intuit.sbd.payroll.psp.common.ofx.response.OFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MigrationTest {

    @BeforeClass
    public static void beforeClass() {
    }

    @AfterClass
    public static void afterClass() {
    }

    @Before
    public void runBeforeEachTest() {
        QBDTTestHelper.typicalRunBeforeEachTest();
        DataLoadServices.reinitialize();
    }

    @After
    public void runAfterEachTest() {
        QBDTTestHelper.typicalRunAfterEachTest();
    }

    @Test
    public void testCustomerMigratedToTax() {
        DataLoadServices.setPSPDate(2012, 10, 1);
        String psid = "122345";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        long beforeToken = DataLoadServices.refreshCompany(company).getCurrentToken();
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 10, 15))));
        PayrollServices.commitUnitOfWork();

        OFX response = QBDTTestHelper.submitSyncRequest(company, beforeToken, true);
        IPAYROLLTX ipayrolltx = assertOne(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD());

        // add tax service to migrate
        DataLoadServices.addTaxService(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        com.intuit.sbd.payroll.psp.common.ofx.request.OFX balf = OFXRequestGenerator.generateBalanceFile(company.getSourceCompanyId(), false);
        com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTX newIpayrolltx = new com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTX();
        newIpayrolltx.setIVOID(ipayrolltx.getIVOID());
        newIpayrolltx.setIACCTNAME(ipayrolltx.getIACCTNAME());
        newIpayrolltx.setIAMT(ipayrolltx.getIAMT());
        newIpayrolltx.setICLEARED(ipayrolltx.getICLEARED());
        newIpayrolltx.setIDTPAYPDEND(ipayrolltx.getIDTPAYPDEND());
        newIpayrolltx.setIDTTX(ipayrolltx.getIDTTX());
        newIpayrolltx.setIEMPID(ipayrolltx.getIEMPID());
        newIpayrolltx.setIEMPNAME(ipayrolltx.getIEMPNAME());
        newIpayrolltx.setIMEMO(ipayrolltx.getIMEMO());
        newIpayrolltx.setINAME(ipayrolltx.getINAME());
        newIpayrolltx.setIONSERVICE(ipayrolltx.getIONSERVICE());
        newIpayrolltx.setIPAYROLLTXID(ipayrolltx.getIPAYROLLTXID());
        newIpayrolltx.setIPAYROLLTXTYPE(ipayrolltx.getIPAYROLLTXTYPE());
        newIpayrolltx.setIREFNUM(ipayrolltx.getIREFNUM());
        for (ITXLINE itxline : ipayrolltx.getITXLINE()) {
            com.intuit.sbd.payroll.psp.common.ofx.request.ITXLINE newItxline = new com.intuit.sbd.payroll.psp.common.ofx.request.ITXLINE();
            newItxline.setIACCTNAME(itxline.getIACCTNAME());
            newItxline.setIAMT(itxline.getIAMT());
            newItxline.setICLASS(itxline.getICLASS());
            newItxline.setIISDD(itxline.getIISDD());
            newItxline.setIMEMO(itxline.getIMEMO());
            newItxline.setIPITEMID(itxline.getIPITEMID());
            newItxline.setITAXABLEWAGE(itxline.getITAXABLEWAGE());
            newItxline.setIWB(itxline.getIWB());
            newIpayrolltx.getITXLINE().add(newItxline);
        }
        balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().add(newIpayrolltx);

        QBDTTestHelper.submitQBDTRequest(balf);

        // make sure the liability check from DIY was negated and hidden (-2 token)
        PayrollServices.beginUnitOfWork();
        LiabilityCheck liabilityCheck = LiabilityCheck.findLiabilityCheckBySourceId(company, "-" + ipayrolltx.getIPAYROLLTXID());
        assertNotNull("negated liability check not found", liabilityCheck);
        assertEquals("token not updated", Company.EXCLUDE_TOKEN, liabilityCheck.getQbdtTransactionInfo().getToken());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCustomerMigratedToTax_PSRV004099() {
        DataLoadServices.setPSPDate(2012, 10, 1);
        String psid = "122345";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);

        Employee employee = new Employee();
        for (Employee emp : company.getEmployees()) {
            if (emp.getSourceEmployeeId().contains("First_")) {
                employee = emp;
                break;
            }
        }
        employee.setSourceEmployeeId("50");
        employee.setMiddleName(null);
        employee.setLastName(null);
        Application.save(employee);
        PayrollServices.commitUnitOfWork();

        long beforeToken = DataLoadServices.refreshCompany(company).getCurrentToken();
        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 10, 15))));
        PayrollServices.commitUnitOfWork();

        OFX response = QBDTTestHelper.submitSyncRequest(company, beforeToken, true);
        IPAYROLLTX ipayrolltx = assertOne(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD());

        // add tax service to migrate
        DataLoadServices.addTaxService(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        com.intuit.sbd.payroll.psp.common.ofx.request.OFX balf = OFXRequestGenerator.generateBalanceFile(company.getSourceCompanyId(), false);
        com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTX newIpayrolltx = new com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTX();
        newIpayrolltx.setIVOID(ipayrolltx.getIVOID());
        newIpayrolltx.setIACCTNAME(ipayrolltx.getIACCTNAME());
        newIpayrolltx.setIAMT(ipayrolltx.getIAMT());
        newIpayrolltx.setICLEARED(ipayrolltx.getICLEARED());
        newIpayrolltx.setIDTPAYPDEND(ipayrolltx.getIDTPAYPDEND());
        newIpayrolltx.setIDTTX(ipayrolltx.getIDTTX());
        newIpayrolltx.setIEMPID(ipayrolltx.getIEMPID());
        newIpayrolltx.setIEMPNAME(ipayrolltx.getIEMPNAME());
        newIpayrolltx.setIMEMO(ipayrolltx.getIMEMO());
        newIpayrolltx.setINAME(ipayrolltx.getINAME());
        newIpayrolltx.setIONSERVICE(ipayrolltx.getIONSERVICE());
        newIpayrolltx.setIPAYROLLTXID(ipayrolltx.getIPAYROLLTXID());
        newIpayrolltx.setIPAYROLLTXTYPE(ipayrolltx.getIPAYROLLTXTYPE());
        newIpayrolltx.setIREFNUM(ipayrolltx.getIREFNUM());
        for (ITXLINE itxline : ipayrolltx.getITXLINE()) {
            com.intuit.sbd.payroll.psp.common.ofx.request.ITXLINE newItxline = new com.intuit.sbd.payroll.psp.common.ofx.request.ITXLINE();
            newItxline.setIACCTNAME(itxline.getIACCTNAME());
            newItxline.setIAMT(itxline.getIAMT());
            newItxline.setICLASS(itxline.getICLASS());
            newItxline.setIISDD(itxline.getIISDD());
            newItxline.setIMEMO(itxline.getIMEMO());
            newItxline.setIPITEMID(itxline.getIPITEMID());
            newItxline.setITAXABLEWAGE(itxline.getITAXABLEWAGE());
            newItxline.setIWB(itxline.getIWB());
            newIpayrolltx.getITXLINE().add(newItxline);
        }
        balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLTX().add(newIpayrolltx);

        balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0).setIEMPID("50");
        balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0).setIEMPNAME("First1");
        balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0).getIADDRINFO().setILAST(null);
        balf.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP().get(0).getIADDRINFO().setIMI(null);

        QBDTTestHelper.submitQBDTRequest(balf);

        // make sure the liability check from DIY was negated and hidden (-2 token)
        PayrollServices.beginUnitOfWork();
        LiabilityCheck liabilityCheck = LiabilityCheck.findLiabilityCheckBySourceId(company, "-" + ipayrolltx.getIPAYROLLTXID());
        assertNotNull("negated liability check not found", liabilityCheck);
        assertEquals("token not updated", Company.EXCLUDE_TOKEN, liabilityCheck.getQbdtTransactionInfo().getToken());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testCustomerMigratedToTax_SubmitsPayrollBeforeBalf() {
        DataLoadServices.setPSPDate(2012, 10, 1);
        String psid = "122345";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        // add tax service to migrate
        DataLoadServices.addTaxService(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        // the unit test code sets the start date by default. Companies in prod go through the EWS adapter which does not set the start date.
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService taxService = company.getCompanyService(ServiceCode.Tax);
        taxService.setServiceStartDate(null);
        Application.save(taxService);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 10, 15))));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 10, 15, SpcfTimeZone.getLocalTimeZone()))));
        assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)));
        PayrollServices.rollbackUnitOfWork();

        com.intuit.sbd.payroll.psp.common.ofx.request.OFX balf = OFXRequestGenerator.generateBalanceFile(company.getSourceCompanyId(), false);

        QBDTTestHelper.submitQBDTRequest(balf);
    }

    @Test
    public void testCustomerMigratedToTax_Archiving_Error_PSP_9022() {
        DataLoadServices.setPSPDate(2012, 10, 1);
        String psid = "122345";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        // add tax service to migrate
        DataLoadServices.addTaxService(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService taxService = company.getCompanyService(ServiceCode.Tax);
        taxService.setServiceStartDate(null);
        Application.save(taxService);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 10, 15))));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 10, 15, SpcfTimeZone.getLocalTimeZone()))));
        assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)));
        PayrollServices.rollbackUnitOfWork();

        com.intuit.sbd.payroll.psp.common.ofx.request.OFX balf = OFXRequestGenerator.generateBalanceFile(company.getSourceCompanyId(), false);

        QBDTTestHelper.submitQBDTRequest(balf);

    }

    @Test
    public void testCustomerMigratedToTax_Archiving_Error_PSP_9022_Run_Payroll_After_balf() {
        DataLoadServices.setPSPDate(2012, 10, 1);
        String psid = "122345";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, true, ServiceCode.DirectDeposit);

        // add tax service to migrate
        DataLoadServices.addTaxService(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.PendingBalanceFile);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        CompanyService taxService = company.getCompanyService(ServiceCode.Tax);
        taxService.setServiceStartDate(null);
        Application.save(taxService);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), DataLoadServices.createDDPayrollRun(company, new DateDTO(2012, 10, 15))));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 10, 15, SpcfTimeZone.getLocalTimeZone()))));
        assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)));
        PayrollServices.rollbackUnitOfWork();

        com.intuit.sbd.payroll.psp.common.ofx.request.OFX balf = OFXRequestGenerator.generateBalanceFile(company.getSourceCompanyId(), false);

        QBDTTestHelper.submitQBDTRequest(balf);

        PayrollServices.beginUnitOfWork();
        assertNotNull(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 10, 20, SpcfTimeZone.getLocalTimeZone()))));
        assertNotNull(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)));
        PayrollServices.rollbackUnitOfWork();

        //Make sure archived is false for the following
        assertEquals(Application.find(CompanyPayrollItem.class, CompanyPayrollItem.PayrollItem().PayrollItemCode().equalTo(PayrollItemCode.Compensation).And(CompanyPayrollItem.IsArchived().equalTo(Boolean.TRUE))).size(),0);


    }

}
