package com.intuit.sbd.payroll.psp.adapters.qbdtws.test;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.CommonValidations;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos.*;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.common.dtos.QBProcessingMessage;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.webservices.BillPaymentWebServices;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.BillPaymentDTO;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.domain.SourceSystemCode;
import com.intuit.sbd.payroll.psp.domain.TransmissionType;
import com.intuit.sbd.payroll.psp.domainsecondary.SourceSystemTransmission;
import com.intuit.sbd.payroll.psp.gateways.email.factory.EventEmailFactory;
import com.intuit.sbd.payroll.psp.gateways.email.factory.product.EventEmail;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.GenerateData;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;
import com.intuit.sbd.payroll.psp.util.launchdarkly.FeatureFlags;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Dec 18, 2009
 * Time: 11:13:18 AM
 */
@SuppressWarnings("deprecation")
public class BillPaymentWebServicesTests {


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testSubmitNullArguments() {
        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(null);

        assertEquals("Error Messages", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 2, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());
    }

    @Test
    public void testSubmitUnexpectedError() {
        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        // company is not set so a NPE should be thrown, this will never happen through the WS because it would fail schema validation
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(new SubmitPaymentRequest());

        assertEquals("Error Messages", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 1, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());
    }

    @Test
    public void testSubmitCommonValidations() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1");
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("15.01.R.10/14586#pro");
        submitPaymentRequest.setCompany(qbCompany);
        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);
        assertEquals("Error Messages", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 7, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        billPaymentWebServices = new BillPaymentWebServices();
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("Error Messages", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 292, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

    }

    @Test
    public void testPINLockOut() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1");
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("15.01.R.10/14586#pro");
        submitPaymentRequest.setCompany(qbCompany);
        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);
        assertEquals("Error Messages", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 7, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        billPaymentWebServices = new BillPaymentWebServices();

        // 1
        submitPaymentRequest.setPIN("1");
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);
        assertEquals("Error Messages", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 292, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

        // 2
        submitPaymentRequest.setPIN("2");
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);
        assertEquals("Error Messages", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 292, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

        // 3
        submitPaymentRequest.setPIN("3");
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);
        assertEquals("Error Messages", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 293, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

        // send a valid payment
        submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1234567a");
        submitPaymentRequest.setPSID(sourceCompanyId);

        qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        submitPaymentRequest.setCompany(qbCompany);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId("1245");

        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");

        QBPayee qbPayee = new QBPayee();
        qbPayee.setAddress(qbAddress);
        qbPayee.setEmailAddress("payee@intuit.com");
        qbPayee.setName("Payee Name");
        qbPayee.setPayeeSourceId("123");
        qbPayee.setPhoneNumber("123-123-1234");
        qbPayee.setTaxId("123-12-1234");
        qbPayee.setIs1099(true);
        paymentTransaction.setPayee(qbPayee);

        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId("1234");

        QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(100.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(200.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        billPaymentWebServices = new BillPaymentWebServices();
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("Error Messages", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 293, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

    }

    @Test
    public void testSubmit_EmailParamValueMoreThan4000Chars() {
        if(FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_MTL_EMAIL_VENDOR_ENABLED, false)){
            return;
        }
        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2013, 4, 18);

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1234567a");
        submitPaymentRequest.setPSID("123272727");

        submitPaymentRequest.setCompany(new QBCompany());
        submitPaymentRequest.getCompany().setClientApplicationName("QUICKBOOKS_DIY");
        submitPaymentRequest.getCompany().setClientApplicationVersion("21.00.R.12/21309#pro");
        submitPaymentRequest.getCompany().setCompanyLegalName("Test Account");
        submitPaymentRequest.getCompany().setVendorGatewayVersion("1.0.0.0");

        for (int i = 1; i < 200; i++) {
            PaymentTransaction paymentTransaction = new PaymentTransaction();

            paymentTransaction.setDepositDate(new Date("04/22/2013"));
            paymentTransaction.setTransactionType(TransactionTypeEnum.PayBills);
            paymentTransaction.setTransactionId(UUID.randomUUID().toString());

            paymentTransaction.setPayee(new QBPayee());
            paymentTransaction.getPayee().setIs1099(true);
            paymentTransaction.getPayee().setName("Employee " + i);
            paymentTransaction.getPayee().setPayeeSourceId(UUID.randomUUID().toString());

            QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
            qbBillPaymentSplit.setAmount(new BigDecimal(i));
            qbBillPaymentSplit.setSourceBillPaymentSplitId(UUID.randomUUID().toString());

            qbBillPaymentSplit.setBankAccount(new QBBankAccount());
            qbBillPaymentSplit.getBankAccount().setAccountNumber("12345" + i);
            qbBillPaymentSplit.getBankAccount().setAccountType(QBBankAccountTypeEnum.CHECKING);
            qbBillPaymentSplit.getBankAccount().setBankName("Bank " + i);
            qbBillPaymentSplit.getBankAccount().setRoutingNumber("321178420");
            qbBillPaymentSplit.getBankAccount().setSourceBankAccountId(UUID.randomUUID().toString());

            paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

            submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);
        }

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        //Assert successful
        assertEquals(0, submitPaymentResponse.getProcessingMessagesList().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        CompanyEvent companyEvent = CompanyEvent.findCompanyEvents(company, EventTypeCode.BillPaymentReceived).getFirst();
        CompanyEventEmail companyEventEmail = companyEvent.getCompanyEventEmailCollection().getFirst();
        DomainEntitySet<CompanyEventEmailParam> params = companyEventEmail.getEmailParamForEmailEvent(EventEmailParamTypeCode.VendorPaymentList);
        assertEquals(2, params.size());
        PayrollServices.commitUnitOfWork();

        //DataLoadServices.runEmailGateway();
    }

    @Test
    public void testSubmit_EmailParamValueMoreThan4000CharsMTLVendors() {
        if(!FeatureFlags.get().booleanValue(FeatureFlags.Key.IS_MTL_EMAIL_VENDOR_ENABLED, false)){
            return;
        }
        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        DataLoadServices.setPSPDate(2013, 4, 18);

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1234567a");
        submitPaymentRequest.setPSID("123272727");

        submitPaymentRequest.setCompany(new QBCompany());
        submitPaymentRequest.getCompany().setClientApplicationName("QUICKBOOKS_DIY");
        submitPaymentRequest.getCompany().setClientApplicationVersion("21.00.R.12/21309#pro");
        submitPaymentRequest.getCompany().setCompanyLegalName("Test Account");
        submitPaymentRequest.getCompany().setVendorGatewayVersion("1.0.0.0");

        for (int i = 1; i < 200; i++) {
            PaymentTransaction paymentTransaction = new PaymentTransaction();

            paymentTransaction.setDepositDate(new Date("04/22/2013"));
            paymentTransaction.setTransactionType(TransactionTypeEnum.PayBills);
            paymentTransaction.setTransactionId(UUID.randomUUID().toString());

            paymentTransaction.setPayee(new QBPayee());
            paymentTransaction.getPayee().setIs1099(true);
            paymentTransaction.getPayee().setName("Employee " + i);
            paymentTransaction.getPayee().setPayeeSourceId(UUID.randomUUID().toString());

            QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
            qbBillPaymentSplit.setAmount(new BigDecimal(i));
            qbBillPaymentSplit.setSourceBillPaymentSplitId(UUID.randomUUID().toString());

            qbBillPaymentSplit.setBankAccount(new QBBankAccount());
            qbBillPaymentSplit.getBankAccount().setAccountNumber("12345" + i);
            qbBillPaymentSplit.getBankAccount().setAccountType(QBBankAccountTypeEnum.CHECKING);
            qbBillPaymentSplit.getBankAccount().setBankName("Bank " + i);
            qbBillPaymentSplit.getBankAccount().setRoutingNumber("321178420");
            qbBillPaymentSplit.getBankAccount().setSourceBankAccountId(UUID.randomUUID().toString());

            paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

            submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);
        }

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        //Assert successful
        assertEquals(0, submitPaymentResponse.getProcessingMessagesList().size());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        CompanyEvent companyEvent = CompanyEvent.findCompanyEvents(company, EventTypeCode.BillPaymentReceived).getFirst();
        CompanyEventEmail companyEventEmail = companyEvent.getCompanyEventEmailCollection().getFirst();
        DomainEntitySet<CompanyEventEmailParam> params = companyEventEmail.getEmailParamForEmailEvent(EventEmailParamTypeCode.VendorPaymentList);
        assertEquals(4, params.size());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testSubmit_HappyPath() {

        String sourceCompanyId = "123272727";
        String payeeId = "Id12345";
        String sourceBankAccountId = "Id45628";
        String sourceBillPaymentId1 = "Id123";
        String sourceBillPaymentId2 = "Id124";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();


        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1234567a");
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");

        QBAddress companyAddress = new QBAddress();
        companyAddress.setAddressLine1("6888 Sierra Cnt Pkwy2upd");
        companyAddress.setCity("Reno2");
        companyAddress.setState("NE");
        companyAddress.setZipCode("89512");
        companyAddress.setCountry("US");
        qbCompany.setLegalAddress(companyAddress);

        submitPaymentRequest.setCompany(qbCompany);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId1);

        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");

        QBPayee qbPayee = new QBPayee();
        qbPayee.setAddress(qbAddress);
        qbPayee.setEmailAddress("payee@intuit.com");
        qbPayee.setName("Payee Name");
        qbPayee.setPayeeSourceId(payeeId);
        qbPayee.setPhoneNumber("123-123-1234");
        qbPayee.setTaxId("123-12-1234");
        qbPayee.setIs1099(true);
        paymentTransaction.setPayee(qbPayee);

        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId(sourceBankAccountId);

        QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(100.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(200.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/22/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId2);
        paymentTransaction.setPayee(qbPayee);
        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(500.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);
        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 0, submitPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 2, submitPaymentResponse.getFeeTransactions().size());
        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), submitPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 1, submitPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/19/2007"), submitPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), submitPaymentResponse.getFeeTransactions().get(1).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(1).getFeeType());
        assertEquals("number of transactions", 1, submitPaymentResponse.getFeeTransactions().get(1).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/21/2007"), submitPaymentResponse.getFeeTransactions().get(1).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(1).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(1).getTransactionId());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.LegalAddressChanged, CompanyEventStatus.Active, null, null);
        assertEquals("legal name changed", 0, companyEvents.size());

        Payee payee = Payee.findPayee(company, payeeId);

        // make sure the payee was added correctly
        assertNotNull("payee", payee);
        assertEquals(qbPayee.getEmailAddress(), payee.getEmail());
        assertEquals(qbPayee.getName(), payee.getName());
        assertEquals(qbPayee.getPayeeSourceId(), payee.getSourcePayeeId());
        assertEquals(qbPayee.getPhoneNumber(), payee.getPhone());
        assertEquals(qbPayee.getTaxId(), payee.getTaxId());

        Address address = payee.getMailingAddress();
        assertNotNull("address", address);
        assertEquals(qbAddress.getAddressLine1(), address.getAddressLine1());
        assertEquals(qbAddress.getAddressLine2(), address.getAddressLine2());
        assertEquals(qbAddress.getAddressLine3(), address.getAddressLine3());
        assertEquals(qbAddress.getCity(), address.getCity());
        assertEquals(qbAddress.getCountry(), address.getCountry());
        assertEquals(qbAddress.getState(), address.getState());
        assertEquals(qbAddress.getZipCode(), address.getZipCode());
        assertEquals(qbAddress.getZipCodeExtension(), address.getZipCodeExtension());

        PayeeBankAccount payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, sourceBankAccountId);
        assertNotNull("payeeBankAccount", payeeBankAccount);
        BankAccount bankAccount = payeeBankAccount.getBankAccount();
        assertNotNull("bankAccount", bankAccount);
        assertEquals(qbBankAccount.getAccountNumber(), bankAccount.getAccountNumber());
        assertEquals(qbBankAccount.getAccountType().value(), bankAccount.getAccountTypeCd().toString());
        assertEquals(qbBankAccount.getBankName(), bankAccount.getBankName());
        assertEquals(qbBankAccount.getRoutingNumber(), bankAccount.getRoutingNumber());
        assertEquals(qbBankAccount.getSourceBankAccountId(), payeeBankAccount.getSourceBankAccountId());

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId1);
        assertNotNull("billPayment", billPayment);
        assertEquals(2, billPayment.getBillPaymentSplitCollection().size());
        assertEquals(new SpcfMoney("300.00"), billPayment.getAmount());
        assertEquals(sourceBillPaymentId1, billPayment.getSourceId());
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
        assertEquals(2, financialTransactions.size());

        assertEquals(9, financialTransactions.get(0).getSettlementDate().getMonth());
        assertEquals(20, financialTransactions.get(0).getSettlementDate().getDay());

        billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId2);
        assertNotNull("billPayment", billPayment);
        assertEquals(1, billPayment.getBillPaymentSplitCollection().size());
        assertEquals(new SpcfMoney("500.00"), billPayment.getAmount());
        assertEquals(sourceBillPaymentId2, billPayment.getSourceId());
        financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
        assertEquals(1, financialTransactions.size());

        PayrollServices.rollbackUnitOfWork();
    }


    //TODO : This test has been failing for a long time in unit tests job. We should analyze more.
    @Test
    @Ignore
    public void testSubmit_TaxService_HappyPath() {

        String sourceCompanyId = "123272727";
        String payeeId = "Id12345";
        String sourceBankAccountId = "Id45628";
        String sourceBillPaymentId1 = "Id123";
        String sourceBillPaymentId2 = "Id124";

        // company setup
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.loadDataForBillPaymentSubmit_SetupCompany();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();
        DataLoadServices.activateDDService(company);
        DataLoader dataLoader = new DataLoader();
        dataLoader.persistTestCompanyTaxService(company);
        PayrollServices.beginUnitOfWork();
        Service service = Application.findById(Service.class, ServiceCode.DirectDeposit);
        CompanyService companyService = CompanyService.findCompanyService(company, service.getServiceCd());
        companyService.setStatusCd(ServiceSubStatusCode.ActiveCurrent);
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        dataLoader.persistBillPaymentCompanyService(company);
        CompanyBankAccount companyBankAccount = dataLoader.persistCompanyBankAccount(company, dataLoader.getTestCompanyBankAccount());

        //Create company PIN
        psdl.persistCompanyPIN(company.getSourceCompanyId());

        // Create Payees

        GenerateData.generatePayees(company, 5);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
         company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1234567a");
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");

        QBAddress companyAddress = new QBAddress();
        companyAddress.setAddressLine1("6888 Sierra Cnt Pkwy2upd");
        companyAddress.setCity("Reno2");
        companyAddress.setState("NE");
        companyAddress.setZipCode("89512");
        companyAddress.setCountry("US");
        qbCompany.setLegalAddress(companyAddress);

        submitPaymentRequest.setCompany(qbCompany);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId1);

        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");

        QBPayee qbPayee = new QBPayee();
        qbPayee.setAddress(qbAddress);
        qbPayee.setEmailAddress("payee@intuit.com");
        qbPayee.setName("Payee Name");
        qbPayee.setPayeeSourceId(payeeId);
        qbPayee.setPhoneNumber("123-123-1234");
        qbPayee.setTaxId("123-12-1234");
        qbPayee.setIs1099(true);
        paymentTransaction.setPayee(qbPayee);

        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId(sourceBankAccountId);

        QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(100.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(200.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/22/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId2);
        paymentTransaction.setPayee(qbPayee);
        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(500.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);
        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 0, submitPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 2, submitPaymentResponse.getFeeTransactions().size());
        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), submitPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 1, submitPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/19/2007"), submitPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), submitPaymentResponse.getFeeTransactions().get(1).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(1).getFeeType());
        assertEquals("number of transactions", 1, submitPaymentResponse.getFeeTransactions().get(1).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/21/2007"), submitPaymentResponse.getFeeTransactions().get(1).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(1).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(1).getTransactionId());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.LegalAddressChanged, CompanyEventStatus.Active, null, null);
        assertEquals("legal name changed", 0, companyEvents.size());

        Payee payee = Payee.findPayee(company, payeeId);

        // make sure the payee was added correctly
        assertNotNull("payee", payee);
        assertEquals(qbPayee.getEmailAddress(), payee.getEmail());
        assertEquals(qbPayee.getName(), payee.getName());
        assertEquals(qbPayee.getPayeeSourceId(), payee.getSourcePayeeId());
        assertEquals(qbPayee.getPhoneNumber(), payee.getPhone());
        assertEquals(qbPayee.getTaxId(), payee.getTaxId());

        Address address = payee.getMailingAddress();
        assertNotNull("address", address);
        assertEquals(qbAddress.getAddressLine1(), address.getAddressLine1());
        assertEquals(qbAddress.getAddressLine2(), address.getAddressLine2());
        assertEquals(qbAddress.getAddressLine3(), address.getAddressLine3());
        assertEquals(qbAddress.getCity(), address.getCity());
        assertEquals(qbAddress.getCountry(), address.getCountry());
        assertEquals(qbAddress.getState(), address.getState());
        assertEquals(qbAddress.getZipCode(), address.getZipCode());
        assertEquals(qbAddress.getZipCodeExtension(), address.getZipCodeExtension());

        PayeeBankAccount payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, sourceBankAccountId);
        assertNotNull("payeeBankAccount", payeeBankAccount);
        BankAccount bankAccount = payeeBankAccount.getBankAccount();
        assertNotNull("bankAccount", bankAccount);
        assertEquals(qbBankAccount.getAccountNumber(), bankAccount.getAccountNumber());
        assertEquals(qbBankAccount.getAccountType().value(), bankAccount.getAccountTypeCd().toString());
        assertEquals(qbBankAccount.getBankName(), bankAccount.getBankName());
        assertEquals(qbBankAccount.getRoutingNumber(), bankAccount.getRoutingNumber());
        assertEquals(qbBankAccount.getSourceBankAccountId(), payeeBankAccount.getSourceBankAccountId());

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId1);
        assertNotNull("billPayment", billPayment);
        assertEquals(2, billPayment.getBillPaymentSplitCollection().size());
        assertEquals(new SpcfMoney("300.00"), billPayment.getAmount());
        assertEquals(sourceBillPaymentId1, billPayment.getSourceId());
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
        assertEquals(2, financialTransactions.size());

        assertEquals(9, financialTransactions.get(0).getSettlementDate().getMonth());
        assertEquals(20, financialTransactions.get(0).getSettlementDate().getDay());

        billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId2);
        assertNotNull("billPayment", billPayment);
        assertEquals(1, billPayment.getBillPaymentSplitCollection().size());
        assertEquals(new SpcfMoney("500.00"), billPayment.getAmount());
        assertEquals(sourceBillPaymentId2, billPayment.getSourceId());
        financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
        assertEquals(1, financialTransactions.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmit_HappyPath2() {

        String sourceCompanyId = "123272727";
        String payeeId = "Id12345";
        String sourceBankAccountId = "Id45628";
        String sourceBillPaymentId1 = "Id123";
        String sourceBillPaymentId2 = "Id124";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();


        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1234567a");
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        submitPaymentRequest.setCompany(qbCompany);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId1);

        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");

        QBPayee qbPayee = new QBPayee();
        qbPayee.setAddress(qbAddress);
        qbPayee.setEmailAddress("payee@intuit.com");
        qbPayee.setName("Payee Name");
        qbPayee.setPayeeSourceId(payeeId);
        qbPayee.setPhoneNumber("123-123-1234");
        qbPayee.setTaxId("123-12-1234");
        qbPayee.setIs1099(true);
        paymentTransaction.setPayee(qbPayee);

        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId(sourceBankAccountId);

        QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(100.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(200.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId2);
        paymentTransaction.setPayee(qbPayee);
        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(500.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);
        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 0, submitPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 1, submitPaymentResponse.getFeeTransactions().size());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 2, submitPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/19/2007"), submitPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        SpcfDecimal perPayrollServiceCharge = ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().multiply(SpcfDecimal.createInstance(submitPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions()));
        assertEquals("fee amount", perPayrollServiceCharge.toString(), submitPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);
        Payee payee = Payee.findPayee(company, payeeId);

        // make sure the payee was added correctly
        assertNotNull("payee", payee);
        assertEquals(qbPayee.getEmailAddress(), payee.getEmail());
        assertEquals(qbPayee.getName(), payee.getName());
        assertEquals(qbPayee.getPayeeSourceId(), payee.getSourcePayeeId());
        assertEquals(qbPayee.getPhoneNumber(), payee.getPhone());
        assertEquals(qbPayee.getTaxId(), payee.getTaxId());

        Address address = payee.getMailingAddress();
        assertNotNull("address", address);
        assertEquals(qbAddress.getAddressLine1(), address.getAddressLine1());
        assertEquals(qbAddress.getAddressLine2(), address.getAddressLine2());
        assertEquals(qbAddress.getAddressLine3(), address.getAddressLine3());
        assertEquals(qbAddress.getCity(), address.getCity());
        assertEquals(qbAddress.getCountry(), address.getCountry());
        assertEquals(qbAddress.getState(), address.getState());
        assertEquals(qbAddress.getZipCode(), address.getZipCode());
        assertEquals(qbAddress.getZipCodeExtension(), address.getZipCodeExtension());

        PayeeBankAccount payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, sourceBankAccountId);
        assertNotNull("payeeBankAccount", payeeBankAccount);
        BankAccount bankAccount = payeeBankAccount.getBankAccount();
        assertNotNull("bankAccount", bankAccount);
        assertEquals(qbBankAccount.getAccountNumber(), bankAccount.getAccountNumber());
        assertEquals(qbBankAccount.getAccountType().value(), bankAccount.getAccountTypeCd().toString());
        assertEquals(qbBankAccount.getBankName(), bankAccount.getBankName());
        assertEquals(qbBankAccount.getRoutingNumber(), bankAccount.getRoutingNumber());
        assertEquals(qbBankAccount.getSourceBankAccountId(), payeeBankAccount.getSourceBankAccountId());

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId1);
        assertNotNull("billPayment", billPayment);
        assertEquals(2, billPayment.getBillPaymentSplitCollection().size());
        assertEquals(new SpcfMoney("300.00"), billPayment.getAmount());
        assertEquals(sourceBillPaymentId1, billPayment.getSourceId());

        //total Employee DD Credit for the payroll run
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
        assertEquals(3, financialTransactions.size());

        assertEquals(9, financialTransactions.get(0).getSettlementDate().getMonth());
        assertEquals(20, financialTransactions.get(0).getSettlementDate().getDay());

        billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId2);
        assertNotNull("billPayment", billPayment);
        assertEquals(1, billPayment.getBillPaymentSplitCollection().size());
        assertEquals(new SpcfMoney("500.00"), billPayment.getAmount());
        assertEquals(sourceBillPaymentId2, billPayment.getSourceId());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmit_HappyPath_PayBills() {

        String sourceCompanyId = "123272727";
        String payeeId = "Id12345";
        String sourceBankAccountId = "Id45628";
        String sourceBillPaymentId1 = "Id123";
        String sourceBillPaymentId2 = "Id124";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();


        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1234567a");
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        submitPaymentRequest.setCompany(qbCompany);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId1);
        paymentTransaction.setMemo("Vendor Payment Memo " + sourceBillPaymentId1);
        paymentTransaction.setTransactionType(TransactionTypeEnum.PayBills);

        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");

        QBPayee qbPayee = new QBPayee();
        qbPayee.setAddress(qbAddress);
        qbPayee.setEmailAddress("payee@intuit.com");
        qbPayee.setName("Payee Name");
        qbPayee.setPayeeSourceId(payeeId);
        qbPayee.setPhoneNumber("123-123-1234");
        qbPayee.setTaxId("123-12-1234");
        qbPayee.setAccountNumber("vendor 123-12-1234");
        qbPayee.setIs1099(true);
        paymentTransaction.setPayee(qbPayee);

        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId(sourceBankAccountId);

        QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(100.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        qbBillPaymentSplit.setReferenceNumber("Ref# " + sourceBillPaymentId1);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(200.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        qbBillPaymentSplit.setReferenceNumber("Ref# " + sourceBillPaymentId1);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId2);
        paymentTransaction.setPayee(qbPayee);
        paymentTransaction.setMemo("Vendor Payment Memo " + sourceBillPaymentId2);
        paymentTransaction.setTransactionType(TransactionTypeEnum.PayBills);

        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(500.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        qbBillPaymentSplit.setReferenceNumber("Ref# " + sourceBillPaymentId2);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);
        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 0, submitPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 1, submitPaymentResponse.getFeeTransactions().size());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 2, submitPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/19/2007"), submitPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        SpcfDecimal perPayrollServiceCharge = ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().multiply(SpcfDecimal.createInstance(submitPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions()));
        assertEquals("fee amount", perPayrollServiceCharge.toString(), submitPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);
        Payee payee = Payee.findPayee(company, payeeId);

        // make sure the payee was added correctly
        assertNotNull("payee", payee);
        assertEquals(qbPayee.getEmailAddress(), payee.getEmail());
        assertEquals(qbPayee.getName(), payee.getName());
        assertEquals(qbPayee.getPayeeSourceId(), payee.getSourcePayeeId());
        assertEquals(qbPayee.getPhoneNumber(), payee.getPhone());
        assertEquals(qbPayee.getTaxId(), payee.getTaxId());
        assertEquals(qbPayee.getAccountNumber(), payee.getAccountNumber());

        Address address = payee.getMailingAddress();
        assertNotNull("address", address);
        assertEquals(qbAddress.getAddressLine1(), address.getAddressLine1());
        assertEquals(qbAddress.getAddressLine2(), address.getAddressLine2());
        assertEquals(qbAddress.getAddressLine3(), address.getAddressLine3());
        assertEquals(qbAddress.getCity(), address.getCity());
        assertEquals(qbAddress.getCountry(), address.getCountry());
        assertEquals(qbAddress.getState(), address.getState());
        assertEquals(qbAddress.getZipCode(), address.getZipCode());
        assertEquals(qbAddress.getZipCodeExtension(), address.getZipCodeExtension());

        PayeeBankAccount payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, sourceBankAccountId);
        assertNotNull("payeeBankAccount", payeeBankAccount);
        BankAccount bankAccount = payeeBankAccount.getBankAccount();
        assertNotNull("bankAccount", bankAccount);
        assertEquals(qbBankAccount.getAccountNumber(), bankAccount.getAccountNumber());
        assertEquals(qbBankAccount.getAccountType().value(), bankAccount.getAccountTypeCd().toString());
        assertEquals(qbBankAccount.getBankName(), bankAccount.getBankName());
        assertEquals(qbBankAccount.getRoutingNumber(), bankAccount.getRoutingNumber());
        assertEquals(qbBankAccount.getSourceBankAccountId(), payeeBankAccount.getSourceBankAccountId());

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId1);
        assertNotNull("billPayment", billPayment);
        assertEquals(2, billPayment.getBillPaymentSplitCollection().size());
        assertEquals(new SpcfMoney("300.00"), billPayment.getAmount());
        assertEquals(sourceBillPaymentId1, billPayment.getSourceId());
        assertEquals(billPayment.getMemo(), "Vendor Payment Memo Id123");
        assertEquals(billPayment.getBillPaymentSplitCollection().get(0).getReferenceNumber(), "Ref# Id123");
        assertEquals(billPayment.getBillPaymentSplitCollection().get(1).getReferenceNumber(), "Ref# Id123");
        assertEquals(billPayment.getTransactionType().toString(), TransactionTypeEnum.PayBills.toString());

        //total Employee DD Credit for the payroll run
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
        assertEquals(3, financialTransactions.size());

        assertEquals(9, financialTransactions.get(0).getSettlementDate().getMonth());
        assertEquals(20, financialTransactions.get(0).getSettlementDate().getDay());

        billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId2);
        assertNotNull("billPayment", billPayment);
        assertEquals(1, billPayment.getBillPaymentSplitCollection().size());
        assertEquals(new SpcfMoney("500.00"), billPayment.getAmount());
        assertEquals(sourceBillPaymentId2, billPayment.getSourceId());
        assertEquals(billPayment.getMemo(), "Vendor Payment Memo Id124");
        assertEquals(billPayment.getBillPaymentSplitCollection().get(0).getReferenceNumber(), "Ref# Id124");
        assertEquals(billPayment.getTransactionType().toString(), TransactionTypeEnum.PayBills.toString());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmit_WithAddressChange() {

        String sourceCompanyId = "123272727";
        String payeeId = "Id12345";
        String sourceBankAccountId = "Id45628";
        String sourceBillPaymentId1 = "Id123";
        String sourceBillPaymentId2 = "Id124";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();


        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1234567a");
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");

        QBAddress companyAddress = new QBAddress();
        companyAddress.setAddressLine1("6888 Sierra Cnt Pkwy2upd");
        companyAddress.setAddressLine2("Apt 5");
        companyAddress.setCity("Reno2");
        companyAddress.setState("NE");
        companyAddress.setZipCode("89512");
        companyAddress.setCountry("US");
        qbCompany.setLegalAddress(companyAddress);

        submitPaymentRequest.setCompany(qbCompany);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId1);

        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");

        QBPayee qbPayee = new QBPayee();
        qbPayee.setAddress(qbAddress);
        qbPayee.setEmailAddress("payee@intuit.com");
        qbPayee.setName("Payee Name");
        qbPayee.setPayeeSourceId(payeeId);
        qbPayee.setPhoneNumber("123-123-1234");
        qbPayee.setTaxId("123-12-1234");
        qbPayee.setIs1099(true);
        paymentTransaction.setPayee(qbPayee);

        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId(sourceBankAccountId);

        QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(100.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(200.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/22/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId2);
        paymentTransaction.setPayee(qbPayee);
        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(500.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);
        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 0, submitPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 2, submitPaymentResponse.getFeeTransactions().size());
        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), submitPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 1, submitPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/19/2007"), submitPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), submitPaymentResponse.getFeeTransactions().get(1).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(1).getFeeType());
        assertEquals("number of transactions", 1, submitPaymentResponse.getFeeTransactions().get(1).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/21/2007"), submitPaymentResponse.getFeeTransactions().get(1).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(1).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(1).getTransactionId());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        DomainEntitySet<CompanyEvent> companyEvents = CompanyEvent.findCompanyEvents(company, EventTypeCode.LegalAddressChanged, CompanyEventStatus.Active, null, null);
        assertEquals("legal name changed", 1, companyEvents.size());

        Payee payee = Payee.findPayee(company, payeeId);

        // make sure the payee was added correctly
        assertNotNull("payee", payee);
        assertEquals(qbPayee.getEmailAddress(), payee.getEmail());
        assertEquals(qbPayee.getName(), payee.getName());
        assertEquals(qbPayee.getPayeeSourceId(), payee.getSourcePayeeId());
        assertEquals(qbPayee.getPhoneNumber(), payee.getPhone());
        assertEquals(qbPayee.getTaxId(), payee.getTaxId());

        Address address = payee.getMailingAddress();
        assertNotNull("address", address);
        assertEquals(qbAddress.getAddressLine1(), address.getAddressLine1());
        assertEquals(qbAddress.getAddressLine2(), address.getAddressLine2());
        assertEquals(qbAddress.getAddressLine3(), address.getAddressLine3());
        assertEquals(qbAddress.getCity(), address.getCity());
        assertEquals(qbAddress.getCountry(), address.getCountry());
        assertEquals(qbAddress.getState(), address.getState());
        assertEquals(qbAddress.getZipCode(), address.getZipCode());
        assertEquals(qbAddress.getZipCodeExtension(), address.getZipCodeExtension());

        PayeeBankAccount payeeBankAccount = PayeeBankAccount.findPayeeBankAccount(payee, sourceBankAccountId);
        assertNotNull("payeeBankAccount", payeeBankAccount);
        BankAccount bankAccount = payeeBankAccount.getBankAccount();
        assertNotNull("bankAccount", bankAccount);
        assertEquals(qbBankAccount.getAccountNumber(), bankAccount.getAccountNumber());
        assertEquals(qbBankAccount.getAccountType().value(), bankAccount.getAccountTypeCd().toString());
        assertEquals(qbBankAccount.getBankName(), bankAccount.getBankName());
        assertEquals(qbBankAccount.getRoutingNumber(), bankAccount.getRoutingNumber());
        assertEquals(qbBankAccount.getSourceBankAccountId(), payeeBankAccount.getSourceBankAccountId());

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId1);
        assertNotNull("billPayment", billPayment);
        assertEquals(2, billPayment.getBillPaymentSplitCollection().size());
        assertEquals(new SpcfMoney("300.00"), billPayment.getAmount());
        assertEquals(sourceBillPaymentId1, billPayment.getSourceId());
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
        assertEquals(2, financialTransactions.size());

        assertEquals(9, financialTransactions.get(0).getSettlementDate().getMonth());
        assertEquals(20, financialTransactions.get(0).getSettlementDate().getDay());

        billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId2);
        assertNotNull("billPayment", billPayment);
        assertEquals(1, billPayment.getBillPaymentSplitCollection().size());
        assertEquals(new SpcfMoney("500.00"), billPayment.getAmount());
        assertEquals(sourceBillPaymentId2, billPayment.getSourceId());
        financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Cancelled);
        assertEquals(1, financialTransactions.size());

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testSubmit_DuplicatePayment() {

        String sourceCompanyId = "123272727";
        String payeeId = "Id12345";
        String sourceBankAccountId = "Id45628";
        String sourceBillPaymentId1 = "Id123";
        String sourceBillPaymentId2 = "Id124";
        String pin = "1234567a";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();


        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN(pin);
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        submitPaymentRequest.setCompany(qbCompany);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId1);

        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");

        QBPayee qbPayee = new QBPayee();
        qbPayee.setAddress(qbAddress);
        qbPayee.setEmailAddress("payee@intuit.com");
        qbPayee.setName("Payee Name");
        qbPayee.setPayeeSourceId(payeeId);
        qbPayee.setPhoneNumber("123-123-1234");
        qbPayee.setTaxId("123-12-1234");
        qbPayee.setIs1099(true);
        paymentTransaction.setPayee(qbPayee);

        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId(sourceBankAccountId);

        QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(100.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(200.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/22/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId2);
        paymentTransaction.setPayee(qbPayee);
        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(500.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);
        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 0, submitPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 2, submitPaymentResponse.getFeeTransactions().size());
        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), submitPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 1, submitPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/19/2007"), submitPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), submitPaymentResponse.getFeeTransactions().get(1).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(1).getFeeType());
        assertEquals("number of transactions", 1, submitPaymentResponse.getFeeTransactions().get(1).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/21/2007"), submitPaymentResponse.getFeeTransactions().get(1).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(1).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(1).getTransactionId());


        // submit the exact same payment again
        submitPaymentRequest.setPIN(pin);
        billPaymentWebServices = new BillPaymentWebServices();
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 0, submitPaymentResponse.getProcessingMessagesList().size());

        // the response should be the same
        assertEquals("fee transactions", 2, submitPaymentResponse.getFeeTransactions().size());
        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), submitPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 1, submitPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/19/2007"), submitPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), submitPaymentResponse.getFeeTransactions().get(1).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, submitPaymentResponse.getFeeTransactions().get(1).getFeeType());
        assertEquals("number of transactions", 1, submitPaymentResponse.getFeeTransactions().get(1).getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/21/2007"), submitPaymentResponse.getFeeTransactions().get(1).getSettlementDate());
        assertEquals("tax amount", "0.08", submitPaymentResponse.getFeeTransactions().get(1).getTaxAmount().toString());
        assertNotNull("id", submitPaymentResponse.getFeeTransactions().get(1).getTransactionId());

        // submit the same payment again with a different amount
        submitPaymentRequest.setPIN(pin);
        billPaymentWebServices = new BillPaymentWebServices();
        submitPaymentRequest.getPaymentTransactions().get(0).getBillPaymentSplits().get(0).setAmount(new BigDecimal(50.00));
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 11, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

        // submit the same payment again with a different account number
        submitPaymentRequest.setPIN(pin);
        billPaymentWebServices = new BillPaymentWebServices();
        submitPaymentRequest.getPaymentTransactions().get(0).getBillPaymentSplits().get(0).setAmount(new BigDecimal(100.00));
        submitPaymentRequest.getPaymentTransactions().get(0).getBillPaymentSplits().get(0).getBankAccount().setAccountNumber("01234567");
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 2, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 11, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

        // submit the same payment again with a different routing number
        submitPaymentRequest.setPIN(pin);
        billPaymentWebServices = new BillPaymentWebServices();
        submitPaymentRequest.getPaymentTransactions().get(0).getBillPaymentSplits().get(0).getBankAccount().setAccountNumber("1234567");
        submitPaymentRequest.getPaymentTransactions().get(0).getBillPaymentSplits().get(0).getBankAccount().setAccountNumber("111111119");
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 2, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 11, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());
    }


    @Test
    public void testSubmit_BPForPayBills_InvalidReferenceNumber() {

        String sourceCompanyId = "123272727";
        String sourceBankAccountId = "Id45628";
        String sourceBillPaymentId = "Id123";
        String pin = "1234567a";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        Company company = psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN(pin);
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        submitPaymentRequest.setCompany(qbCompany);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId);

        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");

        QBPayee qbPayee = new QBPayee();
        qbPayee.setAddress(qbAddress);
        qbPayee.setEmailAddress("payee@intuit.com");
        qbPayee.setName("Payee Name");
        qbPayee.setPayeeSourceId("123456789012345678901234567890123456789012345678901");
        qbPayee.setPhoneNumber("123-123-1234");
        qbPayee.setTaxId("123-12-1234");
        qbPayee.setIs1099(true);
        paymentTransaction.setPayee(qbPayee);

        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId(sourceBankAccountId);

        QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(100.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        qbBillPaymentSplit.setReferenceNumber("23523562356666666666666666666666666666666666444444444444433333333333");
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(200.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        qbBillPaymentSplit.setReferenceNumber("23523562356666666666666666666666666666666666444444444444433333333333");
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 5001, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.QBDT, SourceSystemCode.PSP,
                TransmissionType.WSBillPaySendPaymentsToPayees), "test_billPaymentWeb_InvalidReferenceNumber.xml",
                Arrays.asList("TransactionId", "SettlementDate"));
    }

    @Test
    public void testSubmit_CoreErrors() {
        int transmissionCount = 0;

        String sourceCompanyId = "123272727";
        String payeeId = "Id12345";
        String sourceBankAccountId = "Id45628";
        String sourceBillPaymentId = "Id123";
        String pin = "1234567a";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN(pin);
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        submitPaymentRequest.setCompany(qbCompany);

        PaymentTransaction paymentTransaction = new PaymentTransaction();
        paymentTransaction.setDepositDate(new Date("09/20/2007"));
        paymentTransaction.setTransactionId(sourceBillPaymentId);

        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");

        QBPayee qbPayee = new QBPayee();
        qbPayee.setAddress(qbAddress);
        qbPayee.setEmailAddress("payee@intuit.com");
        qbPayee.setName("Payee Name");
        qbPayee.setPayeeSourceId("123456789012345678901234567890123456789012345678901");
        qbPayee.setPhoneNumber("123-123-1234");
        qbPayee.setTaxId("123-12-1234");
        qbPayee.setIs1099(true);
        paymentTransaction.setPayee(qbPayee);

        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId(sourceBankAccountId);

        QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(100.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        qbBillPaymentSplit = new QBBillPaymentSplit();
        qbBillPaymentSplit.setAmount(new BigDecimal(200.00));
        qbBillPaymentSplit.setBankAccount(qbBankAccount);
        paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

        submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);

        transmissionCount++;
        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 5001, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

        qbPayee.setPayeeSourceId(payeeId);
        paymentTransaction.setDepositDate(new Date("09/20/2008"));

        transmissionCount++;
        submitPaymentRequest.setPIN(pin);
        billPaymentWebServices = new BillPaymentWebServices();
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 606, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());

        qbPayee.setPayeeSourceId(payeeId);
        paymentTransaction.setDepositDate(new Date("09/20/1599"));

        transmissionCount++;
        submitPaymentRequest.setPIN(pin);
        billPaymentWebServices = new BillPaymentWebServices();
        submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 1, submitPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 9, submitPaymentResponse.getProcessingMessagesList().get(0).getCode());
        PayrollServices.beginUnitOfWorkWithSecondary();
            DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = SourceSystemTransmission.findCompanyTransmissions(sourceCompanyId, SourceSystemCode.QBDT);
            assertEquals("source system transmissions", transmissionCount, sourceSystemTransmissions.size());
            for (SourceSystemTransmission sourceSystemTransmission : sourceSystemTransmissions) {
                assertNotNull(sourceSystemTransmission.getRequestDocument());
                assertNotNull(sourceSystemTransmission.getResponseDocument());
            }
        PayrollServices.rollbackUnitOfWorkWithSecondary();

    }

    @Test
    public void testVoidNullArguments() {
        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(null);

        assertEquals("Error Messages", 1, voidPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 2, voidPaymentResponse.getProcessingMessagesList().get(0).getCode());
    }

    @Test
    public void testVoidUnexpectedError() {
        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        // company is not set so a NPE should be thrown, this will never happen through the WS because it would fail schema validation
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(new VoidPaymentRequest());

        assertEquals("Error Messages", 1, voidPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 1, voidPaymentResponse.getProcessingMessagesList().get(0).getCode());
    }

    @Test
    public void testVoidCommonValidations() {
        String sourceCompanyId = "123272727";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();

        VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
        voidPaymentRequest.setPIN("1");
        voidPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("15.01.R.10/14586#pro");
        voidPaymentRequest.setCompany(qbCompany);
        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);
        assertEquals("Error Messages", 1, voidPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 7, voidPaymentResponse.getProcessingMessagesList().get(0).getCode());

        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        billPaymentWebServices = new BillPaymentWebServices();
        voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);

        assertEquals("Error Messages", 1, voidPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 292, voidPaymentResponse.getProcessingMessagesList().get(0).getCode());
    }

    @Test
    public void testBillPaymentVoid_HappyPath() {
        testSubmit_HappyPath();

        String sourceBillPaymentId = "Id123";
        String sourceCompanyId = "123272727";

        VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
        voidPaymentRequest.setPIN("1234567a");
        voidPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        voidPaymentRequest.setCompany(qbCompany);

        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);

        assertEquals("void payment errors", 0, voidPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 1, voidPaymentResponse.getFeeTransactions().size());
        assertEquals("fee amount", "0.00", voidPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, voidPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 0, voidPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", null, voidPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.00", voidPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", voidPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId);
        assertNotNull("billPayment", billPayment);
        assertEquals(billPayment.getStatus(), BillPaymentStatusCode.Inactive);
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);
        assertEquals(2, financialTransactions.size());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals(TransactionStateCode.Cancelled, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        PayrollServices.rollbackUnitOfWork();
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.QBDT, SourceSystemCode.PSP,
                TransmissionType.WSBillPayVoidPayments), "test_billPaymentWeb_voidHappyPath.xml",
                Arrays.asList("TransactionId"));
    }

    @Test
    public void testBillPaymentVoid_HappyPathForNon1099() {
        // The payee is set as 1099 to allow the payment to pass.
        testSubmit_HappyPath();

        String sourceBillPaymentId = "Id123";
        String sourceCompanyId = "123272727";

        PayrollServices.beginUnitOfWork();
        Payee payee = Payee.findPayees(Company.findCompany(sourceCompanyId,
                                                           SourceSystemCode.QBDT)).get(0);
        payee.setIs1099(false);
        PayrollServices.commitUnitOfWork();

        VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
        voidPaymentRequest.setPIN("1234567a");
        voidPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        voidPaymentRequest.setCompany(qbCompany);

        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);

        assertEquals("void payment errors", 0, voidPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 1, voidPaymentResponse.getFeeTransactions().size());
        assertEquals("fee amount", "0.00", voidPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, voidPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 0, voidPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", null, voidPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.00", voidPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", voidPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId);
        assertNotNull("billPayment", billPayment);
        assertEquals(billPayment.getStatus(), BillPaymentStatusCode.Inactive);
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);
        assertEquals(2, financialTransactions.size());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals(TransactionStateCode.Cancelled, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBillPaymentVoidOneTxn() {
        testSubmit_HappyPath2();

        String sourceBillPaymentId = "Id123";
        String sourceCompanyId = "123272727";

        VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
        voidPaymentRequest.setPIN("1234567a");
        voidPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        voidPaymentRequest.setCompany(qbCompany);

        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);

        assertEquals("void payment errors", 0, voidPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 1, voidPaymentResponse.getFeeTransactions().size());
        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), voidPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, voidPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 1, voidPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
