package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.ServiceChargePrices;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayImpl;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfDecimal;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Iterator;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 15, 2008
 * Time: 5:27:24 PM
 * To change this template use File | Settings | File Templates.
 */
public class BillingManagerTests {
    public static final String newline = System.getProperty("line.separator");

    static public final BigDecimal PER_TRANSMISSION_PRICE = BigDecimal.valueOf(3.00);
    static public final BigDecimal PER_PAYCHECK_PRICE = BigDecimal.valueOf(0.99);

    private Company mCompany;
    private PayrollRun mPayrollRun;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        //SalesTaxGatewayFactory.setInstanceClass(SalesTaxGatewayImpl.class);
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        //SalesTaxGatewayImpl.setSalesTaxGatewayParameterInfo(null, null, null);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /**
     * Test to add the billing details without computing the sales tax (i.e company is excempted from the tax)
     *
     * @throws Exception exception
     */
    @Test
    public void testHappyPath_AddBillingDetails_WithoutTax() throws Exception {
        //Load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        createCompanyAndSubmitPayroll(taxExemptExpirationDate);

        //Add billing details
        PayrollServices.beginUnitOfWork();

        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        assertEquals("Billing Detail List", 1, payrollRun.getBillingDetailCollection().size());
        SpcfMoney totalAmount = new SpcfMoney("0");
        SpcfMoney discAmount = new SpcfMoney("0");
        for (Iterator iterator = payrollRun.getBillingDetailCollection().iterator(); iterator.hasNext();) {
            BillingDetail detail = (BillingDetail) iterator.next();
            totalAmount = new SpcfMoney(totalAmount.add(detail.getItemTotal()));
            discAmount = new SpcfMoney(discAmount.add(detail.getDiscountAmount()));

            assertNotNull("Fee Transaction", detail.getFeeTransaction());

            assertEquals("Fee Fin Txn Amt ", detail.getItemTotal(),
                    detail.getFeeTransaction().getFinancialTransactionAmount());

            assertNull("Tax Transaction ", detail.getTaxTransaction());
        }

        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> taxTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of EmployerFeeDebit CR txns", 1, feeTxns.size());
        assertEquals("Number of ServiceSalesAndUseTax CR txns", 0, taxTxns.size());
    }

    /**
     * Test to add multiple billing details with computing the sales tax
     *
     * @throws Exception exception
     */
    @Test
    public void testHappyPath_AddMultipleBillingDetails() throws Exception {
        //Load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        createCompanyAndSubmitPayroll(taxExemptExpirationDate);

        //Add billing details
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(
                ServiceCode.DirectDeposit);
        CompanyOffering companyOffering = getMCompany().getOffering(ServiceCode.DirectDeposit);
        BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerTransmission, 5, companyOffering.getOffering().getOfferingCode());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // must refresh references to entities because we're in a new unit-of-work
        payrollRun = Application.findById(PayrollRun.class, payrollRun.getId());
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        assertEquals("Billing Detail List", 2, payrollRun.getBillingDetailCollection().size());

