package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.common.OFXManager;
import com.intuit.sbd.payroll.psp.common.OfxConversionOptions;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import org.junit.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: Feb 21, 2011
 * Time: 3:10:23 PM
 */
@SuppressWarnings("deprecation")
public class OnHoldOperationsWithTaxServiceTests {
    static String psid = "8574536";

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
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OFXRequestGenerator.reset();
        //Setup company
        setupCompany();

    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(null);
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(null);
    }

    @Test
    public void testMissingPaperworkSubStatusPaycheckMod() throws Exception {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.MissingPaperwork, ErrorMessages.PayrollRejectMissingPaperwork());
    }

    @Test
    public void testAMLHoldSubStatusPaycheckMod() throws Exception {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.AMLHold, ErrorMessages.PayrollRejectFraud());
    }

    @Test
    public void testAuditCorrectionsSubStatusPayrollSubmitAddEmployee() throws Exception {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(ServiceSubStatusCode.AuditCorrections);
        OFX payrollOfx = testOnHoldStatusPayrollSubmit(statusList);
        verifyPayrollRejected(payrollOfx, ErrorMessages.PayrollRejectAuditCorrections(), 2, statusList.size());
    }

    @Test
    public void testMissingPaperworkOnHoldSync() throws Exception {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(ServiceSubStatusCode.MissingPaperwork);
        OFX payrollOfx = testOnHoldStatusPayrollSubmit(statusList);
        verifyPayrollRejected(payrollOfx, ErrorMessages.PayrollRejectMissingPaperwork(), 2, statusList.size());
    }

    @Test
    public void testAuditCorrectionsSubStatusPayrollSubmitNoEmpChanges() throws Exception {
        testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode.AuditCorrections, ErrorMessages.PayrollRejectAuditCorrections());
    }

    @Test
    public void testAuditCorrectionsSubStatusPaycheckMod() throws Exception {
        testOnHoldStatusPaycheckMod(ServiceSubStatusCode.AuditCorrections, ErrorMessages.PayrollRejectAuditCorrections());
    }

    @Test
    public void testAuditCorrectionsOnHoldSync() throws Exception {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(ServiceSubStatusCode.AuditCorrections);
        OFX payrollOfx = testOnHoldStatusPayrollSubmit(statusList);
        verifyPayrollRejected(payrollOfx, ErrorMessages.PayrollRejectAuditCorrections(), 2, statusList.size());
    }

    @Test
    public void testOnHoldStatusPrioritizationPaycheckMod() throws Exception {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(ServiceSubStatusCode.AuditCorrections);
        testOnHoldStatusPaycheckMod(statusList, ErrorMessages.PayrollRejectRiskAssessment());

    }

    private void testOnHoldStatusPayrollSubmitNoEmpChanges(ServiceSubStatusCode subStatusCode, ErrorMessage errorMessage) throws Exception {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(subStatusCode);
        testOnHoldStatusPayrollSubmitNoEmpChanges(statusList, errorMessage);
    }

    private void testOnHoldStatusPayrollSubmitNoEmpChanges(List<ServiceSubStatusCode> serviceSubStatusCodes, ErrorMessage errorMessage) throws Exception {
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        Assert.assertEquals(5, payrollRuns.size());
        OFXAssert.assertPayrolls(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        PayrollServices.rollbackUnitOfWork();

        for (ServiceSubStatusCode subStatusCode : serviceSubStatusCodes) {
            addCompanyOnHoldReason(subStatusCode);
        }

        //ofx message for second payrollRun
        OFX secondPayrollOfx = new OFX();
        secondPayrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));

        PayrollServices.beginUnitOfWork();
        List<IEMP> employees = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP();
        List<IPITEM> payrollItems = ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM();
        PayrollServices.rollbackUnitOfWork();

        List<IPAYROLLRUN> secondPayrollRuns = new ArrayList<IPAYROLLRUN>();
        secondPayrollRuns.add(OFXRequestGenerator.generatePayrollRun(employees,
                payrollItems,
                new Date("01/31/2011"),
                new Date("01/31/2011"),
                new Date("01/31/2011"),
                false));

        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                new Date("01/01/2011"),
                employees,
                null,
                null,
                payrollItems,
                null,
                null,
                null,
                null,
                null,
                secondPayrollRuns);
        secondPayrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));
        if (serviceSubStatusCodes.size() == 2) {
            verifyPayrollRejected(secondPayrollOfx, errorMessage, 1, 3);
        } else {
            verifyPayrollRejected(secondPayrollOfx, errorMessage, 1, serviceSubStatusCodes.size());
        }
    }


    private OFX testOnHoldStatusPayrollSubmit(List<ServiceSubStatusCode> serviceSubStatusCodes) throws Exception {
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        OFX pOfx = QBDTTestHelper.submitBalanceFile(company, true);
        for (ServiceSubStatusCode subStatusCode : serviceSubStatusCodes) {
            addCompanyOnHoldReason(subStatusCode);
        }
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(pOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                pOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                new Date("01/31/2011"),
                new Date("01/31/2011"),
                new Date("01/31/2011"),
                false));

        PayrollServices.beginUnitOfWork();
        // find aeic
        CompanyLaw companyLaw = CompanyLaw.findCompanyLaw(company, "143");
        PayrollServices.rollbackUnitOfWork();

        // updated the tax amounts to match the payroll item id
        for (IPAYROLLRUN payrollRun : payrollRuns) {
            for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                for (Iterator<ITAXLINE> iterator = ipaychk.getITAXLINE().iterator(); iterator.hasNext();) {
                    ITAXLINE itaxline = iterator.next();
                    if(itaxline.getIPITEMID().equals(companyLaw.getSourceId())) {
                        iterator.remove();
                    } else {
                        if(itaxline.getIAMT().contains("-")) {
                            itaxline.setIAMT("$-" + itaxline.getIPITEMID() + ".00");
                        } else {
                            itaxline.setIAMT("$" + itaxline.getIPITEMID() + ".00");
                        }
                    }
                }
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(company.getSourceCompanyId(), DataLoadServices.PIN));
        IPAYROLLTRNRQ ipayrolltrnrq = OFXRequestGenerator.generatePayrollRequest(false,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                payrollRuns);
        payrollOfx.setIPAYROLLMSGSRQV1(OFXRequestGenerator.generatePayrollMessage(company, true, ipayrolltrnrq));

        QBDTTestHelper.submitQBDTRequest(payrollOfx, false);

        return payrollOfx;
    }

    private String setupCompany() {
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        return psid;
    }

    public void verifyPayrollRejected(com.intuit.sbd.payroll.psp.common.ofx.request.OFX ofx, ErrorMessage errorMessage, int transmissionCnt, int eventDetailCount) throws Exception {
        // Need session because we are using SPCFCal
        String ofxStr = OFXManager.javaToOFX(ofx, OfxConversionOptions.ESCAPE_OFX_FOR_QB_RULES);
        QBDTTestHelper.processRequestPayrollError(ofxStr, errorMessage, QBOFX.MESSAGE_SEVERITY.INFO);

        QBDTTestHelper.verifyEventExists(EventTypeCode.TransmissionError, transmissionCnt);
        QBDTTestHelper.verifyCompanyEventDetailExists(EventDetailTypeCode.NewOnHoldReason, eventDetailCount);
    }

    private void testOnHoldStatusPaycheckMod(ServiceSubStatusCode companySubStatus, ErrorMessage errorMessage) throws Exception {
        List<ServiceSubStatusCode> statusList = new ArrayList<ServiceSubStatusCode>();
        statusList.add(companySubStatus);
        testOnHoldStatusPaycheckMod(statusList, errorMessage);
    }

    private void testOnHoldStatusPaycheckMod(List<ServiceSubStatusCode> serviceSubStatusCodes, ErrorMessage errorMessage) throws Exception {
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        Assert.assertEquals(5, payrollRuns.size());
        OFXAssert.assertPayrolls(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        PayrollServices.rollbackUnitOfWork();

        //Add company hold
        for (ServiceSubStatusCode companySubStatus : serviceSubStatusCodes) {
            addCompanyOnHoldReason(companySubStatus);
        }

        // Void PayrollRun
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());

        PayrollRun payrollToVoid = PayrollRun.findPayrollRuns(company).get(0);
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollToVoid.getSourcePayRunId());
        List<String> paychecksToVoid = new ArrayList<String>();
        for (Paycheck paycheck : payrollToVoid.getPaycheckCollection()) {
            paychecksToVoid.add(paycheck.getSourcePaycheckId());
        }
        voidPayrollDTO.setPaycheckIdList(paychecksToVoid);
        PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, company.getSourceCompanyId(), voidPayrollDTO);
        PayrollServices.commitUnitOfWork();
    }

    public static void addCompanyOnHoldReason(ServiceSubStatusCode serviceSubStatusCode) {
        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        assertSuccess("OnHoldReason Added", PayrollServices.companyManager.addOnHoldReason(company.getSourceSystemCd(), company.getSourceCompanyId(), serviceSubStatusCode));
        PayrollServices.commitUnitOfWork();
    }
}