//        assertEquals("settlement date", "Wed Sep 19 00:00:00 PDT 2007", voidPaymentResponse.getFeeTransactions().get(0).getSettlementDate().toString());
//        assertEquals("tax amount", "0.08", voidPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
//        assertNotNull("id", voidPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId);
        assertNotNull("billPayment", billPayment);
        assertEquals(billPayment.getStatus(), BillPaymentStatusCode.Inactive);
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);
        assertEquals(2, financialTransactions.size());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals(TransactionStateCode.Cancelled, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBillPaymentVoid_HappyPath_AlternateOffering() {
        testSubmit_HappyPath();

        String sourceBillPaymentId = "Id123";
        String sourceCompanyId = "123272727";

        VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
        voidPaymentRequest.setPIN("1234567a");
        voidPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        voidPaymentRequest.setCompany(qbCompany);

        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);

        assertEquals("void payment errors", 0, voidPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 1, voidPaymentResponse.getFeeTransactions().size());
        assertEquals("fee amount", "0.00", voidPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, voidPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 0, voidPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", null, voidPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.00", voidPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", voidPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId);
        assertNotNull("billPayment", billPayment);
        assertEquals(billPayment.getStatus(), BillPaymentStatusCode.Inactive);
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);
        assertEquals(2, financialTransactions.size());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals(TransactionStateCode.Cancelled, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBillPaymentVoid_VoidAfterOffload() {
        testSubmit_HappyPath();

        // offload the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070918000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        String sourceBillPaymentId1 = "Id123";
        String sourceBillPaymentId2 = "Id124";
        String sourceCompanyId = "123272727";

        VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
        voidPaymentRequest.setPIN("1234567a");
        voidPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("20.01.R.8/14586#pro");
        voidPaymentRequest.setCompany(qbCompany);

        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId1);
        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId2);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);

        assertEquals("void payment errors", 0, voidPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 2, voidPaymentResponse.getFeeTransactions().size());
        FeeTransaction feeTransaction = voidPaymentResponse.getFeeTransactions().get(0);
        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), feeTransaction.getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, feeTransaction.getFeeType());
        assertEquals("number of transactions", 1, feeTransaction.getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/19/2007"), feeTransaction.getSettlementDate());
        assertEquals("tax amount", "0.08", feeTransaction.getTaxAmount().toString());
        assertTrue("has offloaded", feeTransaction.getHasOffloaded());
        assertEquals("associated transactions", 1, feeTransaction.getAssociatedTransactionIds().size());
        assertNotNull("id", feeTransaction.getTransactionId());

        feeTransaction = voidPaymentResponse.getFeeTransactions().get(1);
        assertEquals("fee amount", "0.00", feeTransaction.getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, feeTransaction.getFeeType());
        assertEquals("number of transactions", 0, feeTransaction.getNumberOfTransactions());
        assertEquals("settlement date", null, feeTransaction.getSettlementDate());
        assertEquals("tax amount", "0.00", feeTransaction.getTaxAmount().toString());
        assertNotNull("id", feeTransaction.getTransactionId());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId2);
        assertNotNull("billPayment", billPayment);
        assertEquals(billPayment.getStatus(), BillPaymentStatusCode.Inactive);
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);
        assertEquals(1, financialTransactions.size());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals(TransactionStateCode.Cancelled, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    private void testBillPaymentVoid_TransactionAlreadyCancelled(String pVersion) {
        String sourceCompanyId = null;
        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, ServiceCode.Cloud, ServiceCode.DirectDeposit);
        sourceCompanyId = company.getSourceCompanyId();
        DataLoadServices.activateCloudService(company);
        DataLoadServices.activateDDService(company);

        DataLoadServices.addBillPaymentService(company);
        DataLoadServices.updateCompanyPIN(company, "1234567a");

        // submit bp payment
        List<Payee> payees = DataLoadServices.addPayees(company, 2);

        PayrollServices.beginUnitOfWork();
        Collection<BillPaymentDTO> billPaymentDTOs = DataLoadServices.createBPPayrollRun(company, payees);
        ProcessResult<Collection<PayrollRun>> submitBPPayroll = PayrollServices.billPaymentManager.submitBillPayment(company.getSourceSystemCd(), company.getSourceCompanyId(), billPaymentDTOs);
        PSP_PRAssert.assertSuccess("submit BP Payroll", submitBPPayroll);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DataLoadServices.assertBillPaymentsEqual(company, billPaymentDTOs);
        PayrollServices.rollbackUnitOfWork();

        String sourceBillPaymentId = null;

        for (PayrollRun payroll : submitBPPayroll.getResult()) {
            PayrollServices.beginUnitOfWork();
            PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payroll.getSourcePayRunId());
            DomainEntitySet<FinancialTransaction> financialTransactions = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit}, new TransactionStateCode[]{TransactionStateCode.Created});
            assertEquals("fin txns", 2, financialTransactions.size());
            ArrayList<String> billPaymentIds = new ArrayList<String>();
            sourceBillPaymentId = financialTransactions.get(0).getBillPaymentSplit().getBillPayment().getSourceId();
            billPaymentIds.add(financialTransactions.get(0).getBillPaymentSplit().getBillPayment().getSourceId());
            ProcessResult cancelBPResult = PayrollServices.billPaymentManager.cancelBillPaymentTransaction(
                    company.getSourceSystemCd(),
                    company.getSourceCompanyId(),
                    billPaymentIds,null);
            PSP_PRAssert.assertSuccess("cancel BP transaction", cancelBPResult);
            PayrollServices.commitUnitOfWork();

            PayrollServices.beginUnitOfWork();
            payrollRun = PayrollRun.findPayrollRun(company, payroll.getSourcePayRunId());
            financialTransactions = payrollRun.getFinancialTransactions(new TransactionTypeCode[]{TransactionTypeCode.EmployeeDdCredit}, new TransactionStateCode[]{TransactionStateCode.Cancelled});
            assertEquals("canceled fin txns", 1, financialTransactions.size());

            PayrollServices.rollbackUnitOfWork();
            break;
        }

        assertNotNull(sourceBillPaymentId);

        VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
        voidPaymentRequest.setPIN("1234567a");
        voidPaymentRequest.setPSID(company.getSourceCompanyId());

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion(pVersion);
        voidPaymentRequest.setCompany(qbCompany);

        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);

        assertEquals("void payment errors", 0, voidPaymentResponse.getProcessingMessagesList().size());

        // check the response
        FeeTransaction feeTransaction = voidPaymentResponse.getFeeTransactions().get(0);
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, feeTransaction.getFeeType());

        OFXAPPVERObject ofxappverObject = CommonValidations.isQBVersionActive(pVersion, voidPaymentResponse);
        boolean isV20R8OrGreater = ofxappverObject.getIntQBVersion() > 20 || (ofxappverObject.getIntQBVersion() == 20 && ofxappverObject.getIntRNumber() >= 8);
        if (isV20R8OrGreater) {
            assertEquals("tax amount", "0.08", feeTransaction.getTaxAmount().toString());
            assertTrue("has offloaded", feeTransaction.getHasOffloaded());
            assertEquals("fee amount", new BigDecimal(1.75), feeTransaction.getFeeAmount());
            assertEquals("number of transactions", 1, feeTransaction.getNumberOfTransactions());
        } else {
            assertEquals("tax amount", "0", feeTransaction.getTaxAmount().toString());
            assertNull("has offloaded is null", feeTransaction.getHasOffloaded());
            assertEquals("fee amount", new BigDecimal(0), feeTransaction.getFeeAmount());
            assertEquals("number of transactions", 0, feeTransaction.getNumberOfTransactions());
        }
        assertNotNull("id", feeTransaction.getTransactionId());

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId);
        assertNotNull("billPayment", billPayment);
        assertEquals(billPayment.getStatus(), BillPaymentStatusCode.Inactive);
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);
        assertEquals(1, financialTransactions.size());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals(TransactionStateCode.Cancelled, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBillPaymentVoid_TransactionAlreadyCancelled_R8() {
        testBillPaymentVoid_TransactionAlreadyCancelled("20.01.R.8/14586#pro");
    }

    @Test
    public void testBillPaymentVoid_TransactionAlreadyCancelled_R6() {
        testBillPaymentVoid_TransactionAlreadyCancelled("20.01.R.6/14586#pro");
    }

    @Test
    public void testBillPaymentVoid_VoidAfterOffloadR6() {
        testSubmit_HappyPath();

        // offload the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070918000000");
        PayrollServices.commitUnitOfWork();
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        String sourceBillPaymentId1 = "Id123";
        String sourceBillPaymentId2 = "Id124";
        String sourceCompanyId = "123272727";

        VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
        voidPaymentRequest.setPIN("1234567a");
        voidPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("20.01.R.6/14586#pro");
        voidPaymentRequest.setCompany(qbCompany);

        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId1);
        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId2);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);

        assertEquals("void payment errors", 0, voidPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 2, voidPaymentResponse.getFeeTransactions().size());
        assertEquals("fee amount", "0.00", voidPaymentResponse.getFeeTransactions().get(0).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, voidPaymentResponse.getFeeTransactions().get(0).getFeeType());
        assertEquals("number of transactions", 0, voidPaymentResponse.getFeeTransactions().get(0).getNumberOfTransactions());
        assertEquals("settlement date", null, voidPaymentResponse.getFeeTransactions().get(0).getSettlementDate());
        assertEquals("tax amount", "0.00", voidPaymentResponse.getFeeTransactions().get(0).getTaxAmount().toString());
        assertNotNull("id", voidPaymentResponse.getFeeTransactions().get(0).getTransactionId());

        assertEquals("fee amount", "0", voidPaymentResponse.getFeeTransactions().get(1).getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.FundsNotRecovered, voidPaymentResponse.getFeeTransactions().get(1).getFeeType());
        assertEquals("number of transactions", 0, voidPaymentResponse.getFeeTransactions().get(1).getNumberOfTransactions());
        assertEquals("settlement date", null, voidPaymentResponse.getFeeTransactions().get(1).getSettlementDate());
        assertEquals("tax amount", "0", voidPaymentResponse.getFeeTransactions().get(1).getTaxAmount().toString());
        assertEquals("id", sourceBillPaymentId1, voidPaymentResponse.getFeeTransactions().get(1).getTransactionId());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId2);
        assertNotNull("billPayment", billPayment);
        assertEquals(billPayment.getStatus(), BillPaymentStatusCode.Inactive);
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);
        assertEquals(1, financialTransactions.size());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals(TransactionStateCode.Cancelled, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testBillPaymentVoid_VoidAfterOffloadCutoff() {
        testSubmit_HappyPath();

        // offload the transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070918172100");
        PayrollServices.commitUnitOfWork();

        String sourceBillPaymentId1 = "Id123";
        String sourceBillPaymentId2 = "Id124";
        String sourceCompanyId = "123272727";

        VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
        voidPaymentRequest.setPIN("1234567a");
        voidPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("20.01.R.8/14586#pro");
        voidPaymentRequest.setCompany(qbCompany);

        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId1);
        voidPaymentRequest.getPaymentGUIDs().add(sourceBillPaymentId2);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);

        assertEquals("void payment errors", 0, voidPaymentResponse.getProcessingMessagesList().size());

        // check the response
        assertEquals("fee transactions", 2, voidPaymentResponse.getFeeTransactions().size());
        FeeTransaction feeTransaction = voidPaymentResponse.getFeeTransactions().get(0);
        assertEquals("fee amount", ServiceChargePrices.getNormalPerPayrollServiceChargeFY16().toString(), feeTransaction.getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, feeTransaction.getFeeType());
        assertEquals("number of transactions", 1, feeTransaction.getNumberOfTransactions());
        assertEquals("settlement date", new Date("09/19/2007"), feeTransaction.getSettlementDate());
        assertEquals("tax amount", "0.08", feeTransaction.getTaxAmount().toString());
        assertTrue("has offloaded", feeTransaction.getHasOffloaded());
        assertEquals("associated transactions", 1, feeTransaction.getAssociatedTransactionIds().size());
        assertNotNull("id", feeTransaction.getTransactionId());

        feeTransaction = voidPaymentResponse.getFeeTransactions().get(1);
        assertEquals("fee amount", "0.00", feeTransaction.getFeeAmount().toString());
        assertEquals("fee type", FeeTypeEnum.PerPaycheck, feeTransaction.getFeeType());
        assertEquals("number of transactions", 0, feeTransaction.getNumberOfTransactions());
        assertEquals("settlement date", null, feeTransaction.getSettlementDate());
        assertEquals("tax amount", "0.00", feeTransaction.getTaxAmount().toString());
        assertNotNull("id", feeTransaction.getTransactionId());

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        assertNotNull("Company", company);

        BillPayment billPayment = BillPayment.findBillPaymentBySourceId(company, sourceBillPaymentId2);
        assertNotNull("billPayment", billPayment);
        assertEquals(billPayment.getStatus(), BillPaymentStatusCode.Inactive);
        DomainEntitySet<FinancialTransaction> financialTransactions = FinancialTransaction.findFinTxnForPayrollByTypeAndExclTxnState(billPayment.getPayrollRun(), TransactionTypeCode.EmployeeDdCredit, TransactionStateCode.Created);
        assertEquals(1, financialTransactions.size());
        for (FinancialTransaction financialTransaction : financialTransactions) {
            assertEquals(TransactionStateCode.Cancelled, financialTransaction.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testVoid_CoreErrors() {
        testSubmit_HappyPath();
        int transmissionCount = 1;

        //String sourceBillPaymentId = "Id123";
        String sourceCompanyId = "123272727";

        VoidPaymentRequest voidPaymentRequest = new VoidPaymentRequest();
        voidPaymentRequest.setPIN("1234567a");
        voidPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        voidPaymentRequest.setCompany(qbCompany);

        voidPaymentRequest.getPaymentGUIDs().add("1234");

        transmissionCount++;
        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        VoidPaymentResponse voidPaymentResponse = billPaymentWebServices.VoidPayments(voidPaymentRequest);

        assertEquals("void payment errors", 1, voidPaymentResponse.getProcessingMessagesList().size());
        assertEquals("Processing Error Code", 8, voidPaymentResponse.getProcessingMessagesList().get(0).getCode());
        PayrollServices.beginUnitOfWorkWithSecondary();
            DomainEntitySet<SourceSystemTransmission> sourceSystemTransmissions = SourceSystemTransmission.findCompanyTransmissions(sourceCompanyId, SourceSystemCode.QBDT);
            assertEquals("source system transmissions", transmissionCount, sourceSystemTransmissions.size());
            for (SourceSystemTransmission sourceSystemTransmission : sourceSystemTransmissions) {
                assertNotNull(sourceSystemTransmission.getRequestDocument());
                assertNotNull(sourceSystemTransmission.getResponseDocument());
            }
        PayrollServices.rollbackUnitOfWorkWithSecondary();
    }

    @Test
    public void testQuery_HappyPath() {
        testSubmit_HappyPath();

        String sourceCompanyId = "123272727";
        String sourceBillPaymentId = "Id123";

        QueryBillPaymentStatusRequest queryBillPaymentStatusRequest = new QueryBillPaymentStatusRequest();
        queryBillPaymentStatusRequest.setPIN("1234567a");
        queryBillPaymentStatusRequest.setPSID(sourceCompanyId);

        queryBillPaymentStatusRequest.getBillPaymentIds().add(sourceBillPaymentId);

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        QueryBillPaymentStatusResponse queryBillPaymentStatusResponse = billPaymentWebServices.QueryPaymentStatus(queryBillPaymentStatusRequest);

        assertEquals("query payment errors", 0, queryBillPaymentStatusResponse.getProcessingMessagesList().size());
        assertEquals("payments", 1, queryBillPaymentStatusResponse.getPaymentStatuses().size());

        assertNotNull("sourceId", queryBillPaymentStatusResponse.getPaymentStatuses().get(0).getSourcePaymentId());
        assertEquals("splits", 2, queryBillPaymentStatusResponse.getPaymentStatuses().get(0).getBillPaymentSplitStatuses().size());

        for (BillPaymentSplitStatus billPaymentSplitStatus : queryBillPaymentStatusResponse.getPaymentStatuses().get(0).getBillPaymentSplitStatuses()) {
            assertEquals("returns", 0, billPaymentSplitStatus.getBillPaymentReturns().size());
            assertNotNull("split Id", billPaymentSplitStatus.getSourcePaymentSplitId());
            assertEquals("state", TransactionStateEnum.Created, billPaymentSplitStatus.getTransactionState());
        }
        Company company = Company.findCompany(sourceCompanyId, SourceSystemCode.QBDT);
        PayrollServicesTest.validateSourceSystemTransmission(PayrollServicesTest.getSourceSystem(company.getId().toString(), SourceSystemCode.QBDT, SourceSystemCode.PSP,
                TransmissionType.WSBillPayQueryPaymentStatus), "test_billPaymentWeb_queryHappyPath.xml",
                Arrays.asList("SourcePaymentSplitId"));
        // todo offload and/or return the splits and make sure the returns come back ok
    }

    @Test
    public void testSubmit_Non1099() {
        String sourceCompanyId = "123272727";
        String payeeId = "Id12345";
        String sourceBankAccountId = "Id45628";

        // company setup
        PayrollServices.beginUnitOfWork();
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        psdl.loadDataForBillPaymentSubmit();
        PayrollServices.commitUnitOfWork();


        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1234567a");
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        submitPaymentRequest.setCompany(qbCompany);


        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");


        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId(sourceBankAccountId);

        for (int i = 0; i < 2; i++) {
            PaymentTransaction paymentTransaction = new PaymentTransaction();

            paymentTransaction.setDepositDate(new Date("04/22/2013"));
            paymentTransaction.setTransactionType(TransactionTypeEnum.PayBills);
            paymentTransaction.setTransactionId(UUID.randomUUID().toString());

            paymentTransaction.setPayee(new QBPayee());
            //Set oen payee as 1099 and another as non-1099
            paymentTransaction.getPayee().setIs1099(i == 0 ? true : false);
            paymentTransaction.getPayee().setName("Employee " + i);
            paymentTransaction.getPayee().setPayeeSourceId(UUID.randomUUID().toString());

            QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
            qbBillPaymentSplit.setAmount(new BigDecimal(i));
            qbBillPaymentSplit.setSourceBillPaymentSplitId(UUID.randomUUID().toString());

            qbBillPaymentSplit.setBankAccount(new QBBankAccount());
            qbBillPaymentSplit.getBankAccount().setAccountNumber("12345" + i);
            qbBillPaymentSplit.getBankAccount().setAccountType(QBBankAccountTypeEnum.CHECKING);
            qbBillPaymentSplit.getBankAccount().setBankName("Bank " + i);
            qbBillPaymentSplit.getBankAccount().setRoutingNumber("321178420");
            qbBillPaymentSplit.getBankAccount().setSourceBankAccountId(UUID.randomUUID().toString());

            paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

            submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);
        }

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

        assertEquals("submit payment errors", 1, submitPaymentResponse.getProcessingMessagesList().size());
        QBProcessingMessage errorMessage = submitPaymentResponse.getProcessingMessagesList().get(0);

        Assert.assertEquals("Error code should be 15", 15, errorMessage.getCode());

    }

    public void submitBillPayment(boolean only1099) {
        String sourceCompanyId = "123272727";
        String payeeId = "Id12345";
        String sourceBankAccountId = "Id45628";
        SubmitPaymentRequest submitPaymentRequest = new SubmitPaymentRequest();
        submitPaymentRequest.setPIN("1234567a");
        submitPaymentRequest.setPSID(sourceCompanyId);

        QBCompany qbCompany = new QBCompany();
        qbCompany.setClientApplicationName("qb");
        qbCompany.setClientApplicationVersion("20.01.R.10/14586#pro");
        submitPaymentRequest.setCompany(qbCompany);


        QBAddress qbAddress = new QBAddress();
        qbAddress.setAddressLine1("address line 1");
        qbAddress.setAddressLine2("address line 2");
        qbAddress.setAddressLine3("address line 3");
        qbAddress.setCity("Reno");
        qbAddress.setState("NV");
        qbAddress.setZipCode("89511");
        qbAddress.setZipCodeExtension("0443");


        QBBankAccount qbBankAccount = new QBBankAccount();
        qbBankAccount.setAccountNumber("1234567");
        qbBankAccount.setAccountType(QBBankAccountTypeEnum.CHECKING);
        qbBankAccount.setBankName("BOA");
        qbBankAccount.setRoutingNumber("111111118");
        qbBankAccount.setSourceBankAccountId(sourceBankAccountId);

        for (int i = 0; i < 2; i++) {
            PaymentTransaction paymentTransaction = new PaymentTransaction();

            paymentTransaction.setDepositDate(new Date(PSPDate.getPSPTime().getTimeInMilliseconds()));
            paymentTransaction.setTransactionType(TransactionTypeEnum.PayBills);
            paymentTransaction.setTransactionId(UUID.randomUUID().toString());

            paymentTransaction.setPayee(new QBPayee());
            //Set oen payee as 1099 and another as non-1099
            if (only1099) {
                paymentTransaction.getPayee().setIs1099(true);
            } else {
                paymentTransaction.getPayee().setIs1099(i == 0 ? true : false);
            }

            paymentTransaction.getPayee().setName("Employee " + i);
            paymentTransaction.getPayee().setPayeeSourceId(UUID.randomUUID().toString());

            QBBillPaymentSplit qbBillPaymentSplit = new QBBillPaymentSplit();
            qbBillPaymentSplit.setAmount(new BigDecimal(i+1));
            qbBillPaymentSplit.setSourceBillPaymentSplitId(UUID.randomUUID().toString());

            qbBillPaymentSplit.setBankAccount(new QBBankAccount());
            qbBillPaymentSplit.getBankAccount().setAccountNumber("12345" + i);
            qbBillPaymentSplit.getBankAccount().setAccountType(QBBankAccountTypeEnum.CHECKING);
            qbBillPaymentSplit.getBankAccount().setBankName("Bank " + i);
            qbBillPaymentSplit.getBankAccount().setRoutingNumber("321178420");
            qbBillPaymentSplit.getBankAccount().setSourceBankAccountId(UUID.randomUUID().toString());

            paymentTransaction.getBillPaymentSplits().add(qbBillPaymentSplit);

            submitPaymentRequest.getPaymentTransactions().add(paymentTransaction);
        }

        BillPaymentWebServices billPaymentWebServices = new BillPaymentWebServices();
        SubmitPaymentResponse submitPaymentResponse = billPaymentWebServices.SendPaymentsToPayees(submitPaymentRequest);

    }
}