        SpcfMoney totalAmount = new SpcfMoney("0");
        SpcfMoney discAmount = new SpcfMoney("0");
        for (Iterator iterator = payrollRun.getBillingDetailCollection().iterator(); iterator.hasNext();) {
            BillingDetail detail = (BillingDetail) iterator.next();
            totalAmount = new SpcfMoney(totalAmount.add(detail.getItemTotal()));
            discAmount = new SpcfMoney(discAmount.add(detail.getDiscountAmount()));

            assertNotNull("Fee Transaction", detail.getFeeTransaction());

            assertEquals("Fee Fin Txn Amt ", detail.getItemTotal(),
                    detail.getFeeTransaction().getFinancialTransactionAmount());

            assertNull("Tax Transaction ", detail.getTaxTransaction());
        }

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> taxTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of EmployerFeeDebit CR txns", 2, feeTxns.size());
        assertEquals("Number of ServiceSalesAndUseTax CR txns", 0, taxTxns.size());
    }

    /**
     * Test to add a new bill to the payroll run when rest of the bills are already offloaded for that payroll run
     *
     * @throws Exception exception
     */
    @Test
    public void testAddBillingDetails_AfterOffload() throws Exception {
        //Load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        createCompanyAndSubmitPayroll(taxExemptExpirationDate);

        // make sure exactly one BillingDetail was created
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        int numDetails = payrollRun.getBillingDetailCollection().size();
        assertEquals("Billing Detail List", 1, numDetails);

        // offload that fee transaction
        offloadIncludingFT(payrollRun.getBillingDetailCollection().get(0).getFeeTransaction());

        PayrollServices.commitUnitOfWork();

        // make sure it offloaded
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> eeFinancialTxs = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of EmployerFeeDebit EX txns", 1, eeFinancialTxs.size());

        PayrollServices.beginUnitOfWork();

        // these have to be created/gotten in the same session where the BillingManager will be used
        payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        //Add new bill
        assertEquals("Billing Detail List", 1, payrollRun.getBillingDetailCollection().size());
        BillingDetail older = payrollRun.getBillingDetailCollection().get(0);
        assertEquals("Older Fee FT is Executed",
                    TransactionStateCode.Executed,
                    older.getFeeTransaction().getCurrentTransactionState().getTransactionStateCd());

        BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerTransmission, 6, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());

        assertEquals("Billing Detail List", 2, payrollRun.getBillingDetailCollection().size());
        BillingDetail newer = null;
        for (BillingDetail bd : payrollRun.getBillingDetailCollection()) {
            if (!older.equals(bd)) {
                newer = bd;
            }
        }

        assertEquals("Newer Fee FT is Created",
                    TransactionStateCode.Created,
                    newer.getFeeTransaction().getCurrentTransactionState().getTransactionStateCd());

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test to add a new bill for duplicate charge type
     *
     * @throws Exception exception
     */
    @Test
    public void testAddBillingDetails_DuplicateChargeType() throws Exception {
        //Load the data for Payroll
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        createCompanyAndSubmitPayroll(taxExemptExpirationDate);

        //Add billing details
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        try {
            BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.DirectDepositFee, 6, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());
            BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.DirectDepositFee, 5, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());
        } catch (Exception ex) {
            assertEquals("Exception Message", "Billing Detail Already Exists for the Charge Type : "
                    + OfferingServiceChargeType.DirectDepositFee, ex.getMessage());
        }

        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Test to add the billing details with sales tax (i.e company is not tax exempt)
     *
     * @throws Exception exception
     */
    @Test
    public void testAddBillingDetails_WithTax() throws Exception {

        //Load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        createCompanyAndSubmitPayroll(DataLoader.TAXABLE_ADDRESS, null, null);

        // add a PerTransmission charge (usually subject to sales tax, if the address is right)
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(
                ServiceCode.DirectDeposit);
        SpcfCalendar settlementDate = payrollRun.getBillingDetailCollection().get(0).getFeeTransaction().getSettlementDate();
        CompanyOffering companyOffering = getMCompany().getOffering(ServiceCode.DirectDeposit);
        BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerTransmission, 5, settlementDate.toLocal(), companyOffering.getOffering().getOfferingCode());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("Billing Detail List", 2, payrollRun.getBillingDetailCollection().size()); // per-check and per-transmission

        // make sure there are 2 charges, both with fee FTs, and that the per-transmission fee has a tax FT
        for (Iterator iterator = payrollRun.getBillingDetailCollection().iterator(); iterator.hasNext();) {
            BillingDetail detail = (BillingDetail) iterator.next();
            assertNotNull("Fee Transaction", detail.getFeeTransaction());
            if (detail.getOfferingServiceChargeType() == OfferingServiceChargeType.PerTransmission) {
                assertNotNull("Tax Transaction ", detail.getTaxTransaction());
            }
        }
        PayrollServices.commitUnitOfWork();

        // make sure all fee, tax and DD transactions have the right MMT association
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> taxTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> ddTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerDdDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        assertEquals("Number of fee CR txns", 2, feeTxns.size()); // per-check and per-transmission
        assertEquals("Number of sales tax CR txns", 2, taxTxns.size());
        assertEquals("Number of dd CR txns", 1, ddTxns.size());

        // there should be 1 DD, 2 fee, and 2 tax FT associated with the same MMT
        MoneyMovementTransaction mmt = feeTxns.get(0).getMoneyMovementTransaction();
        assertEquals("Number of Financial Transactions Associated with MMT", 5,
                mmt.getFinancialTransactionCollection().size());

        for (FinancialTransaction ft : feeTxns) {
            assertEquals("SKU " + ft.getSku() + " fee FT in the right MMT", mmt.getId(), ft.getMoneyMovementTransaction().getId());
        }
        for (FinancialTransaction ft : taxTxns) {
            assertEquals("SKU " + ft.getSku() + " tax FT in the right MMT", mmt.getId(), ft.getMoneyMovementTransaction().getId());
        }
        assertEquals("DD FT in the right MMT", mmt.getId(), ddTxns.get(0).getMoneyMovementTransaction().getId());

        // make sure the MMT amount is the sum of the FT amounts (all FTs are debits, in this case)
        DomainEntitySet<FinancialTransaction> finTxns = mmt.getFinancialTransactionCollection();
        SpcfDecimal ftSum = SpcfDecimal.createInstance(0);
        for (FinancialTransaction ft : mmt.getFinancialTransactionCollection()) {
            ftSum = ftSum.add(ft.getFinancialTransactionAmount());
        }
        assertEquals("Money Movement Txn Amount ", ftSum, mmt.getMoneyMovementTransactionAmount());

        // there should be only 3 entry detail records: 1 for DD, 1 PerTransmission fee+tax, and 1 PerPaycheck fee+tax
        DomainEntitySet<EntryDetailRecord> entryDetailRecords = mmt.getEntryDetailRecordCollection();
        assertEquals("Number of Entry Detail Records", 3, entryDetailRecords.size());

        entryDetailRecords = entryDetailRecords.sort(EntryDetailRecord.Amount());
        assertEquals("Entry Detail Amount ", ftSum, entryDetailRecords.get(2).getAmount());

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test to add a new bill to the payroll run when rest of the bills are already offloaded for that payroll run
     *
     * @throws Exception exception
     */
    @Test
    public void testUpdateBillingDetails_AfterOffload() throws Exception {
        //Load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        createCompanyAndSubmitPayroll(taxExemptExpirationDate);

        //Offload EmployerFeeDebit Transactions
        PayrollServices.beginUnitOfWork();

        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        offloadIncludingFT(payrollRun.getBillingDetailCollection().get(0).getFeeTransaction());

        PayrollServices.commitUnitOfWork();

        //Update a billing details
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        CompanyOffering companyOffering = getMCompany().getOffering(ServiceCode.DirectDeposit);
        BillingDetail.updateBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.DirectDepositFee, 5, companyOffering.getOffering().getOfferingCode());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        // must refresh references to entities because we're in a new unit-of-work
        payrollRun = Application.findById(PayrollRun.class, payrollRun.getId());
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        assertEquals("Billing Detail List", 1, payrollRun.getBillingDetailCollection().size()); // the one saved and offloaded
        PayrollServices.commitUnitOfWork();

        //Assertion for Fee Transaction
        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> feeTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();

        assertEquals("Number of EmployerFeeDebit Executed txns", 1, feeTxns.size());
    }

    /**
     * Test to update the existing bill details without computing the sales tax (i.e company is excempted from the tax)
     *
     * @throws Exception exception
     */
    @Test
    public void testHappyPath_UpdateBillingDetails() throws Exception {
        //Load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        createCompanyAndSubmitPayroll(taxExemptExpirationDate);

        //Add billing details
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount cba = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        CompanyOffering companyOffering = getMCompany().getOffering(ServiceCode.DirectDeposit);

        BillingDetail.createBillingDetail(payrollRun, cba, OfferingServiceChargeType.PerTransmission, 6, companyOffering.getOffering().getOfferingCode());
        companyOffering = getMCompany().getOffering(ServiceCode.DirectDeposit);
        BillingDetail.updateBillingDetail(payrollRun, cba, OfferingServiceChargeType.PerTransmission, 5, companyOffering.getOffering().getOfferingCode());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertEquals("Billing Detail List", 2, payrollRun.getBillingDetailCollection().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> feeTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        DomainEntitySet<FinancialTransaction> taxTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        DomainEntitySet<FinancialTransaction> feeTxns1 = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> taxTxns1 = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();

        //Assertion for Canceled Fee Transaction & Tax Transactions
        assertEquals("Number of EmployerFeeDebit Cancelled txns", 1, feeTxns.size());
        assertEquals("Number of ServiceSalesAndUseTax Cancelled txns", 0, taxTxns.size());

        //Assertion for Created Fee Transaction & Tax Transactions
        assertEquals("Number of EmployerFeeDebit Created txns", 2, feeTxns1.size());
        assertEquals("Number of ServiceSalesAndUseTax Created txns", 0, taxTxns1.size());
    }

    /**
     * Test to update the existing bill details with sales tax (i.e company is not excempted from the tax)
     *
     * @throws Exception exception
     */
    @Test
    public void testHappyPathUpdateBillingDetails_WithSalesTax() throws Exception {

        //Load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(-1); // expired long ago... not exempt from tax
        createCompanyAndSubmitPayroll(DataLoader.TAXABLE_ADDRESS, taxExemptExpirationDate, "P57553");

        // add a PerTransmission charge (usually subject to sales tax, if the address is right)
        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(
                ServiceCode.DirectDeposit);
        SpcfCalendar settlementDate = payrollRun.getBillingDetailCollection().get(0).getFeeTransaction().getSettlementDate();

        CompanyOffering companyOffering = getMCompany().getOffering(ServiceCode.DirectDeposit);
        DomainEntitySet<BillingDetail> detailPerTrans = BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerTransmission, 1, settlementDate.toLocal() , companyOffering.getOffering().getOfferingCode());

        PayrollServices.commitUnitOfWork();

        for (BillingDetail detailPerTran : detailPerTrans) {
            // call update to increase the per-transmission quantity
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
            payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
            companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
            companyOffering = getMCompany().getOffering(ServiceCode.DirectDeposit);
            BillingDetail.updateBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerTransmission, 5, companyOffering.getOffering().getOfferingCode());
            DomainEntitySet<BillingDetail> details = payrollRun.getBillingDetailCollection();
            detailPerTran = Application.refresh(detailPerTran);
            PayrollServices.commitUnitOfWork();
            assertEquals("Billing Detail List", 2, details.size());
        }

        PayrollServices.beginUnitOfWork();
        Application.refresh(payrollRun);
        DomainEntitySet<FinancialTransaction> feeTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        DomainEntitySet<FinancialTransaction> taxTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        DomainEntitySet<FinancialTransaction> feeTxns1 = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> taxTxns1 = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();

        //Assertion for Canceled Fee Transaction & Tax Transactions
        assertEquals("Number of EmployerFeeDebit Cancelled txns", 1, feeTxns.size());
        assertEquals("Number of ServiceSalesAndUseTax Cancelled txns", 1, taxTxns.size());

        //Assertion for Created Fee Transaction & Tax Transactions
        assertEquals("Number of EmployerFeeDebit Created txns", 2, feeTxns1.size()); // per-check and (updated) per-trans
        assertEquals("Number of ServiceSalesAndUseTax Created txns", 2, taxTxns1.size());

        SpcfMoney totalAmount = new SpcfMoney("0");
        SpcfMoney discAmount = new SpcfMoney("0");
        SpcfMoney taxAmount = new SpcfMoney("0");

        PayrollServices.beginUnitOfWork();
        // must refresh references to entities because we're in a new unit-of-work
        payrollRun = Application.findById(PayrollRun.class, payrollRun.getId());
        companyBankAccount = Application.findById(CompanyBankAccount.class, companyBankAccount.getId());
        for (Iterator iterator = payrollRun.getBillingDetailCollection().iterator(); iterator.hasNext();) {
            BillingDetail detail = (BillingDetail) iterator.next();
            totalAmount = new SpcfMoney(totalAmount.add(detail.getItemTotal()));
            discAmount = new SpcfMoney(discAmount.add(detail.getDiscountAmount()));
            taxAmount = new SpcfMoney(taxAmount.add(detail.getTaxAmount()));

            assertNotNull("Fee Transaction", detail.getFeeTransaction());
            if (detail.getOfferingServiceChargeType() == OfferingServiceChargeType.PerTransmission) {
                assertNotNull("Tax Transaction ", detail.getTaxTransaction());
            }
        }
        PayrollServices.rollbackUnitOfWork();
    }

    /**
     * Test fix for PSRV000961: When a per paycheck fee is updated, the new paycheck fee has the wrong settlement date
     * @throws Exception
     */
    @Test
    public void bug961() {
        // submit a payroll
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollRunDTO dtoPayroll = psdl.loadDataForPayrollSubmitForCompany123272727WithValidAddress();
        Company company = Company.findCompany("123272727", SourceSystemCode.QBDT);
        ProcessResult<PayrollRun> prSubmit = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), dtoPayroll);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("submit payroll", prSubmit);

        // get the settlement date for the PerPaycheck fee
        PayrollServices.beginUnitOfWork();
        company = Application.refresh(company);
        PayrollRun payrollRun = Application.refresh(prSubmit.getResult());
        CompanyBankAccount cba = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        SpcfCalendar settlementDateBefore = null;
        for (BillingDetail detail : payrollRun.getBillingDetailCollection()) {
            if (detail.getOfferingServiceChargeType() == OfferingServiceChargeType.DirectDepositFee) {
                settlementDateBefore = detail.getFeeTransaction().getSettlementDate().toLocal();
                break;
            }
        }
        PayrollServices.commitUnitOfWork();
        assertTrue("got PerPaycheck settlement date", settlementDateBefore != null);

        // update the PerPaycheck quantity
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        payrollRun = Application.refresh(payrollRun);
        cba = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        CompanyOffering companyOffering = payrollRun.getCompany().getOffering(ServiceCode.DirectDeposit);
        BillingDetail.updateBillingDetail(payrollRun, cba, OfferingServiceChargeType.DirectDepositFee, 1, companyOffering.getOffering().getOfferingCode());
        SpcfCalendar settlementDateAfter = null;
        for (BillingDetail detail : payrollRun.getBillingDetailCollection()) {
            if (detail.getOfferingServiceChargeType() == OfferingServiceChargeType.DirectDepositFee) {
                settlementDateAfter = detail.getFeeTransaction().getSettlementDate().toLocal();
                break;
            }
        }
        PayrollServices.commitUnitOfWork();

        assertEquals("settlement date after update", settlementDateBefore, settlementDateAfter); // this failed before the bug fix
    }

    /**
     * Test to add the billing details with -ve Quantity
     *
     * @throws Exception exception
     */
    @Test
    public void testAddBillingDetails_NegativeQuantity() throws Exception {
        //Load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        createCompanyAndSubmitPayroll(taxExemptExpirationDate);

        //Add billing details
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");

        CompanyBankAccount companyBankAccount = payrollRun
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        //Add new bill
        try {
            BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerPayroll, -6, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());
            PayrollServices.rollbackUnitOfWork();
            Assert.fail("The expected RuntimeException was not thrown");
        } catch (RuntimeException rx) {
            PayrollServices.rollbackUnitOfWork();
            assertEquals("Expected RuntimeException message", "The quantity must be a non-zero, positive number.", rx.getMessage());
        }
    }

    /**
     * Test to add the billing details with -ve Quantity
     *
     * @throws Exception exception
     */
    @Test
    public void testUpdateBillingDetails_NegativeQuantity() {
        //Load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        createCompanyAndSubmitPayroll(taxExemptExpirationDate);

        //Add billing details
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");

        CompanyBankAccount companyBankAccount = payrollRun
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        //Update the bill with -ve quantity
        try {
            //todo:offerings pass the offering here
            BillingDetail.updateBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerPayroll, -6, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());
            PayrollServices.rollbackUnitOfWork();
            Assert.fail("The expected RuntimeException was not thrown");
        } catch (RuntimeException rx) {
            PayrollServices.rollbackUnitOfWork();
            assertEquals("Expected RuntimeException message", "The quantity must be a positive number.", rx.getMessage());
        }
    }

    /**
     * Test to update the existing bill details without computing the sales tax (i.e company is excempted from the tax)
     *
     * @throws Exception exception
     */
    @Test
    public void testUpdateBillingDetails_ZeroQuantity() throws Exception {
        //Load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        createCompanyAndSubmitPayroll(taxExemptExpirationDate);

        //Add billing details
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");

        CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        BillingDetail.updateBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.DirectDepositFee, 0, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());

        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        assertEquals("Billing Detail List", 1, payrollRun.getBillingDetailCollection().size());
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollServices.entityFinder.findById(PayrollRun.class, payrollRun.getId());
        DomainEntitySet<FinancialTransaction> feeTxns = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Cancelled});

        DomainEntitySet<FinancialTransaction> feeTxns1 = payrollRun.getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        //Assertion for Canceled Fee Transaction & Tax Transactions
        assertEquals("Number of EmployerFeeDebit Cancelled txns", 1, feeTxns.size());

        //Assertion for Created Fee Transaction & Tax Transactions
        assertEquals("Number of EmployerFeeDebit Created txns", 0, feeTxns1.size());

        SpcfMoney totalAmount = new SpcfMoney("0");
        SpcfMoney discAmount = new SpcfMoney("0");
        for (Iterator iterator = payrollRun.getBillingDetailCollection().iterator(); iterator.hasNext();) {
            BillingDetail detail = (BillingDetail) iterator.next();
            totalAmount = new SpcfMoney(totalAmount.add(detail.getItemTotal()));
            discAmount = new SpcfMoney(discAmount.add(detail.getDiscountAmount()));

            assertNull("Fee Transaction", detail.getFeeTransaction());
            assertNull("Tax Transaction ", detail.getTaxTransaction());
        }

        PayrollServices.commitUnitOfWork();
    }

    /**
     * Add a fee with a price override, company is tax-exempt
     */
    @Test
    public void addWithPriceOverride_NoTax() {
        // load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(1);
        createCompanyAndSubmitPayroll(taxExemptExpirationDate);

        // add a fee using the price-override API
        BigDecimal overridePrice = PER_TRANSMISSION_PRICE;
        overridePrice = overridePrice.add(BigDecimal.valueOf(1.00));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount cba = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        BillingDetail.createBillingDetailWithPriceOverride(payrollRun, cba, OfferingServiceChargeType.PerTransmission, 1, overridePrice, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode(), null);

        BillingDetail pTransFee = null;
        DomainEntitySet<BillingDetail> details = payrollRun.getBillingDetailCollection();
        Iterator<BillingDetail> it = details.iterator();
        while (it.hasNext()) {
            BillingDetail detail = it.next();
            if (detail.getOfferingServiceChargeType() == OfferingServiceChargeType.PerTransmission) {
                pTransFee = detail;
                break;
            }
        }
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue("PerTransmission fee exists", pTransFee != null);
        BigDecimal savedPrice = SpcfUtils.convertToBigDecimal(pTransFee.getUnitPrice());
        Assert.assertTrue("Saved unit price is override price", savedPrice.compareTo(overridePrice) == 0);
        Assert.assertTrue("Saved unit price bigger than list price",
                pTransFee.getUnitPrice().compareTo(SpcfUtils.convertToSpcfMoney(PER_TRANSMISSION_PRICE)) > 0);
        Assert.assertTrue("Fee FT exists", pTransFee.getFeeTransaction() != null);
        Assert.assertTrue("Fee FT is ACH settlement", pTransFee.getFeeTransaction().getSettlementTypeCd() == SettlementType.ACH);

        // make sure we have the right FTs
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> taxTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of EmployerFeeDebit CR txns", 2, feeTxns.size());
        assertEquals("Number of ServiceSalesAndUseTax CR txns", 0, taxTxns.size());
    }

    /**
     * Add a fee with a price override, company is taxable
     */
    @Test
    public void addWithPriceOverride_WithTax() {
        // load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        SpcfCalendar taxExemptExpirationDate = PSPDate.getPSPTime();
        taxExemptExpirationDate.addYears(-1);
        createCompanyAndSubmitPayroll(DataLoader.TAXABLE_ADDRESS, taxExemptExpirationDate, "P57553");

        // add a fee using the price-override API
        BigDecimal overridePrice = PER_TRANSMISSION_PRICE;
        overridePrice = overridePrice.add(BigDecimal.valueOf(1.00));

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount cba = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        DomainEntitySet<BillingDetail> pTransFees = BillingDetail.createBillingDetailWithPriceOverride(payrollRun, cba, OfferingServiceChargeType.PerTransmission, 1, overridePrice, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode(), null);
        PayrollServices.commitUnitOfWork();

        for (BillingDetail pTransFee : pTransFees) {
            Assert.assertTrue("PerTransmission fee exists", pTransFee != null);
            BigDecimal savedPrice = SpcfUtils.convertToBigDecimal(pTransFee.getUnitPrice());
            Assert.assertTrue("Saved unit price is override price", savedPrice.compareTo(overridePrice) == 0);
            Assert.assertTrue("Saved unit price bigger than list price",
                    pTransFee.getUnitPrice().compareTo(SpcfUtils.convertToSpcfMoney(PER_TRANSMISSION_PRICE)) > 0);
            Assert.assertTrue("Fee FT exists", pTransFee.getFeeTransaction() != null);
            Assert.assertTrue("Fee FT is ACH settlement", pTransFee.getFeeTransaction().getSettlementTypeCd() == SettlementType.ACH);
        }

        // make sure we have the right FTs
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> taxTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of EmployerFeeDebit CR txns", 2, feeTxns.size());
        assertEquals("Number of ServiceSalesAndUseTax CR txns", 2, taxTxns.size());
    }

    /**
     * Add a fee using the price-override API but without and override price, company is taxable
     */
    @Test
    public void addWithoutPriceOverride_WithTax() {
        // load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        createCompanyAndSubmitPayroll(DataLoader.TAXABLE_ADDRESS, null, "P57553");

        // add a fee using the price-override API, but specify null as the override price to get the default (list) price
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount cba = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        DomainEntitySet<BillingDetail> pTransFees = BillingDetail.createBillingDetailWithPriceOverride(payrollRun, cba, OfferingServiceChargeType.PerTransmission, 1, null, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode(), null);
        PayrollServices.commitUnitOfWork();

        for (BillingDetail pTransFee : pTransFees) {
            Assert.assertTrue("PerTransmission fee exists", pTransFee != null);
            Assert.assertTrue("Saved unit price is list price", pTransFee.getBasePrice().compareTo(SpcfUtils.convertToSpcfMoney(PER_TRANSMISSION_PRICE)) == 0);
            Assert.assertTrue("There's a non-zero discount amount (offer was applied)", pTransFee.getDiscountAmount().compareTo(new SpcfMoney("0")) > 0);
            Assert.assertTrue("Fee FT exists", pTransFee.getFeeTransaction() != null);
            Assert.assertTrue("Fee FT is ACH settlement", pTransFee.getFeeTransaction().getSettlementTypeCd() == SettlementType.ACH);
        }

        // make sure we have the right FTs
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> taxTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of EmployerFeeDebit CR txns", 2, feeTxns.size());
        assertEquals("Number of ServiceSalesAndUseTax CR txns", 2, taxTxns.size());
    }

    /**
     * Add a fee with non-ACH settlement.  Company is not tax-exempt, but no sales tax should be charged
     */
    @Test
    public void addNonACH_WithTax() {
        // load the data for payroll (adds a PerPaycheck charge, which is almost never subject to sales tax)
        createCompanyAndSubmitPayroll(DataLoader.TAXABLE_ADDRESS, null, "P57553");

        // add a fee
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        CompanyBankAccount cba = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        BigDecimal totalPrice = BigDecimal.valueOf(123.45);
        DomainEntitySet<BillingDetail> pTransFees = BillingDetail.createNonACHFee(payrollRun,  cba,
                                                       OfferingServiceChargeType.PerTransmission, 1, totalPrice,
                                                       SettlementType.Wire, null);
        PayrollServices.commitUnitOfWork();

        for (BillingDetail pTransFee : pTransFees) {
            Assert.assertTrue("PerTransmission fee exists", pTransFee != null);
            Assert.assertTrue("Tax amount is zero", pTransFee.getTaxAmount().compareTo(new SpcfMoney("0")) == 0);
            Assert.assertTrue("No tax FT", pTransFee.getTaxTransaction() == null);
            Assert.assertTrue("Saved unit price is not list price",
                    pTransFee.getUnitPrice().compareTo(SpcfUtils.convertToSpcfMoney(PER_TRANSMISSION_PRICE)) != 0);
            Assert.assertTrue("Saved unit price is input price", pTransFee.getUnitPrice().compareTo(SpcfUtils.convertToSpcfMoney(totalPrice)) == 0);
            Assert.assertTrue("There's no discount amount (offer was not applied)", pTransFee.getDiscountAmount().compareTo(new SpcfMoney("0")) == 0);
            Assert.assertTrue("Fee FT exists", pTransFee.getFeeTransaction() != null);
            Assert.assertTrue("Fee FT is non-ACH settlement", pTransFee.getFeeTransaction().getSettlementTypeCd() == SettlementType.Wire);
        }

        // make sure we have the right FTs
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> taxTxns = getPayrollRun().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});
        PayrollServices.commitUnitOfWork();
        assertEquals("Number of EmployerFeeDebit CR txns", 2, feeTxns.size());
        assertEquals("Number of ServiceSalesAndUseTax CR txns", 1, taxTxns.size());
    }

    @Test
    public void bug562() {

        createCompanyAndSubmitPayroll(DataLoader.TAXABLE_ADDRESS, null, null);

        PayrollServices.beginUnitOfWork();
        mPayrollRun = Application.refresh(mPayrollRun);
        CompanyBankAccount cba = mPayrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        DomainEntitySet<BillingDetail> details1 = BillingDetail.createBillingDetail(mPayrollRun, cba,OfferingServiceChargeType.PerTransmission, 100, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());
        DomainEntitySet<BillingDetail> details2 = BillingDetail.createBillingDetail(mPayrollRun, cba, OfferingServiceChargeType.PerTransmission, 200, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());
        PayrollServices.commitUnitOfWork();

        for (BillingDetail detail1 : details1) {
            for (BillingDetail detail2 : details2) {
                double tax1 = Double.valueOf(detail1.getTaxAmount().toString());
                double total1 = Double.valueOf(detail1.getItemTotal().toString());
                double tax2 = Double.valueOf(detail2.getTaxAmount().toString());
                double total2 = Double.valueOf(detail2.getItemTotal().toString());

                assertTrue(tax1 > 0.0);
                assertTrue(tax2 > 0.0);

                double rate1 = Math.round(100.0 * (100.0 * tax1 / (total1 - tax1))) / 100.0;
                double rate2 = Math.round(100.0 * (100.0 * tax2 / (total2 - tax2))) / 100.0;

                System.out.println("1: qty="+detail1.getQuantity()+", unit="+detail1.getUnitPrice()+", discount="+detail1.getDiscountAmount()+", pretax="+ detail1.getPretaxAmount()+", tax="+detail1.getTaxAmount()+" ("+rate1+"%)");
                System.out.println("2: qty="+detail2.getQuantity()+", unit="+detail2.getUnitPrice()+", discount="+detail2.getDiscountAmount()+", pretax="+ detail2.getPretaxAmount()+", tax="+detail2.getTaxAmount()+" ("+rate2+"%)");

                assertEquals("Tax rates agree regardless of quantity", rate1, rate2);
            }
        }
    }

    @Test
    public void alternatePriceOffer() {
        createCompanyAndSubmitPayroll(DataLoader.TAXABLE_ADDRESS, null, "P58359"); // P58359 is the alternate-price offer

        PayrollServices.beginUnitOfWork();
        mPayrollRun = Application.refresh(mPayrollRun);
        mCompany = Application.refresh(mCompany);
        CompanyBankAccount cba = mPayrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        DomainEntitySet<BillingDetail> details = mPayrollRun.getBillingDetailCollection();

        BillingDetail detail = details.get(0);
        SpcfMoney listPrice = Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getListPrice(OfferingServiceChargeType.DirectDepositFee, 1);
        SpcfMoney unitPrice = detail.getUnitPrice();
        PayrollServices.commitUnitOfWork();

        assertEquals("BillingDetail charge type", OfferingServiceChargeType.DirectDepositFee, detail.getOfferingServiceChargeType());
        assertEquals("Configured PerPaycheck list price", ServiceChargePrices.getNormalPerPayrollServiceCharge(), listPrice);
        assertEquals("Actual PerPaycheck unit price charged", new SpcfMoney("0.75"), unitPrice);
    }

    @Test
    public void priceIncreaseTest() {
        final String TEST_OFFER_CODE = "P60642";
        final SpcfMoney TEST_PRICE = new SpcfMoney("1.50");
        final String NORMAL_OFFERING_ID = "5001608c-0001-3d29-e040-11ac3bda788c"; // DIYDDSTD-3 offering
        final String NORMAL_SKU = "293934";
        final SpcfMoney NORMAL_PRICE = ServiceChargePrices.getNormalPerPayrollServiceCharge();

        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();

        // make sure the price-test offer exists
        PayrollServices.beginUnitOfWork();
        Offer offer = Offer.findOfferByOfferCode(TEST_OFFER_CODE);
        OfferPrice altPrice = offer.getAlternatePrice(OfferingServiceChargeType.DirectDepositFee);
        PayrollServices.commitUnitOfWork();
        Assert.assertTrue("test offer exists", offer != null);
        Assert.assertEquals("test offer type", DiscountType.AltPrice, offer.getDiscountType());
        Assert.assertTrue("per-paycheck alternate price exists", altPrice != null);
        Assert.assertEquals("per-paycheck alternate price", TEST_PRICE, altPrice.getAltUnitPrice());

        // create a company WITHOUT the price-test offer code and make sure it gets the right offering and per-check unit price
        createCompanyAndSubmitPayroll(DataLoader.TAXABLE_ADDRESS, null, null);

        PayrollServices.beginUnitOfWork();
        mCompany = Application.refresh(mCompany);
        String offeringID = Offering.findOffering(mCompany,ServiceCode.DirectDeposit).getId().toString();
        mPayrollRun = Application.refresh(mPayrollRun);
        CompanyBankAccount cba = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);
        DomainEntitySet<BillingDetail> details = mPayrollRun.getBillingDetailCollection();

        Assert.assertEquals("normal company's offering", NORMAL_OFFERING_ID, offeringID);
        Assert.assertEquals("normal billing detail count", 1, details.size());
        Assert.assertEquals("normal billing detail charge type", OfferingServiceChargeType.DirectDepositFee, details.get(0).getOfferingServiceChargeType());
        Assert.assertEquals("normal billing detail sku", NORMAL_SKU, details.get(0).getItemSku());
        Assert.assertEquals("normal billing detail unit price", NORMAL_PRICE, details.get(0).getUnitPrice());
        PayrollServices.commitUnitOfWork();

        // create a company WITH the price-test offer code and make sure it gets the right offering and per-check unit price
        PayrollServices.beginUnitOfWork();
        PayrollServicesTest.truncateTables();
        PayrollServices.commitUnitOfWork();

        mCompany = null;
        mPayrollRun = null;
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        PayrollServices.commitUnitOfWork();
        createCompanyAndSubmitPayroll(DataLoader.TAXABLE_ADDRESS, null, TEST_OFFER_CODE);

        PayrollServices.beginUnitOfWork();
        mCompany = Application.refresh(mCompany);
        CompanyOffer companyOffer = mCompany.getClaimedOffer(Offer.findOfferByOfferCode(TEST_OFFER_CODE));
        offeringID = Offering.findOffering(mCompany,ServiceCode.DirectDeposit).getId().toString();
        mPayrollRun = Application.refresh(mPayrollRun);
        cba = CompanyBankAccount.findActiveCompanyBankAccount(mCompany);
        details = mPayrollRun.getBillingDetailCollection();

        Assert.assertTrue("test company has an offer", companyOffer != null);
        Assert.assertEquals("test company's offering", NORMAL_OFFERING_ID, offeringID); // same offering
        Assert.assertEquals("test billing detail count", 1, details.size());
        Assert.assertEquals("test billing detail charge type", OfferingServiceChargeType.DirectDepositFee, details.get(0).getOfferingServiceChargeType());
        Assert.assertEquals("test billing detail sku", NORMAL_SKU, details.get(0).getItemSku()); // same sku...
        Assert.assertTrue("test billing detail unit price different", ! NORMAL_PRICE.equals(details.get(0).getUnitPrice())); // ...different price
        Assert.assertEquals("test billing detail unit price", TEST_PRICE, details.get(0).getUnitPrice());
        PayrollServices.commitUnitOfWork();
    }

    // PSRV001493
    @Test
    public void testSalesTaxGatewayErrorDefaultsToPrevTaxRate() {
        final String stgErrorXmlResponse =
                "<ns0:ConfirmBOD xmlns:ns0=\"http://www.openapplications.org/oagis\">" + newline +
                "  <ns0:ApplicationArea>" + newline +
                "    <ns0:CreationDateTime>2009-09-29T04:36:13.931-07:00</ns0:CreationDateTime>" + newline +
                "    <ns0:UserArea>" + newline +
                "      <ns1:Version xmlns:ns1=\"http://www.intuit.com\">V2-0</ns1:Version>" + newline +
                "    </ns0:UserArea>" + newline +
                "  </ns0:ApplicationArea>" + newline +
                "  <ns0:DataArea>" + newline +
                "    <ns0:BOD>" + newline +
                "      <ns0:Header>" + newline +
                "        <ns0:BODFailure>" + newline +
                "          <ns0:ErrorMessage>" + newline +
                "            <ns0:Description>Transaction failed.  At least one data error occurred.</ns0:Description>" + newline +
                "            <ns0:ReasonCode>101</ns0:ReasonCode>" + newline +
                "          </ns0:ErrorMessage>" + newline +
                "        </ns0:BODFailure>" + newline +
                "      </ns0:Header>" + newline +
                "      <ns0:NounOutcome>" + newline +
                "        <ns0:NounFailure>" + newline +
                "          <ns0:ErrorMessage>" + newline +
                "            <ns0:Description>error: Unexpected element: CDATA</ns0:Description>" + newline +
                "            <ns0:ReasonCode>1000</ns0:ReasonCode>" + newline +
                "          </ns0:ErrorMessage>" + newline +
                "        </ns0:NounFailure>" + newline +
                "      </ns0:NounOutcome>" + newline +
                "    </ns0:BOD>" + newline +
                "  </ns0:DataArea>" + newline +
                "</ns0:ConfirmBOD>" + newline;

        final String stgSuccessXmlResponse =
                "<ShowQuote revision=\"8.0\" xmlns:Lacerte=\"http://www.lacertesoftware.com\" xmlns:VistaPrint=\"http://www.vistaprint.com\" xmlns:PSB=\"http://www.intuit.com/PSB\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ETX=\"http://www.intuit.com/ETX\" xmlns:ERP=\"http://www.intuit.com/ERP\" xmlns:CRM=\"http://www.intuit.com/CRM\" xmlns:Harland=\"http://www.harland.com\" xmlns:Payroll=\"http://www.intuit.com/Payroll\" xmlns:Intuit=\"http://www.intuit.com\" xmlns=\"http://www.openapplications.org/oagis\">" + newline +
                "  <ApplicationArea>" + newline +
                "    <CreationDateTime>2009-09-29T07:26:41-07:00</CreationDateTime>" + newline +
                "    <UserArea>" + newline +
                "      <Intuit:Version>V3-0</Intuit:Version>" + newline +
                "    </UserArea>" + newline +
                "  </ApplicationArea>" + newline +
                "  <DataArea>" + newline +
                "    <Show confirm=\"Never\"/>" + newline +
                "    <Quote>" + newline +
                "      <Header>" + newline +
                "        <DocumentIds>" + newline +
                "          <DocumentId>" + newline +
                "            <Id>BatchId01</Id>" + newline +
                "          </DocumentId>" + newline +
                "        </DocumentIds>" + newline +
                "        <DocumentDateTime>2009-09-29T07:24:51.605-07:00</DocumentDateTime>" + newline +
                "        <Parties>" + newline +
                "          <CustomerParty>" + newline +
                "            <Business>" + newline +
                "              <Name>Dreams Come True, Inc</Name>" + newline +
                "            </Business>" + newline +
                "            <TaxExemptInd>false</TaxExemptInd>" + newline +
                "            <Addresses>" + newline +
                "              <PrimaryAddress>" + newline +
                "                <AddressLine>13433 Wyoming Valley</AddressLine>" + newline +
                "                <AddressLine/>" + newline +
                "                <AddressLine/>" + newline +
                "                <City>Austin</City>" + newline +
                "                <StateOrProvince>TX</StateOrProvince>" + newline +
                "                <Country/>" + newline +
                "                <PostalCode>78727</PostalCode>" + newline +
                "                <TaxJurisdiction>444530000</TaxJurisdiction>" + newline +
                "              </PrimaryAddress>" + newline +
                "            </Addresses>" + newline +
                "            <Contacts>" + newline +
                "              <PrimaryContact>" + newline +
                "                <Person>" + newline +
                "                  <PersonName>" + newline +
                "                    <GivenName/>" + newline +
                "                    <FamilyName/>" + newline +
                "                  </PersonName>" + newline +
                "                </Person>" + newline +
                "                <Telephone type=\"Work\"/>" + newline +
                "                <EMailAddress/>" + newline +
                "              </PrimaryContact>" + newline +
                "            </Contacts>" + newline +
                "          </CustomerParty>" + newline +
                "        </Parties>" + newline +
                "        <UserArea>" + newline +
                "          <Intuit:SalesOrganization>PSP</Intuit:SalesOrganization>" + newline +
                "          <Intuit:TaxHandling>Standard</Intuit:TaxHandling>" + newline +
                "          <Intuit:TotalFreight currency=\"USD\">0</Intuit:TotalFreight>" + newline +
                "          <Intuit:TotalTax currency=\"USD\">19.8</Intuit:TotalTax>" + newline +
                "        </UserArea>" + newline +
                "      </Header>" + newline +
                "      <Line>" + newline +
                "        <LineNumber>1</LineNumber>" + newline +
                "        <OrderItem>" + newline +
                "          <ItemIds>" + newline +
                "            <ItemId>" + newline +
                "              <Id>293938</Id>" + newline +
                "            </ItemId>" + newline +
                "          </ItemIds>" + newline +
                "        </OrderItem>" + newline +
                "        <OrderQuantity uom=\"Each\">1</OrderQuantity>" + newline +
                "        <UnitPrice>" + newline +
                "          <Amount currency=\"USD\">300</Amount>" + newline +
                "          <PerQuantity uom=\"Each\">1</PerQuantity>" + newline +
                "        </UnitPrice>" + newline +
                "        <Tax>" + newline +
                "          <TaxAmount currency=\"USD\">19.8</TaxAmount>" + newline +
                "          <PercentQuantity uom=\"Each\">8.25</PercentQuantity>" + newline +
                "          <UserArea>" + newline +
                "            <Intuit:TaxType>SalesTax</Intuit:TaxType>" + newline +
                "          </UserArea>" + newline +
                "        </Tax>" + newline +
                "      </Line>" + newline +
                "    </Quote>" + newline +
                "  </DataArea>" + newline +
                "</ShowQuote>" + newline;

        final String stgErrorHttpResponse =
                "HTTP/1.1 200 OK" + newline +
                "Server: Apache-Coyote/1.1" + newline +
                "X-Powered-By: Servlet 2.4; JBoss-4.2.0.GA (build: SVNTag=JBPAPP_4_2_0_GA date=200706281411)/Tomcat-5.5" + newline +
                "Content-Length: " + String.valueOf(stgErrorXmlResponse.length()) + newline +
                "Date: Tue, 29 Sep 2009 11:36:13 GMT" + newline +
                "" + newline + stgErrorXmlResponse;

        final String stgSuccessHttpResponse =
                "HTTP/1.1 200 OK" + newline +
                "Server: Apache-Coyote/1.1" + newline +
                "X-Powered-By: Servlet 2.4; JBoss-4.2.0.GA (build: SVNTag=JBPAPP_4_2_0_GA date=200706281411)/Tomcat-5.5" + newline +
                "Content-Length: " + String.valueOf(stgSuccessXmlResponse.length()) + newline +
                "Date: Tue, 29 Sep 2009 14:27:16 GMT" + newline +
                "" + newline + stgSuccessXmlResponse;

        class ServerSocketThread extends Thread {
            private ServerSocket mServerSocket;
            private boolean mClosing = false;
            private String mDesiredServerResponse = null;

            class ServerClientSocketThread extends Thread {
                private final Socket mClientSocket;
                private String mDesiredResponseToClient = null;

                ServerClientSocketThread(Socket pClientSocket, String pDesiredResponseToClient) {
                    mClientSocket = pClientSocket;
                    mDesiredResponseToClient = pDesiredResponseToClient;

                    setDaemon(true);
                    start();
                }

                public void run() {
                    try {
                        System.out.println("In server client socket handler, reading data...");

                        BufferedReader reader = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));

                        while (reader.ready()) {
                            System.out.print((char) reader.read());
                        }

                        System.out.println("");

                        if (mDesiredResponseToClient != null) {
                            mClientSocket.getOutputStream().write(mDesiredResponseToClient.getBytes());
                            mClientSocket.getOutputStream().flush();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            System.out.println("Closing server client socket.");
                            mClientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            public ServerSocketThread(int pPort) {
                try {
                    mServerSocket = new ServerSocket(pPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                this.setDaemon(true);
                this.start();
            }

            public void setDesiredServerResponse(String pDesiredServerResponse) {
                mDesiredServerResponse = pDesiredServerResponse;
            }

            public void run() {
                try {
                    System.out.println("Server socket listening for client connections.");

                    while (true) {
                        final Socket client = mServerSocket.accept();

                        System.out.println("Incoming connection request detected, establishing client socket.");

                        new ServerClientSocketThread(client, mDesiredServerResponse);
                    }
                } catch (SocketException e) {
                    if (mClosing) {
                        System.out.println("Closing server socket.");
                    } else {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            public void close() {
                if ((mServerSocket != null) && !mServerSocket.isClosed()) {
                    try {
                        mClosing = true;
                        mServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        ServerSocketThread serverSocket = new ServerSocketThread(5000);

        try {
        createCompanyAndSubmitPayroll(DataLoader.TAXABLE_ADDRESS, null, null);

        // simulate a successful Sales Tax Gateway response to create a valid tax transaction.
        serverSocket.setDesiredServerResponse(stgSuccessHttpResponse);
        PayrollServices.beginUnitOfWork();
        mPayrollRun = Application.refresh(mPayrollRun);
        CompanyBankAccount cba = mPayrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        SalesTaxGatewayImpl.setSalesTaxGatewayParameterInfo("http://localhost:5000/EnterpriseOrder/Quote", 1000, 1000);
        DomainEntitySet<BillingDetail> details1 = BillingDetail.createBillingDetail(mPayrollRun, cba, OfferingServiceChargeType.PerTransmission, 100, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());
        PayrollServices.commitUnitOfWork();

        // simulate a Sales Tax Gateway error response to test default tax calculation using previous known tax rate.
        serverSocket.setDesiredServerResponse(stgErrorHttpResponse);
        PayrollServices.beginUnitOfWork();
        mPayrollRun = Application.refresh(mPayrollRun);
        cba = mPayrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        SalesTaxGatewayImpl.setSalesTaxGatewayParameterInfo("http://localhost:5000/EnterpriseOrder/Quote", 1000, 1000);
        DomainEntitySet<BillingDetail>  details2 = BillingDetail.createBillingDetail(mPayrollRun, cba, OfferingServiceChargeType.PerTransmission, 100, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());
        PayrollServices.commitUnitOfWork();

        // simulate a Sales Tax Gateway null response to test default tax calculation using previous known tax rate.
        serverSocket.setDesiredServerResponse(null);
        PayrollServices.beginUnitOfWork();
        mPayrollRun = Application.refresh(mPayrollRun);
        cba = mPayrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        SalesTaxGatewayImpl.setSalesTaxGatewayParameterInfo("http://localhost:5000/EnterpriseOrder/Quote", 1000, 1000);
        DomainEntitySet<BillingDetail>  details3 = BillingDetail.createBillingDetail(mPayrollRun, cba, OfferingServiceChargeType.PerTransmission, 100, Offering.findOffering(mCompany, ServiceCode.DirectDeposit).getOfferingCode());
        PayrollServices.commitUnitOfWork();

        for (BillingDetail detail1 : details1) {
            for (BillingDetail detail2 : details2) {
                for (BillingDetail detail3 : details3) {
                    assertEquals("Compare Billing Detail totals for 1 and 2", detail1.getItemTotal(), detail2.getItemTotal());
                    assertEquals("Compare Billing Detail totals for 2 and 3", detail2.getItemTotal(), detail3.getItemTotal());
                }
            }
        }

        } finally {
            serverSocket.close();

            // reset sales tax gateway (via billing manager) to config file settings
            SalesTaxGatewayImpl.setSalesTaxGatewayParameterInfo(null, null, null);
        }
    }

    private void createCompanyAndSubmitPayroll(SpcfCalendar pTaxExemptExpirationDate) {
        createCompanyAndSubmitPayroll(DataLoader.NON_TAXABLE_ADDRESS, pTaxExemptExpirationDate, "P57553");
    }

    /**
     * Function to load the data for payroll
     *
     * @param pTaxExemptExpirationDate SpcfCalendar
     * @throws Exception exception
     */
    private void createCompanyAndSubmitPayroll(AddressDTO pLegalAddress, SpcfCalendar pTaxExemptExpirationDate, String pOfferCd) {
        PayrollServices.beginUnitOfWork();
        // this call creates and persists company QBDT/123272727, and makes a PayrollRunDTO for it
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        PayrollRunDTO payrollRunDTO = psdl.loadDataForPayrollSubmitForCompany123272727WithValidAddress();

        // set the company's tax-exempt expiration date based on the input param
        mCompany = Company.findCompany("123272727", SourceSystemCode.QBDT);
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(mCompany);
        dtoUpdate.setTaxExemptExpirationDate(pTaxExemptExpirationDate==null ? null : new DateDTO(pTaxExemptExpirationDate));
        if (pTaxExemptExpirationDate != null) {
            /*  Tax Exemption is intended   */
            dtoUpdate.setTaxExemptStatus(TaxExemptStatusCode.Exempt);
        }
        
        if (pLegalAddress != null) {
            dtoUpdate.setLegalAddress(pLegalAddress);
        }
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(mCompany.getSourceSystemCd(), mCompany.getSourceCompanyId(), dtoUpdate);
        PayrollServices.commitUnitOfWork();
        PayrollServicesTest.assertSuccess("Updating company for tax-exempt-expiration and legal address", prUpdate);

        mCompany = prUpdate.getResult();

        DataLoadServices.updateOffering(mCompany, OfferingCode.DIYDDSTD, "DIYDD-STD");

        CompanyOffer companyOffer = null;
        if (pOfferCd != null) {
            PayrollServices.beginUnitOfWork();
            // claim offer for Company
            Offer offer = Offer.findOfferByOfferCode(pOfferCd); // P57553 is 50% off PerPaycheck charges
            SpcfCalendar offerEffectiveDate= offer.getEffectiveDate() ;
            offer.setEffectiveDate(SpcfCalendar.createInstance(2007, 6, 30, SpcfTimeZone.getLocalTimeZone()));
            mCompany = Company.findCompany("123272727", SourceSystemCode.QBDT);
            companyOffer = mCompany.claimOfferForCompany(offer);

            PayrollServices.commitUnitOfWork();

            assertTrue("offer claimed", companyOffer != null);
            assertTrue("Company Offer", companyOffer != null);
            assertTrue("Company Offer is Active", companyOffer.companyOfferIsActive());
            PayrollServices.beginUnitOfWork();
             offer = Offer.findOfferByOfferCode(pOfferCd);
            offer.setEffectiveDate(offerEffectiveDate);
            PayrollServices.commitUnitOfWork();

        }

        PayrollServices.beginUnitOfWork();
        // submit the payroll -- this will add a PerPaycheck fee with some quantity
        ProcessResult<PayrollRun> processResult;
        processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertTrue("Process Result", processResult.isSuccess());

        PayrollServices.beginUnitOfWork();
        // save the payroll run for use later in the test
        mPayrollRun = PayrollRun.findPayrollRun(getMCompany(), "BatchId01");
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Runs the offload process so as to process the given FT.  To do so, it TEMPORARILY changes the PSPTime,
     * setting it back after offload to what it was before.
     * @param pFT
     * @return the PSPTime that was used for make the offload happen
     */
    private SpcfCalendar offloadIncludingFT(FinancialTransaction pFT) {
        SpcfCalendar mmtInitDate = pFT.getMoneyMovementTransaction().getInitiationDate();

        // adjust PSP time so offload will process the target transaction
        SpcfCalendar origPspTime = PSPDate.getPSPTime();
        PSPDate.setPSPTime(mmtInitDate);
        Application.commitUnitOfWork();

        // offload
        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        Application.beginUnitOfWork();
        // restore PSP time
        PSPDate.setPSPTime(origPspTime);
        return mmtInitDate;
    }

    private Company getMCompany() {
        return PayrollServices.entityFinder.findById(Company.class, mCompany.getId());
    }

    public PayrollRun getPayrollRun() {
        return PayrollServices.entityFinder.findById(PayrollRun.class, mPayrollRun.getId());
    }
}
