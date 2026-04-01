package com.intuit.sbd.payroll.psp.adapters.qbdt;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYCHK;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLRUN;
import com.intuit.sbd.payroll.psp.common.ofx.request.IPAYROLLTRNRQ;
import com.intuit.sbd.payroll.psp.common.ofx.request.OFX;
import com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLRS;
import com.intuit.sbd.payroll.psp.common.ofx.response.ITXLINE;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: 7/10/12
 * Time: 12:56 PM
 */
public class AssistedBillingTests {
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
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 11, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testAllPayrollFees_Backdated_ExtraStateFees_RefundBackdatedFees(){
        String psid = "123456789";

        DataLoadServices.setPSPDate(2011, 3, 15);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.updatePaymentTemplateSupportedDate("PA-501-PAYMENT", SpcfCalendar.createInstance(2011, 1, 1));

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        company = DataLoadServices.refreshCompany(company);

        OFX balanceFile = OFXRequestGenerator.generateEmptyBalanceFile(company, "01/01/2011");
        QBDTTestHelper.submitQBDTRequestStringResponse(balanceFile);
        QBDTTestHelper.assertNoErrorRequests(company);

        EmployeeDTO ee1 = DataLoadServices.createEE(null, true);

        DataLoadServices.addEE(company, ee1);

        PayrollItemRepository payrollItemRepository = new PayrollItemRepository();
        OFXPaycheckGenerator ofxPaycheckGenerator = new OFXPaycheckGenerator(payrollItemRepository, ee1.getEmployeeId(),"03/15/2011",1250.00);
        ofxPaycheckGenerator.addSalaryLine(80, 1250, 7500);
        ofxPaycheckGenerator.addTaxLine(24,100, PayrollItemRepository.Tax.CA_SUI_ER);
        ofxPaycheckGenerator.addTaxLine(62,345, PayrollItemRepository.Tax.CA_SIT);
        ofxPaycheckGenerator.addTaxLine(44,666, PayrollItemRepository.Tax.CA_ETT);
        ofxPaycheckGenerator.addTaxLine(80,800, PayrollItemRepository.Tax.PA_SWT);
        ofxPaycheckGenerator.addTaxLine(67,140, PayrollItemRepository.Tax.FIT);
        ofxPaycheckGenerator.addTaxLine(34,234, PayrollItemRepository.Tax.FUTA);
        ofxPaycheckGenerator.addTaxLine(12,167, PayrollItemRepository.Tax.FICA_EE);
        ofxPaycheckGenerator.addDeductionLine(-4,-44, "1", "2");

        IPAYCHK ipaychk = ofxPaycheckGenerator.getPaycheck();
        IPAYROLLRUN payrollRun = OFXRequestGenerator.generatePayrollRun(ipaychk.getIDTTX());
        payrollRun.getIPAYCHK().add(ipaychk);

        company = DataLoadServices.refreshCompany(company);
        OFX ofx = OFXRequestGenerator.generateEmptyOFX(company);
        OFXRequestGenerator.addPayrollRunToOfx(ofx, payrollRun);
        OFXRequestGenerator.addPayrollItemsToOfx(ofx, payrollItemRepository.getAllPayrollItems());

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);
        OFXAssert.assertPayrolls(company, ofx);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.FirstPayrollReceived);
        assertEquals("FirstPayrollReceived company events", 1, companyEvents.size());
        DomainEntitySet<CompanyEventDetail> companyEventDetails = companyEvents.get(0).getCompanyEventDetails(EventDetailTypeCode.ServiceCode);
        assertEquals("CompanyEventDetails for FirstPayrollReceived with Service code", 1, companyEventDetails.size());
        assertEquals("CompanyEventDetails Value for Service code", ServiceCode.Tax.toString(), companyEventDetails.get(0).getValue());

        //Asserts for billing fees
        DomainEntitySet<BillingDetail> billingDetails = Application.find(BillingDetail.class);
        assertEquals("All payroll billing fees", 5, billingDetails.size());

        Application.refresh(company);
        Offering offering = company.getDirectDepositCompanyOffering().getOffering();
        OfferingServiceCharge serviceCharge = offering.getCharge(OfferingServiceChargeType.ExtraStateFee, 1);

        BillingDetail billingDetail = assertOne(billingDetails.find(BillingDetail.OfferingServiceChargeType().equalTo(OfferingServiceChargeType.ExtraStateFee).And(BillingDetail.ItemTotal().equalTo(SpcfMoney.ZERO))));
        assertNull("Offering Service charge price", billingDetail.getOfferingServiceChargePrice());
        assertEquals("First Extra state Fee SKU", serviceCharge.getSKU(), billingDetail.getItemSku());

        billingDetail = assertOne(billingDetails.find(BillingDetail.OfferingServiceChargeType().equalTo(OfferingServiceChargeType.ExtraStateFee).And(BillingDetail.ItemTotal().greaterThan(SpcfMoney.ZERO))));
        assertEquals("Extra state fee amount for the second state", offering.getListPrice(OfferingServiceChargeType.ExtraStateFee, 1).add(new SpcfMoney(".08")), billingDetail.getItemTotal());
        assertEquals("Extra state fee SKU", serviceCharge.getSKU(), billingDetail.getItemSku());
        assertEquals("Extra state fee price", serviceCharge.getCurrentPrice(), billingDetail.getOfferingServiceChargePrice());
        assertEquals("Extra state base price", serviceCharge.getCurrentPrice().getBasePrice(), billingDetail.getBasePrice());
        assertEquals("Extra state unit price", serviceCharge.getCurrentPrice().getUnitPrice(), billingDetail.getUnitPrice());

        billingDetail = assertOne(billingDetails.find(BillingDetail.OfferingServiceChargeType().equalTo(OfferingServiceChargeType.MonthlyFee)));
        serviceCharge = offering.getCharge(OfferingServiceChargeType.MonthlyFee, 1);
        assertEquals("Monthly fee amount for the second state", offering.getListPrice(OfferingServiceChargeType.MonthlyFee, 1).add(new SpcfMoney(".08")), billingDetail.getItemTotal());
        assertEquals("Monthly fee SKU", serviceCharge.getSKU(), billingDetail.getItemSku());
        assertEquals("Monthly fee price", serviceCharge.getCurrentPrice(), billingDetail.getOfferingServiceChargePrice());
        assertEquals("Monthly fee base price", serviceCharge.getCurrentPrice().getBasePrice(), billingDetail.getBasePrice());
        assertEquals("Monthly fee unit price", serviceCharge.getCurrentPrice().getUnitPrice(), billingDetail.getUnitPrice());

        billingDetail = assertOne(billingDetails.find(BillingDetail.OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        serviceCharge = offering.getCharge(OfferingServiceChargeType.EmployeesPaid, 1);
        assertEquals("EmployeesPaid fee amount for the second state", offering.getListPrice(OfferingServiceChargeType.EmployeesPaid, 1).add(new SpcfMoney(".08")), billingDetail.getItemTotal());
        assertEquals("EmployeesPaid fee SKU", serviceCharge.getSKU(), billingDetail.getItemSku());
        assertEquals("EmployeesPaid fee price", serviceCharge.getCurrentPrice(), billingDetail.getOfferingServiceChargePrice());
        assertEquals("EmployeesPaid base price", serviceCharge.getCurrentPrice().getBasePrice(), billingDetail.getBasePrice());
        assertEquals("EmployeesPaid unit price", serviceCharge.getCurrentPrice().getUnitPrice(), billingDetail.getUnitPrice());

        billingDetail = assertOne(billingDetails.find(BillingDetail.OfferingServiceChargeType().equalTo(OfferingServiceChargeType.BackdatedPayroll)));
        serviceCharge = offering.getCharge(OfferingServiceChargeType.BackdatedPayroll, 1);
        assertEquals("BackdatedPayroll fee amount for the second state", offering.getListPrice(OfferingServiceChargeType.BackdatedPayroll, 1).add(new SpcfMoney(".08")), billingDetail.getItemTotal());
        assertEquals("BackdatedPayroll fee SKU", serviceCharge.getSKU(), billingDetail.getItemSku());
        assertEquals("BackdatedPayroll fee price", serviceCharge.getCurrentPrice(), billingDetail.getOfferingServiceChargePrice());
        assertEquals("BackdatedPayroll base price", serviceCharge.getCurrentPrice().getBasePrice(), billingDetail.getBasePrice());
        assertEquals("BackdatedPayroll unit price", serviceCharge.getCurrentPrice().getUnitPrice(), billingDetail.getUnitPrice());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload();

        PayrollServices.beginUnitOfWork();
        Application.refresh(billingDetail);
        ERRefundDTO erRefundDTO = new ERRefundDTO(billingDetail.getFeeTransaction().getId().toString(), new SpcfMoney("100.00"),
                                                  new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.Wire);

        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), erRefundDTO);
        assertSuccess(processResult);
        FinancialTransaction financialTransaction = processResult.getResult();
        assertEquals("Fee Refund amount", billingDetail.getFeeTransaction().getFinancialTransactionAmount(), financialTransaction.getFinancialTransactionAmount());
        assertEquals("Fee Refund transaction SKU", billingDetail.getFeeTransaction().getSku(), financialTransaction.getSku());
        assertEquals("Fee Refund transaction original transaction", billingDetail.getFeeTransaction(), financialTransaction.getOriginalTransaction());
        company.setCurrentToken(company.getCurrentToken() + 1);
        PayrollServices.commitUnitOfWork();

        //Submitting another backdated payroll
        DataLoadServices.setPSPDate(2011, 3, 16);
        payrollItemRepository = new PayrollItemRepository();
        ofxPaycheckGenerator = new OFXPaycheckGenerator(payrollItemRepository, ee1.getEmployeeId(),"03/16/2011",1250.00);
        ofxPaycheckGenerator.addSalaryLine(80, 1250, 7500);
        ofxPaycheckGenerator.addTaxLine(62,345, PayrollItemRepository.Tax.CA_SIT);
        ofxPaycheckGenerator.addTaxLine(67,140, PayrollItemRepository.Tax.FIT);
        ofxPaycheckGenerator.addDeductionLine(-4,-44, "1", "2");

        ipaychk = ofxPaycheckGenerator.getPaycheck();
        payrollRun = OFXRequestGenerator.generatePayrollRun(ipaychk.getIDTTX());
        payrollRun.getIPAYCHK().add(ipaychk);

        ofx = OFXRequestGenerator.generateEmptyOFX(company);
        OFXRequestGenerator.addPayrollRunToOfx(ofx, payrollRun);
        OFXRequestGenerator.addPayrollItemsToOfx(ofx, payrollItemRepository.getAllPayrollItems());

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);
        QBDTTestHelper.assertNoErrorRequests(company);
        OFXAssert.assertPayrolls(company, ofx);

        PayrollServices.beginUnitOfWork();
        SpcfCalendar today = PSPDate.getPSPTime().copy();
        CalendarUtils.clearTime(today);
        PayrollRun payrollRun1 = assertOne(PayrollRun.findPayrollRuns(company, today, null));
        billingDetails = payrollRun1.getBillingDetailCollection();
        assertEquals("Billing details for second payroll run", 2, billingDetails.size());

        billingDetail = assertOne(billingDetails.find(BillingDetail.OfferingServiceChargeType().equalTo(OfferingServiceChargeType.EmployeesPaid)));
        serviceCharge = offering.getCharge(OfferingServiceChargeType.EmployeesPaid, 1);
        assertEquals("EmployeesPaid fee amount for the second state", offering.getListPrice(OfferingServiceChargeType.EmployeesPaid, 1).add(new SpcfMoney(".08")), billingDetail.getItemTotal());
        assertEquals("EmployeesPaid fee SKU", serviceCharge.getSKU(), billingDetail.getItemSku());
        assertEquals("EmployeesPaid fee price", serviceCharge.getCurrentPrice(), billingDetail.getOfferingServiceChargePrice());
        assertEquals("EmployeesPaid base price", serviceCharge.getCurrentPrice().getBasePrice(), billingDetail.getBasePrice());
        assertEquals("EmployeesPaid unit price", serviceCharge.getCurrentPrice().getUnitPrice(), billingDetail.getUnitPrice());

        billingDetail = assertOne(billingDetails.find(BillingDetail.OfferingServiceChargeType().equalTo(OfferingServiceChargeType.BackdatedPayroll)));
        serviceCharge = offering.getCharge(OfferingServiceChargeType.BackdatedPayroll, 1);
        assertEquals("BackdatedPayroll fee amount for the second state", offering.getListPrice(OfferingServiceChargeType.BackdatedPayroll, 1).add(new SpcfMoney(".08")), billingDetail.getItemTotal());
        assertEquals("BackdatedPayroll fee SKU", serviceCharge.getSKU(), billingDetail.getItemSku());
        assertEquals("BackdatedPayroll fee price", serviceCharge.getCurrentPrice(), billingDetail.getOfferingServiceChargePrice());
        assertEquals("BackdatedPayroll base price", serviceCharge.getCurrentPrice().getBasePrice(), billingDetail.getBasePrice());
        assertEquals("BackdatedPayroll unit price", serviceCharge.getCurrentPrice().getUnitPrice(), billingDetail.getUnitPrice());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload();

        DataLoadServices.setPrincipalToAgent(OperationId.CreateRefundTransaction);
        
        PayrollServices.beginUnitOfWork();
        Application.refresh(billingDetail);
        erRefundDTO = new ERRefundDTO(billingDetail.getFeeTransaction().getId().toString(), new SpcfMoney("100.00"),
                                                  new DateDTO(PSPDate.getPSPTime()), SettlementTypeDTO.ACH);

        processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), erRefundDTO);
        assertEquals("Error messages", 1, processResult.getErrorMessages().size());
        assertEquals("Error message code", "618", processResult.getErrorMessages().get(0).getMessageCode());
        assertEquals("Error message", "Already refunded one time courtesy refund on BackdatedPayroll fees.", processResult.getErrorMessages().get(0).getMessage());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.setPrincipalToAgent(OperationId.CreateMultipleBackdatingRefunds);

        PayrollServices.beginUnitOfWork();
        processResult = PayrollServices.financialTransactionManager.refundEmployerTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), erRefundDTO);
        assertSuccess(processResult);
        financialTransaction = processResult.getResult();
        assertEquals("Fee Refund amount", billingDetail.getFeeTransaction().getFinancialTransactionAmount(), financialTransaction.getFinancialTransactionAmount());
        assertEquals("Fee Refund transaction SKU", billingDetail.getFeeTransaction().getSku(), financialTransaction.getSku());
        assertEquals("Fee Refund transaction original transaction", billingDetail.getFeeTransaction(), financialTransaction.getOriginalTransaction());
        PayrollServices.commitUnitOfWork();

    }

    @Test
    public void testPayrollsAddedWithBalanceFile_Updating_CompanyOffering() throws Exception {
        // If assisted offering is present and changing the price type doesn't change the offering to other assisted offering.
        String psid = "123456789";

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);
        CompanyService companyService = company.getCompanyService(ServiceCode.Tax);
        assertNotNull(companyService);
        assertEquals("Company status is PendingBalanceFile prior to balance file submit", ServiceSubStatusCode.PendingBalanceFile, companyService.getStatusCd());

        CompanyOffering companyOffering = company.getDirectDepositCompanyOffering();
        assertEquals("Default offering", OfferingCode.AP79FY13, companyOffering.getOffering().getOfferingCode());
        assertEquals("Price Type", "Standard", company.getPriceType());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(psid, SourceSystemCode.QBDT);

        CompanyDTO dto = PayrollServices.dtoFactory.create(company);
        String papPriceType = "PAP Price";
        dto.setPriceType(papPriceType);

        assertSuccess(PayrollServices.companyManager.updateCompany(SourceSystemCode.QBDT, psid, dto));

        PayrollServices.commitUnitOfWork();

        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, true);

        QBDTTestHelper.submitQBDTRequestStringResponse(ofx);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        companyService = company.getCompanyService(ServiceCode.Tax);
        assertEquals("Company status is ActiveCurrent after balance file submit", ServiceSubStatusCode.ActiveCurrent, companyService.getStatusCd());
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        assertEquals(5, payrollRuns.size());
        OFXAssert.assertPayrolls(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPAYROLLRUN(), company);
        companyOffering = company.getDirectDepositCompanyOffering();
        assertEquals("Price Type", papPriceType, company.getPriceType());
        assertEquals("Default offering", OfferingCode.AP79FY13, companyOffering.getOffering().getOfferingCode());
        PayrollServices.rollbackUnitOfWork();

    }

    @Test
    public void testMultiplePayrollsOneTransmission() {
        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);

        DataLoadServices.updatePaymentTemplateSupportedDate("CA-PITSDI-PAYMENT", SpcfCalendar.createInstance(2005, 1, 1));

        // add 2 payrolls for the same month
        DataLoadServices.setPSPDate(2012, 1, 1);
        List<IPAYROLLRUN> payrollRuns = new ArrayList<IPAYROLLRUN>();
        payrollRuns.add(OFXRequestGenerator.generatePayrollRun(ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIEMP(),
                                                               ofx.getIPAYROLLMSGSRQV1().getIPAYROLLUPDATERQ().getIPAYROLLTRNRQ().getIPAYROLLRQ().getIPITEM(),
                                                               new Date("01/15/2012"),
                                                               new Date("01/15/2012"),
                                                               new Date("01/15/2012"),
                                                               false));

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
        assertEquals(2, ipayrollrs.getIPAYROLLTX().size());

        // state fees should be on the first lc
        com.intuit.sbd.payroll.psp.common.ofx.response.IPAYROLLTX ipayrolltx = ipayrollrs.getIPAYROLLTX().get(0);
        boolean stateFeeFound = false;
        for (ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIMEMO() != null && itxline.getIMEMO().contains("state")) {
                assertEquals(String.format(BillingDetail.MEMOS.NO_STATE_FEE, "CA", "Jan", 2012), itxline.getIMEMO());
                stateFeeFound = true;
            }
        }
        assertTrue("State fee not found", stateFeeFound);

        ipayrolltx = ipayrollrs.getIPAYROLLTX().get(1);
        stateFeeFound = false;
        for (ITXLINE itxline : ipayrolltx.getITXLINE()) {
            if(itxline.getIMEMO() != null && itxline.getIMEMO().contains("State")) {
                stateFeeFound = true;
            }
        }
        assertFalse("State fee found on second liability check", stateFeeFound);
    }


     //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
     @Test
     @Ignore
     public void testVoidPayrollWithOnlyAFeeDebit() {
        DataLoadServices.setPSPDate(2012, 10, 1);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);

        List<Employee> employees = DataLoadServices.addEEs(company, 1, false, true);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(2012, 10, 5), employees, new HashMap<String, String>());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        CompanyPayrollItem companyPayrollItem = CompanyPayrollItem.findByPayrollItemType(company, PayrollItemType.Compensation).get(0);
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.getDdTransactions().clear();
            paycheckDTO.getLiabilityTransactions().clear();

            CompensationTransactionDTO compensationTransactionDTO = new CompensationTransactionDTO();
            compensationTransactionDTO.setCompensationAmount(new SpcfMoney("5.00"));
            compensationTransactionDTO.setCompensationYTDAmount(new SpcfMoney("5.00"));
            compensationTransactionDTO.setHoursWorked(SpcfDecimal.createInstance(5));
            compensationTransactionDTO.setPayStubOrder(0L);
            compensationTransactionDTO.setQBDTPaylineInfoDTO(new QBDTPaylineInfoDTO());
            compensationTransactionDTO.setSourcePayrollItemId(companyPayrollItem.getSourcePayrollItemId());
            paycheckDTO.getCompensationTransactions().add(compensationTransactionDTO);
        }
        PayrollServices.rollbackUnitOfWork();

        // submit payroll with only compensation information (no taxes or dd)
        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        // there should only be fees created
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRuns(company));
        assertEquals("Paycheck date", SpcfCalendar.createInstance(2012, 10, 5, SpcfTimeZone.getLocalTimeZone()), payrollRun.getPaycheckDate().toLocal());
        SpcfCalendar offloadDate = null;
        assertTrue("Payroll has fts", payrollRun.getFinancialTransactionCollection().size() > 0);
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            assertTrue("Not a fee transaction", financialTransaction.getTransactionType().getTransactionTypeCd().in(TransactionTypeCode.EmployerFeeDebit,
                                                                                                                    TransactionTypeCode.ServiceSalesAndUseTax));
            if(offloadDate == null) {
                offloadDate = financialTransaction.getMoneyMovementTransaction().getInitiationDate();
            }
        }
        if(offloadDate == null) {
            fail("Offload date not found");
        }
        PayrollServices.rollbackUnitOfWork();

        // offload fee transactions
        DataLoadServices.runOffload(company, offloadDate.toLocal());

        // verify offload
        PayrollServices.beginUnitOfWork();
        payrollRun = assertOne(PayrollRun.findPayrollRuns(company));
        assertTrue("Payroll has fts", payrollRun.getFinancialTransactionCollection().size() > 0);
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection()) {
            assertEquals("Transaction not executed", TransactionStateCode.Executed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }
        PayrollServices.rollbackUnitOfWork();

        // void the payroll
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setIsVoid(true);
        }
        QBDTTestHelper.submitPayroll(company, payrollRunDTO, false, true, true);

        // verify void was successful
        PayrollServices.beginUnitOfWork();
        payrollRun = assertOne(PayrollRun.findPayrollRuns(company));
        Paycheck paycheck = assertOne(payrollRun.getPaycheckCollection());
        assertTrue("Paycheck not voided", paycheck.isVoidedOrRecalled());
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testVoidPayrollWithOnlyAFeeDebitAndADirectDebit() {
        DataLoadServices.setPSPDate(2012, 10, 1);

        String psid = "123456789";
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.activateTaxServiceExceptBalanceFile(company);
        OFX ofx = OFXRequestGenerator.generateBalanceFile(psid, false, false, false, false);
        QBDTTestHelper.submitQBDTRequest(ofx);

        company = DataLoadServices.refreshCompany(company);

        List<Employee> employees = DataLoadServices.addEEs(company, 1, false, true);

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        HashMap<String, String> lawMap = new HashMap<String, String>();
        lawMap.put("1", "100001.00");
        payrollRunDTO = DataLoadServices.createPayrollRun(payrollRunDTO, company, new DateDTO(2012, 10, 5), employees, lawMap);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.getDdTransactions().clear();
        }
        PayrollServices.rollbackUnitOfWork();

        // submit payroll with a 100k debit and fees
        QBDTTestHelper.submitPayroll(company, payrollRunDTO);

        // there should only be fees created
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRuns(company));
        assertEquals("Paycheck date", SpcfCalendar.createInstance(2012, 10, 5, SpcfTimeZone.getLocalTimeZone()), payrollRun.getPaycheckDate().toLocal());
        assertTrue("Payroll has fts", payrollRun.getFinancialTransactionCollection().size() > 0);
        SpcfCalendar feeOffloadDate = null;
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled))) {
            assertTrue("Tax debit or dd debit found", financialTransaction.getTransactionType().getTransactionTypeCd().notIn(TransactionTypeCode.EmployerTaxDebit,
                                                                                                                       TransactionTypeCode.EmployerDdDebit));
            if(feeOffloadDate == null && financialTransaction.getTransactionType().getTransactionTypeCd().equals(TransactionTypeCode.EmployerFeeDebit)) {
                feeOffloadDate = financialTransaction.getMoneyMovementTransaction().getInitiationDate();
            }
        }
        if(feeOffloadDate == null) {
            fail("Fee offload date not found");
        }
        PayrollServices.rollbackUnitOfWork();

        // offload fee transactions
        DataLoadServices.runOffload(company, feeOffloadDate.toLocal());

        // verify offload and 100k debit
        PayrollServices.beginUnitOfWork();
        payrollRun = assertOne(PayrollRun.findPayrollRuns(company));
        for (FinancialTransaction financialTransaction : payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerFeeDebit))) {
            assertEquals("Transaction not executed", TransactionStateCode.Executed, financialTransaction.getCurrentTransactionState().getTransactionStateCd());
        }
        FinancialTransaction directDebit = assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDirectDebit)));
        assertEquals("Transaction not created", TransactionStateCode.Created, directDebit.getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.rollbackUnitOfWork();

        // void the payroll
        for (PaycheckDTO paycheckDTO : payrollRunDTO.getPaychecks()) {
            paycheckDTO.setIsVoid(true);
        }
        QBDTTestHelper.submitPayroll(company, payrollRunDTO, false, true, true);

        // verify void was successful
        PayrollServices.beginUnitOfWork();
        payrollRun = assertOne(PayrollRun.findPayrollRuns(company));
        Paycheck paycheck = assertOne(payrollRun.getPaycheckCollection());
        assertTrue("Paycheck not voided", paycheck.isVoidedOrRecalled());
        directDebit = assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().equalTo(TransactionTypeCode.EmployerTaxDirectDebit)));
        assertEquals("Transaction not cancelled", TransactionStateCode.Cancelled, directDebit.getCurrentTransactionState().getTransactionStateCd());
        PayrollServices.rollbackUnitOfWork();
    }

}
