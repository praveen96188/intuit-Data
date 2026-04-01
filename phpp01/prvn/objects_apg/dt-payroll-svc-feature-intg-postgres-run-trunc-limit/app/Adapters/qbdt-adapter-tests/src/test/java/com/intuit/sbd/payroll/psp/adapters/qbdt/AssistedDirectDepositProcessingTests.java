package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.MockSocketManager;
import com.intuit.sbd.payroll.psp.adapters.qbdt.socket.SocketManagerFactory;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PaycheckDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.TransactionCancelEEDTO;
import com.intuit.sbd.payroll.psp.common.ofx.request.*;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYCHKMOD;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX;
import com.intuit.sbd.payroll.psp.common.utils.QBOFX;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.*;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 4/15/12
 * Time: 10:34 AM
 */
@SuppressWarnings("deprecation")
public class AssistedDirectDepositProcessingTests {
    @BeforeClass
    public static void beforeClass() {
        SocketManagerFactory.setInstanceClass(MockSocketManager.class);
    }

    @AfterClass
    public static void afterClass() {
        SocketManagerFactory.setInstanceClass(null);
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 12, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        OFXRequestGenerator.reset();
        MockSocketManager.reset();

        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void test_DD_LiabilityCheck() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        FinancialTransaction ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                                    .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                    .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee)));
        FinancialTransaction monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                    .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                    .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        FinancialTransaction employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        SpcfDecimal liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
                if(itxline.getIISDD() != null) {
                    assertEquals("is dd", "Y", itxline.getIISDD());
                    SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                    assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                    liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);

                } else if(itxline.getIMEMO().contains("direct deposit")) {
                    assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                    SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                    assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                    liabilityCheckTotal = liabilityCheckTotal.add(amount);
                } else if(itxline.getIMEMO().contains("Sales Tax")) {
                    liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                    assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                    SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                    assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                    liabilityCheckTotal = liabilityCheckTotal.add(amount);
                } else if(itxline.getIMEMO().contains("Monthly processing")) {
                    assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                    SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                    assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                    liabilityCheckTotal = liabilityCheckTotal.add(amount);
                }
        }

        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());

        company  = DataLoadServices.refreshCompany(company);
        OFXAssert.assertPayrolls(company, payrollOfx, company.getCurrentToken());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = Application.find(EmployeeBankAccount.class);
        // 5 employees x2, and 1 for the activate DD service
        assertEquals(11, employeeBankAccounts.size());
        DomainEntitySet<PaycheckSplit> paycheckSplits = Application.find(PaycheckSplit.class);
        assertEquals(11, paycheckSplits.size());
        PayrollServices.rollbackUnitOfWork();

        // recover payroll
        OFX sync = OFXRequestGenerator.generateSyncRequest(psid, 0L);
        response = QBDTTestHelper.submitQBDTRequest(sync);
        assertNotNull(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA());
        List<IPAYCHKMOD> ipaychkmods = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYCHKMOD();
        assertEquals(5, ipaychkmods.size());
        OFXAssert.assertPaychecks(payrollOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK(), response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYCHKMOD());

        List<IPAYROLLTX> ipayrolltxes = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD();
        assertEquals(1, ipayrolltxes.size());
        assertEquals(liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltxes.get(0).getIAMT()).negate());
    }

    @Test
    public void testSubmit_EmailParamValueMoreThan4000Chars() throws Exception {
        if(!FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_MTL_EMAIL_ASSISTED_ENABLED,false)){
            return;
        }
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false, false, false, null, 200);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2020, 4, 21);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                new Date("04/21/2020"),
                new Date("04/21/2020"),
                new Date("04/21/2020"),
                false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);

        PayrollServices.beginUnitOfWork();
        CompanyEvent companyEvent = CompanyEvent.findCompanyEvents(company, EventTypeCode.AssistedPayrollConfirmation).getFirst();
        CompanyEventEmail companyEventEmail = companyEvent.getCompanyEventEmailCollection().getFirst();
        DomainEntitySet<CompanyEventEmailParam> params = companyEventEmail.getEmailParamForEmailEvent(EventEmailParamTypeCode.EmployeeList);
        assertEquals(2, params.size());

        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void generateLargePaychecks() {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false, false, false, null, 200);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2020, 4, 21);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                new Date("04/21/2020"),
                new Date("04/21/2020"),
                new Date("04/21/2020"),
                false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);

        PayrollServicesTest.validateQbdtRequestInfo(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.QBDT,null, TransmissionType.BalanceFile,"Sent Balance File"),PayrollServicesTest.getQbdtRequestInfo(200,0,0,0,0,0,26,0,0,0,0,0));
        PayrollServicesTest.validateQbdtRequestInfo(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.QBDT,null, TransmissionType.PayrollSubmission,"Sent 200 Paychecks"),PayrollServicesTest.getQbdtRequestInfo(0,0,0,200,0,0,0,0,0,0,0,0));
    }

    @Test
    public void test_TaxAndDD_LiabilityCheck() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));
        for (IPAYROLLRUN payrollRun : payrollRuns) {
            for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                for (ITAXLINE itaxline : ipaychk.getITAXLINE()) {
                    if (itaxline.getIAMT().contains("-")) {
                        itaxline.setIAMT("$-" + itaxline.getIPITEMID() + ".00");
                    } else {
                        itaxline.setIAMT("$" + itaxline.getIPITEMID() + ".00");
                    }
                }
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        FinancialTransaction ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                                            .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                            .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee)));
        FinancialTransaction monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                                .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                                .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        FinancialTransaction employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        SpcfDecimal liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);

            } else if(itxline.getIMEMO() == null) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(5));
                assertEquals("amount", amount, QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }

        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());

        company  = DataLoadServices.refreshCompany(company);
        OFXAssert.assertPayrolls(company, payrollOfx, company.getCurrentToken());

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<EmployeeBankAccount> employeeBankAccounts = Application.find(EmployeeBankAccount.class);
        // 5 employees x2, and 1 for the activate DD service
        assertEquals(11, employeeBankAccounts.size());
        DomainEntitySet<PaycheckSplit> paycheckSplits = Application.find(PaycheckSplit.class);
        assertEquals(11, paycheckSplits.size());
        PayrollServices.rollbackUnitOfWork();

        // recover payroll
        OFX sync = OFXRequestGenerator.generateSyncRequest(psid, 0L);
        response = QBDTTestHelper.submitQBDTRequest(sync);
        assertNotNull(response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA());
        List<IPAYCHKMOD> ipaychkmods = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYCHKMOD();
        assertEquals(5, ipaychkmods.size());
        OFXAssert.assertPaychecks(payrollOfx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().get(0).getIPAYCHK(), response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYCHKMOD());

        List<IPAYROLLTX> ipayrolltxes = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD();
        assertEquals(1, ipayrolltxes.size());
        assertEquals(liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltxes.get(0).getIAMT()).negate());
    }

    @Test
    public void test_recall_DD_with_live_paycheck() {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
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
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
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
        PayrollServices.rollbackUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitPayroll(company, payrollRunDTO, false);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 15, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        FinancialTransaction ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                                            .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                            .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee)));
        FinancialTransaction monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                    .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                    .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        FinancialTransaction employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        SpcfDecimal liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);

            } else if(itxline.getIMEMO() == null) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 1, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(1)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 2), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }

        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());

        PayrollServices.beginUnitOfWork();
        TransactionCancelEEDTO transactionCancelEEDTO = new TransactionCancelEEDTO();
        transactionCancelEEDTO.setSourcePaycheckIdList(Arrays.asList(paycheckId));
        transactionCancelEEDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        assertSuccess(PayrollServices.payrollManager.cancelEmployeeTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), transactionCancelEEDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 15, SpcfTimeZone.getLocalTimeZone()))));
        ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                    .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                    .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        response = QBDTTestHelper.submitSyncRequest(company, company.getCurrentToken()-1, true);
        ipayrolltx = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLUPDATEDATA().getIPAYROLLTXMOD().get(0);
        liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIPITEMID() != null) {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.NO_FEE_FOR_DIRECT_DEPOSIT, 0), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 2), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }

        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());
    }

    @Test
    public void test_DD_Void_Partial_LiabilityCheck() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        FinancialTransaction ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                                            .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                            .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee)));
        FinancialTransaction monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                                .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                                .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        FinancialTransaction employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        SpcfDecimal liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);

            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());

        company  = DataLoadServices.refreshCompany(company);
        OFXAssert.assertPayrolls(company, payrollOfx, company.getCurrentToken());

        // offload the payroll
        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2012, 1, 27));

        // void 2 of the paychecks after offload
        response = QBDTTestHelper.voidPaychecks(company, payrollRuns, true);
        ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull("Void response", ipayrollrs);
        assertEquals("Liability Check Mod", 1, ipayrollrs.getIPAYROLLTXMOD().size());
        ipayrolltx = ipayrollrs.getIPAYROLLTXMOD().get(0);
        assertEquals("memo", QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK, ipayrolltx.getIMEMO());

        liabilityCheckTotal = SpcfMoney.ZERO;
        SpcfDecimal ddTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                if(itxline.getIMEMO() != null && !itxline.getIAMT().equals("$7.00")) {
                    assertTrue(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK));
                } else {
                    assertTrue(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.DD_OVERPAYMENT));
                }
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                ddTotal = ddTotal.add(ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddTotal);
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());
    }

    @Test
    public void test_TaxAndDD_Void_Partial_LiabilityCheck() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));

        for (IPAYROLLRUN payrollRun : payrollRuns) {
            for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                for (ITAXLINE itaxline : ipaychk.getITAXLINE()) {
                    if (itaxline.getIAMT().contains("-")) {
                        itaxline.setIAMT("$-" + itaxline.getIPITEMID() + ".00");
                    } else {
                        itaxline.setIAMT("$" + itaxline.getIPITEMID() + ".00");
                    }
                }
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        FinancialTransaction ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                                            .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                            .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee)));
        FinancialTransaction monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                                .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                                .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        FinancialTransaction employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        SpcfDecimal liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);
            } else if(itxline.getIMEMO() == null) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(5));
                assertEquals("amount", amount, QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());

        company  = DataLoadServices.refreshCompany(company);
        OFXAssert.assertPayrolls(company, payrollOfx, company.getCurrentToken());

        // offload the payroll
        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2012, 1, 27));

        // void 2 of the paychecks after offload
        response = QBDTTestHelper.voidPaychecks(company, payrollRuns, true);
        ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull("Void response", ipayrollrs);
        assertEquals("Liability Check Mod", 1, ipayrollrs.getIPAYROLLTXMOD().size());
        ipayrolltx = ipayrollrs.getIPAYROLLTXMOD().get(0);
        assertEquals("memo", QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK, ipayrolltx.getIMEMO());

        liabilityCheckTotal = SpcfMoney.ZERO;
        SpcfDecimal ddTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                if(itxline.getIMEMO() != null && !itxline.getIAMT().equals("$7.00")) {
                    assertTrue(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK));
                } else {
                    assertTrue(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.DD_OVERPAYMENT));
                }
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                ddTotal = ddTotal.add(ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);
            } else if(itxline.getIMEMO().equals(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK)) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                // 2 of the 5 paychecks were voided
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(3));
                assertEquals("amount", amount, QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.EXCESS_TAX_RESULT_OF_VOID)) {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddTotal);
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());
    }

    @Test
    public void test_TaxAndDD_Void_Complete_LiabilityCheck() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));

        for (IPAYROLLRUN payrollRun : payrollRuns) {
            for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                for (ITAXLINE itaxline : ipaychk.getITAXLINE()) {
                    if (itaxline.getIAMT().contains("-")) {
                        itaxline.setIAMT("$-" + itaxline.getIPITEMID() + ".00");
                    } else {
                        itaxline.setIAMT("$" + itaxline.getIPITEMID() + ".00");
                    }
                }
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        FinancialTransaction ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                                            .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                            .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee)));
        FinancialTransaction monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                                .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                                .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        FinancialTransaction employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        SpcfDecimal liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);
            } else if(itxline.getIMEMO() == null) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(5));
                assertEquals("amount", amount, QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());

        company  = DataLoadServices.refreshCompany(company);
        OFXAssert.assertPayrolls(company, payrollOfx, company.getCurrentToken());

        // offload the payroll
        DataLoadServices.runOffload(company, SpcfCalendar.createInstance(2012, 1, 27));

        // void 2 of the paychecks after offload
        response = QBDTTestHelper.voidPaychecks(company, payrollRuns, false);
        ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull("Void response", ipayrollrs);
        assertEquals("Liability Check Mod", 1, ipayrollrs.getIPAYROLLTXMOD().size());
        ipayrolltx = ipayrollrs.getIPAYROLLTXMOD().get(0);
        assertEquals("memo", QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK, ipayrolltx.getIMEMO());

        liabilityCheckTotal = SpcfMoney.ZERO;
        SpcfDecimal ddTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                if(itxline.getIMEMO() != null && !itxline.getIAMT().equals("$7.00")) {
                    assertTrue(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK));
                } else {
                    assertTrue(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.DD_OVERPAYMENT));
                }
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                ddTotal = ddTotal.add(ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);
            } else if(itxline.getIMEMO().equals(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK)) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                // 2 of the 5 paychecks were voided
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(0));
                assertEquals("amount", amount, QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.EXCESS_TAX_RESULT_OF_VOID)) {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddTotal);
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());
    }

    @Test
    public void test_TaxAndDD_Recall_Partial_LiabilityCheck() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));

        for (IPAYROLLRUN payrollRun : payrollRuns) {
            for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                for (ITAXLINE itaxline : ipaychk.getITAXLINE()) {
                    if (itaxline.getIAMT().contains("-")) {
                        itaxline.setIAMT("$-" + itaxline.getIPITEMID() + ".00");
                    } else {
                        itaxline.setIAMT("$" + itaxline.getIPITEMID() + ".00");
                    }
                }
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        FinancialTransaction ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                                            .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                            .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee)));
        FinancialTransaction monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                                .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                                .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        FinancialTransaction employeeFee = payrollRun.getFinancialTransactionCollection()
                                                    .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                    .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        SpcfDecimal liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);
            } else if(itxline.getIMEMO() == null) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(5));
                assertEquals("amount", amount, QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());

        company  = DataLoadServices.refreshCompany(company);
        OFXAssert.assertPayrolls(company, payrollOfx, company.getCurrentToken());

        // recall 2 of the paychecks
        response = QBDTTestHelper.voidPaychecks(company, payrollRuns, true);
        ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull("Recall response", ipayrollrs);
        assertEquals("Liability Check Mod", 1, ipayrollrs.getIPAYROLLTXMOD().size());
        ipayrolltx = ipayrollrs.getIPAYROLLTXMOD().get(0);
        assertEquals("memo", QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK, ipayrolltx.getIMEMO());

        PayrollServices.beginUnitOfWork();
        payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit)
                                                                                                .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                       .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                       .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee))
                                                                       .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        monthlyFee = payrollRun.getFinancialTransactionCollection()
                                           .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                           .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee))
                                                                           .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));

        employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        liabilityCheckTotal = SpcfMoney.ZERO;
        SpcfDecimal ddTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                if(itxline.getIMEMO() != null && !itxline.getIAMT().equals("$7.00")) {
                    assertTrue(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK));
                } else {
                    assertTrue(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.DD_OVERPAYMENT));
                }
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                ddTotal = ddTotal.add(ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);
            } else if(itxline.getIMEMO().equals(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK)) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                // 2 of the 5 paychecks were recalled
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(3));
                assertEquals("amount", amount, QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.EXCESS_TAX_RESULT_OF_VOID)) {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 3, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(3)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddTotal);
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());
    }

    @Test
    public void test_TaxAndDD_Recall_LiabilityCheck() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));

        for (IPAYROLLRUN payrollRun : payrollRuns) {
            for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                for (ITAXLINE itaxline : ipaychk.getITAXLINE()) {
                    if (itaxline.getIAMT().contains("-")) {
                        itaxline.setIAMT("$-" + itaxline.getIPITEMID() + ".00");
                    } else {
                        itaxline.setIAMT("$" + itaxline.getIPITEMID() + ".00");
                    }
                }
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        FinancialTransaction ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                                            .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                            .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee)));
        FinancialTransaction monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                    .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                    .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        FinancialTransaction employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        SpcfDecimal liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);
            } else if(itxline.getIMEMO() == null) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(5));
                assertEquals("amount", amount, QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());

        company  = DataLoadServices.refreshCompany(company);
        OFXAssert.assertPayrolls(company, payrollOfx, company.getCurrentToken());

        // recall 2 of the paychecks
        response = QBDTTestHelper.voidPaychecks(company, payrollRuns, false);
        ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull("Recall response", ipayrollrs);
        assertEquals("Liability Check Mod", 1, ipayrollrs.getIPAYROLLTXMOD().size());
        ipayrolltx = ipayrollrs.getIPAYROLLTXMOD().get(0);
        assertEquals("memo", QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK, ipayrolltx.getIMEMO());

        PayrollServices.beginUnitOfWork();
        payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                       .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                       .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee))
                                                                       .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));
        monthlyFee = payrollRun.getFinancialTransactionCollection()
                               .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                               .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee))
                                                               .And(FinancialTransaction.CurrentTransactionState().TransactionStateCd().equalTo(TransactionStateCode.Created)));

        employeeFee = payrollRun.getFinancialTransactionCollection()
                                .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        liabilityCheckTotal = SpcfMoney.ZERO;
        SpcfDecimal ddTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                if(itxline.getIMEMO() != null && !itxline.getIAMT().equals("$7.00")) {
                    assertTrue(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK));
                } else {
                    assertTrue(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.DD_OVERPAYMENT));
                }
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                ddTotal = ddTotal.add(ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);
            } else if(itxline.getIMEMO().equals(QBOFX.MEMOS.VOID.ADJUSTED_FOR_VOIDED_PAYCHECK)) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(0));
                assertEquals("amount", amount, QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains(QBOFX.MEMOS.VOID.EXCESS_TAX_RESULT_OF_VOID)) {
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.NO_FEE_FOR_DIRECT_DEPOSIT, 0), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, SpcfMoney.ZERO);
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());
    }

    @Test
    public void test_TaxAndDD_100k_LiabilityCheck() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));

        for (IPAYROLLRUN payrollRun : payrollRuns) {
            for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                for (ITAXLINE itaxline : ipaychk.getITAXLINE()) {
                    if (itaxline.getIAMT().contains("-")) {
                        itaxline.setIAMT("$-" + itaxline.getIPITEMID() + "0000.00");
                    } else {
                        itaxline.setIAMT("$" + itaxline.getIPITEMID() + "0000.00");
                    }
                }
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = null;
        for (IPAYROLLTX ipayrolltx1 : ipayrollrs.getIPAYROLLTX()) {
            if(ipayrolltx1.getINAME().equals(QBOFX.AGENCIES.QUICKBOOKS_PAYROLL_SERVICE)) {
                ipayrolltx = ipayrolltx1;
            }
        }

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        FinancialTransaction ddPaycheckFeeDebit = payrollRun.getFinancialTransactionCollection()
                                                            .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                            .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.DirectDepositFee)));
        FinancialTransaction monthlyFee = payrollRun.getFinancialTransactionCollection()
                                                                .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                                .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));

        FinancialTransaction employeeFee = payrollRun.getFinancialTransactionCollection()
                                                     .findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit)
                                                                                     .And(FinancialTransaction.BillingDetail().OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        SpcfDecimal liabilityCheckTotal = SpcfMoney.ZERO;
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                liabilityCheckTotal = liabilityCheckTotal.add(ddAmount);
            } else if(itxline.getIMEMO() == null) {
                assertNotNull("payroll item id", itxline.getIPITEMID());
                SpcfDecimal amount = QBOFX.mapOFXStringToMoney(itxline.getIPITEMID()).multiply(SpcfDecimal.createInstance(50000));
                assertEquals("amount", amount, QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("direct deposit")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_DIRECT_DEPOSIT_AT_EACH, 5, ddPaycheckFeeDebit.getFinancialTransactionAmount().divide(SpcfDecimal.createInstance(5)).toString()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, ddPaycheckFeeDebit.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Sales Tax")) {
                liabilityCheckTotal = liabilityCheckTotal.add(QBOFX.mapOFXStringToMoney(itxline.getIAMT()));
            } else if(itxline.getIMEMO().contains("employee(s) paid")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.FEE_FOR_EMPLOYEE_PAID, 5), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, employeeFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            } else if(itxline.getIMEMO().contains("Monthly processing")) {
                assertEquals("memo", String.format(BillingDetail.MEMOS.MONTHLY_PROCESSING_FEE, BillingDetail.MONTHS[monthlyFee.getPayrollRun().getPaycheckDate().getMonth() - 1], monthlyFee.getPayrollRun().getPaycheckDate().getYear()), itxline.getIMEMO());
                SpcfMoney amount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("amount", amount, monthlyFee.getFinancialTransactionAmount());
                liabilityCheckTotal = liabilityCheckTotal.add(amount);
            }
        }
        assertEquals("amount", liabilityCheckTotal, QBOFX.mapOFXStringToMoney(ipayrolltx.getIAMT()).negate());

        company  = DataLoadServices.refreshCompany(company);
        OFXAssert.assertPayrolls(company, payrollOfx, company.getCurrentToken());
    }

    @Test
    public void test_TaxAndDD_DDInBalanceFile() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);


        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));
        for (IPAYROLLRUN payrollRun : payrollRuns) {
            for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                for (ITAXLINE itaxline : ipaychk.getITAXLINE()) {
                    if (itaxline.getIAMT().contains("-")) {
                        itaxline.setIAMT("$-" + itaxline.getIPITEMID() + ".00");
                    } else {
                        itaxline.setIAMT("$" + itaxline.getIPITEMID() + ".00");
                    }
                }
            }
        }

        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN().addAll(payrollRuns);
        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        // dd financial transactions should not be created for balance files
        assertNull("ER DD Debit found", ddDebit);
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_voidWithoutPaycheckDetails() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(1999, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));
        for (IPAYROLLRUN payrollRun : payrollRuns) {
            for (IPAYCHK ipaychk : payrollRun.getIPAYCHK()) {
                ipaychk.getIDDLINE().clear();
                ipaychk.getIADJLINE().clear();
                ipaychk.getIHRLYWAGELINE().clear();
                ipaychk.getISALARYLINE().clear();
                ipaychk.getITAXLINE().clear();
                ipaychk.setIVOID(QBOFX.Y_N(true));
            }
        }

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        for (Paycheck paycheck : payrollRun.getPaycheckCollection()) {
            assertTrue(paycheck.isRecalled());
            assertEquals(0, paycheck.getTaxCollection().size());
            assertEquals(0, paycheck.getCompensationCollection().size());
            assertEquals(0, paycheck.getDeductionCollection().size());
            assertEquals(0, paycheck.getPaycheckSplits().size());
            assertFalse(paycheck.getQbdtPaycheckInfo().getIsAssisted());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void test_DD_LiabilityCheck_multipleDDItems() throws Exception {
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false);

        // add a second dd item
        List<IPITEM> ipitems = OFXRequestGenerator.generatePayrollItems(null, QBOFX.OFXPayrollItemType.DirectDeposit);
        String newDDItemId = ipitems.get(0).getIPITEMID();
        ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM().addAll(ipitems);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               new Date("01/31/2012"),
                                                               false));

        OFX payrollOfx = new OFX();
        payrollOfx.setSIGNONMSGSRQV1(OFXRequestGenerator.generateSignOnMessage(psid, DataLoadServices.PIN));
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

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitQBDTRequest(payrollOfx);
        IPAYROLLRS ipayrollrs = response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getIPAYROLLRS();
        assertNotNull(ipayrollrs);
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(Application.find(PayrollRun.class, PayrollRun.PaycheckDate().equalTo(SpcfCalendar.createInstance(2012, 1, 31, SpcfTimeZone.getLocalTimeZone()))));
        FinancialTransaction ddDebit = payrollRun.getFinancialTransactionCollection().findEntity(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerDdDebit));
        PayrollServices.rollbackUnitOfWork();

        OFXAssert.assertNonVoidLiabilityCheckExceptTransactionLines(company, ipayrolltx, ipayrolltx.getIDTPAYPDEND());
        for (com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIISDD() != null) {
                assertEquals("is dd", "Y", itxline.getIISDD());
                SpcfMoney ddAmount = QBOFX.mapOFXStringToMoney(itxline.getIAMT());
                assertEquals("dd amount", ddDebit.getFinancialTransactionAmount(), ddAmount);
                assertEquals("latest dd pitem used", newDDItemId, itxline.getIPITEMID());
            }
        }
    }

    @Test
    public void test_OldQBUniqueID_NewPaycheckId() {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2008, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax, ServiceCode.DirectDeposit);
        DataLoadServices.activateDDService(company);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        DataLoadServices.addCompanyLaws(company, "66", "61", "62", "63", "64", "1");
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
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
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(2012, 1, 15), emps, lawAmounts);
        String paycheckId = null;
        int nextPaycheckId = Integer.parseInt(company.getNextPaycheckId());
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
            paycheckDTO.getQBDTPaycheckInfoDTO().setListId(paycheckDTO.getPaycheckId());
        }
        PayrollServices.rollbackUnitOfWork();

        company = DataLoadServices.refreshCompany(company);
        QBDTTestHelper.submitPayroll(company, payrollRunDTO, false);

        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setPaycheckId(nextPaycheckId++ + "");
        }

        com.intuit.sbd.payroll.psp.common.ofx.response.OFX response = QBDTTestHelper.submitPayroll(company, payrollRunDTO, false, false, false);
        assertTrue("Unexpected error returned", response.getIPAYROLLMSGSRSV1().getIPAYROLLUPDATERS().getIPAYROLLTRNRS().getSTATUS().getMESSAGE().contains("Message Code 2120"));
    }

}
