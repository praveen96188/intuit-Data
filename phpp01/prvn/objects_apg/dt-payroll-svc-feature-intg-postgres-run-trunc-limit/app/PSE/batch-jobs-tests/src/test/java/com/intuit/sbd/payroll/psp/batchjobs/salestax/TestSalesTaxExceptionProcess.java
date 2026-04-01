package com.intuit.sbd.payroll.psp.batchjobs.salestax;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.CompanyDTO;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.PayrollRunDTO;
import com.intuit.sbd.payroll.psp.api.dtos.factory.DTOFactory;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayFactory;
import com.intuit.sbd.payroll.psp.gateways.salestax.SalesTaxGatewayImpl;
import com.intuit.sbd.payroll.psp.junit.LenientRunner;
import com.intuit.sbd.payroll.psp.processes.DataLoader;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.PayrollSubmitDataLoader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Calendar;
import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotSame;

/**
 * Created by IntelliJ IDEA.
 * User: rkrishna
 * Date: Apr 22, 2008
 * Time: 9:34:17 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(LenientRunner.class)
public class TestSalesTaxExceptionProcess {
    private Company mCompany;
    private PayrollRun mPayrollRun1;
    private PayrollRun mPayrollRun2;
    static final long EFFECTIVE_DATE_GRACE_PERIOD_MINUTES = 5;

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        SalesTaxGatewayFactory.setInstanceClass(SalesTaxGatewayImpl.class);
        PayrollServicesTest.truncateTables();
        PayrollServicesTest.updateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 31, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testMainProcess() {

        //Load the data for payroll
        loadData(null);

        // verify number of billing details for Payroll Run BatchId01
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        assertEquals("Billing Detail List for PayrollRun : BatchId01", 1, payrollRun.getBillingDetailCollection().size());
        PayrollServices.commitUnitOfWork();      

        // verify number of billing details for Payroll Run BatchId02
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        payrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId02");
        companyBankAccount = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        assertEquals("Billing Detail List for PayrollRun : BatchId02", 1, payrollRun.getBillingDetailCollection().size());
        PayrollServices.commitUnitOfWork();

        //Offload DdServiceFee Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070928000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Call to Sales Tax Exception Batch Process
        PayrollServices.beginUnitOfWork();
        SalesTaxExceptionProcess process = new SalesTaxExceptionProcess();
        process.process("20071017");
        PayrollServices.commitUnitOfWork();

        //Offload DdServiceFee Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20070914000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Call to Sales Tax Exception Batch Process
        PayrollServices.beginUnitOfWork();
        process = new SalesTaxExceptionProcess();
        process.process("20071017");
        PayrollServices.commitUnitOfWork();

        //Offload DdServiceFee Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071004000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        //Call to Sales Tax Exception Batch Process
        PayrollServices.beginUnitOfWork();
        process = new SalesTaxExceptionProcess();
        process.process("20071017");
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeFinancialTxs = getPayrollRun1().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        DomainEntitySet<FinancialTransaction> feeFinancialTxs1 = getPayrollRun2().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        DomainEntitySet<FinancialTransaction> taxTxs = getPayrollRun1().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        DomainEntitySet<FinancialTransaction> taxTxs1 = getPayrollRun2().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();

        //Assertion for Fee Transactions
        assertEquals("Number of DdServiceFee EX txns for Payroll Run : BatchId01", 1, feeFinancialTxs.size());
        assertEquals("Number of DdServiceFee EX txns for Payroll Run : BatchId02", 1, feeFinancialTxs1.size());

        //Assertion for Tax Transactions
        assertEquals("Number of ServiceSalesAndUseTax EX txns for Payroll Run : BatchId01", 1, taxTxs.size());
        assertEquals("Number of ServiceSalesAndUseTax EX txns for Payroll Run : BatchId02", 1, taxTxs1.size());


        PayrollServices.beginUnitOfWork();

        //Assertion for Billing Details List
        assertEquals("Billing Detail List for PayrollRun : BatchId01", 1, getPayrollRun1().getBillingDetailCollection().size());
        assertEquals("Billing Detail List for PayrollRun : BatchId02", 1, getPayrollRun2().getBillingDetailCollection().size());

        //Assertion for TaxAmoutn When Offloaded
        for (BillingDetail billingDetail : getPayrollRun1().getBillingDetailCollection()) {
            assertEquals("Tax Amount When Offloaded for PayrollRun: BatchId01", billingDetail.getTaxAmount(),
                    billingDetail.getTaxAmountWhenOffloaded());
        }

        for (BillingDetail billingDetail : getPayrollRun2().getBillingDetailCollection()) {
            assertEquals("Tax Amount When Offloaded for PayrollRun: BatchId02", billingDetail.getTaxAmount(),
                    billingDetail.getTaxAmountWhenOffloaded());
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Test case to test the Billing Details with Multiple Offload dates.
     * @throws Exception exception
     */
    //@Test
    public void testMainProcess_WithMultipleOffloads() throws Exception {
        //Load the data for payroll
        loadData(null);

        //Add billing details for Payroll Run BatchId01
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId01");
        CompanyBankAccount companyBankAccount = payrollRun
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        try {
            CompanyOffering companyOffering = mCompany.getOffering(ServiceCode.DirectDeposit);
            BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerTransmission, 5, companyOffering.getOffering().getOfferingCode());
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            Assert.fail("Unable to execute BillingManager : " + ex.getMessage());
        }

        PayrollServices.commitUnitOfWork();

        assertEquals("Billing Detail List for PayrollRun : BatchId01", 2, payrollRun.getBillingDetailCollection().size());

        //Add billing details for Payroll Run BatchId02
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 14, SpcfTimeZone.getLocalTimeZone()));
        payrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId02");
        companyBankAccount = payrollRun
                .getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        try {
            CompanyOffering companyOffering = mCompany.getOffering(ServiceCode.DirectDeposit);
            BillingDetail.createBillingDetail(payrollRun, companyBankAccount, OfferingServiceChargeType.PerTransmission, 5, companyOffering.getOffering().getOfferingCode());
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            Assert.fail("Unable to execute BillingManager : " + ex.getMessage());
        }

        PayrollServices.commitUnitOfWork();

        assertEquals("Billing Detail List for PayrollRun : BatchId02", 2, payrollRun.getBillingDetailCollection().size());

        //Offload DdServiceFee Transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071012000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> feeFinancialTxs = getPayrollRun1().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> feeFinancialTxs1 = getPayrollRun2().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        DomainEntitySet<FinancialTransaction> taxTxs = getPayrollRun1().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> taxTxs1 = getPayrollRun2().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();

        //Assertion for Not Offloaded Fee Transactions
        assertEquals("Number of DdServiceFee Created txns for Payroll Run : BatchId01", 2, feeFinancialTxs.size());

        //Assertion for Offloaded Fee Transactions
        assertEquals("Number of DdServiceFee Executed txns for Payroll Run : BatchId02", 2, feeFinancialTxs1.size());

        //Assertion for Not Offloade Tax Transactions
        assertEquals("Number of ServiceSalesAndUseTax Created txns for Payroll Run : BatchId01", 2, taxTxs.size());

        //Assertion for Not Offloade Tax Transactions
        assertEquals("Number of ServiceSalesAndUseTax Executed txns for Payroll Run : BatchId02", 2, taxTxs1.size());

        //Call Sales Tax Exception Batch Process
        PayrollServices.beginUnitOfWork();
        SalesTaxExceptionProcess process = new SalesTaxExceptionProcess();
        process.process("20071017");
        PayrollServices.commitUnitOfWork();

        //Assertion for Billing Details List
        assertEquals("Billing Detail List for PayrollRun : BatchId01", 2, getPayrollRun1().getBillingDetailCollection().size());
        assertEquals("Billing Detail List for PayrollRun : BatchId02", 2, getPayrollRun2().getBillingDetailCollection().size());

        //Assertion for TaxAmountWhenOffloaded for the Non Offloaded Transactions
        for (BillingDetail billingDetail : getPayrollRun1().getBillingDetailCollection()) {
            assertNotSame("Tax Amount When Offloaded for PayrollRun: BatchId01", billingDetail.getTaxAmount(),
                    billingDetail.getTaxAmountWhenOffloaded());
        }

        //Assertion for TaxAmountWhenOffloaded for Offloaded Transactions
        for (BillingDetail billingDetail : getPayrollRun2().getBillingDetailCollection()) {
            assertEquals("Tax Amount When Offloaded for PayrollRun: BatchId02", billingDetail.getTaxAmount(),
                    billingDetail.getTaxAmountWhenOffloaded());
        }
    }

    /**
     * Test case to test the Billing Details with Multiple Offload dates.
     */
    @Test
    public void testMainProcess_WithOutSalesTax() {
        //Load the data for payroll (adds a PerPaycheck charge)

        SpcfCalendar taxExpirationDate = PSPDate.getPSPTime();
        taxExpirationDate.addYears(1);
        loadData(taxExpirationDate);


        // add a PerTransmission charge
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 9, 14, SpcfTimeZone.getLocalTimeZone()));
        PayrollRun payrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId01");
        CompanyBankAccount cba = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        try {
            mCompany = Application.refresh(mCompany);
            CompanyOffering companyOffering = mCompany.getOffering(ServiceCode.DirectDeposit);
            BillingDetail.createBillingDetail(payrollRun, cba, OfferingServiceChargeType.PerTransmission, 5, companyOffering.getOffering().getOfferingCode());
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            Assert.fail("Unable to execute BillingManager : " + ex.getMessage());
        }
        int numDetails = payrollRun.getBillingDetailCollection().size();
        PayrollServices.commitUnitOfWork();

        assertEquals("Billing Detail List for PayrollRun : BatchId01", 1, numDetails);


        //Add billing details for Payroll Run BatchId02
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 10, 14, SpcfTimeZone.getLocalTimeZone()));
        payrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId02");
        cba = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
        CompanyOffering companyOffering = mCompany.getOffering(ServiceCode.DirectDeposit);
        try {
            BillingDetail.createBillingDetail(payrollRun, cba, OfferingServiceChargeType.PerTransmission, 5, companyOffering.getOffering().getOfferingCode());
        } catch (Exception ex) {
            PayrollServices.rollbackUnitOfWork();
            Assert.fail("Unable to execute BillingManager : " + ex.getMessage());
        }
        numDetails = payrollRun.getBillingDetailCollection().size();
        PayrollServices.commitUnitOfWork();

        assertEquals("Billing Detail List for PayrollRun : BatchId02", 1, numDetails);

        // offload one group of transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071004000000");
        Application.commitUnitOfWork();

        OffloadACHTransactions offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // offload the other group of transactions
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20071015000000");
        Application.commitUnitOfWork();

        offloader = new OffloadACHTransactions();
        offloader.offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> pr1CreatedFeeFTs = getMPayrollRun1().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> pr2ExecutedFeeFTs = getMPayrollRun2().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.EmployerFeeDebit},
                new TransactionStateCode[]{TransactionStateCode.Executed});

        DomainEntitySet<FinancialTransaction> pr1CreatedTaxFTs = getMPayrollRun1().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Created});

        DomainEntitySet<FinancialTransaction> pr2ExecutedTaxFTs = getMPayrollRun2().getFinancialTransactions(
                new TransactionTypeCode[]{TransactionTypeCode.ServiceSalesAndUseTax},
                new TransactionStateCode[]{TransactionStateCode.Executed});
        PayrollServices.commitUnitOfWork();

        //Assertion for Not Offloaded Fee Transactions
        assertEquals("Number of DdServiceFee Created txns for Payroll Run : BatchId01", 1, pr1CreatedFeeFTs.size());

        //Assertion for Offloaded Fee Transactions
        assertEquals("Number of DdServiceFee Executed txns for Payroll Run : BatchId02", 1, pr2ExecutedFeeFTs.size());

        //Assertion for Not Offloade Tax Transactions
        assertEquals("Number of ServiceSalesAndUseTax Created txns for Payroll Run : BatchId01", 0, pr1CreatedTaxFTs.size());

        //Assertion for Not Offloade Tax Transactions
        assertEquals("Number of ServiceSalesAndUseTax Executed txns for Payroll Run : BatchId02", 0, pr2ExecutedTaxFTs.size());

        //Call Sales Tax Exception Batch Process
        PayrollServices.beginUnitOfWork();
        SalesTaxExceptionProcess process = new SalesTaxExceptionProcess();
        process.process("20071017");
        PayrollServices.commitUnitOfWork();        

        PayrollServices.beginUnitOfWork();
        payrollRun = PayrollRun.findPayrollRun(mCompany, "BatchId01");
        cba = payrollRun.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        PayrollRun payrollRun2 = PayrollRun.findPayrollRun(mCompany, "BatchId02");
        cba = payrollRun2.getCompanyBankAccountForService(ServiceCode.DirectDeposit);

        //Assertion for Billing Details List
        assertEquals("Billing Detail List for PayrollRun : BatchId01", 1, payrollRun.getBillingDetailCollection().size());
        assertEquals("Billing Detail List for PayrollRun : BatchId02", 1, payrollRun2.getBillingDetailCollection().size());

        //Assertion for TaxAmountWhenOffloaded
        for (BillingDetail billingDetail : payrollRun.getBillingDetailCollection()) {
            assertEquals("Tax Amount When Offloaded for PayrollRun: BatchId01", new SpcfMoney("0"),
                    billingDetail.getTaxAmountWhenOffloaded());
        }

        //Assertion for TaxAmountWhenOffloadeds
        for (BillingDetail billingDetail : payrollRun2.getBillingDetailCollection()) {
            assertEquals("Tax Amount When Offloaded for PayrollRun: BatchId02", new SpcfMoney("0"),
                    billingDetail.getTaxAmountWhenOffloaded());
        }
        PayrollServices.commitUnitOfWork();
    }

    /**
     * Function to load the data for payroll
     * @param pTaxExemptExpirationDate boolean
     */
    public void loadData(SpcfCalendar pTaxExemptExpirationDate){
        PayrollServices.beginUnitOfWork();

        // create multiple PayrollRunDTOs so we can submit those payrolls
        PayrollSubmitDataLoader psdl = new PayrollSubmitDataLoader();
        psdl.setSrcSystemCodeForNewCompany(SourceSystemCode.QBDT);
        Collection<PayrollRunDTO> payrollRunDTOs = psdl.loadMultiplePayrollsForCompany123272727WithValidAddres();

        mCompany = Company.findCompany("123272727", SourceSystemCode.QBDT);

        // make sure that company is at an address that will be subject to sales tax
        DTOFactory fac = new DTOFactory();
        CompanyDTO dtoUpdate = fac.create(mCompany);
        dtoUpdate.setLegalAddress(DataLoader.TAXABLE_ADDRESS);
        if (pTaxExemptExpirationDate != null) {
            /*  Tax Exemption is intended   */
            dtoUpdate.setTaxExemptExpirationDate(new DateDTO(pTaxExemptExpirationDate));
            dtoUpdate.setTaxExemptStatus(TaxExemptStatusCode.Exempt);
        }
        ProcessResult<Company> prUpdate = PayrollServices.companyManager.updateCompany(mCompany.getSourceSystemCd(),
                                                                                       mCompany.getSourceCompanyId(),
                                                                                       dtoUpdate);
        PayrollServicesTest.assertSuccess("Updating company address for taxability", prUpdate);

        mCompany = prUpdate.getResult();

        PayrollServices.commitUnitOfWork();

        // submit those payrolls
        PayrollServices.beginUnitOfWork();
        for (PayrollRunDTO payrollRunDTO : payrollRunDTOs) {
            // submit this payroll -- this will add a PerPaycheck charge
            ProcessResult<PayrollRun> result = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, "123272727", payrollRunDTO);
            PayrollServicesTest.assertSuccess("submitPayroll()", result);
            PayrollRun payroll = result.getResult();
            
            // add a per-transmission charge -- it's subject to sales tax at the TAXABLE_ADDRESS... per-check is not
            mCompany = Application.refresh(mCompany);
            DomainEntitySet<FinancialTransaction> found = FinancialTransaction.findFinancialTransactions(
                                                        mCompany, payrollRunDTO.getPayrollTXBatchId(), null, null, null,
                                                        TransactionTypeCode.EmployerDdDebit, null, null,
                                                        TransactionStateCode.Created);
            SpcfCalendar settlementDate = found.get(0).getSettlementDate().toLocal();
            CompanyBankAccount cba = payroll.getCompanyBankAccountForService(ServiceCode.DirectDeposit);
            CompanyOffering companyOffering = mCompany.getOffering(ServiceCode.DirectDeposit);
            BillingDetail.createBillingDetail(payroll, cba, OfferingServiceChargeType.PerTransmission, 1, settlementDate, companyOffering.getOffering().getOfferingCode()); // a taxable fee -- per-check is not
        }
        PayrollServices.commitUnitOfWork();

        Calendar nextMonth = CalendarUtils.convertToCalendar(PSPDate.getPSPTime());
        nextMonth.add(Calendar.MONTH, 1);

        PayrollServices.beginUnitOfWork();
        mCompany = Company.findCompany("123272727", SourceSystemCode.QBDT);
        mPayrollRun1 = PayrollRun.findPayrollRun(mCompany, "BatchId01");
        mPayrollRun2 = PayrollRun.findPayrollRun(mCompany, "BatchId02");
        PayrollServices.commitUnitOfWork();
    }

    private PayrollRun getPayrollRun1() {
        return PayrollServices.entityFinder.findById(PayrollRun.class, getMPayrollRun1().getId());
    }

    private PayrollRun getPayrollRun2() {
        return PayrollServices.entityFinder.findById(PayrollRun.class, getMPayrollRun2().getId());
    }

    private PayrollRun getMPayrollRun1() {
        return PayrollServices.entityFinder.findById(PayrollRun.class, mPayrollRun1.getId());
    }

    private PayrollRun getMPayrollRun2() {
        return PayrollServices.entityFinder.findById(PayrollRun.class, mPayrollRun2.getId());
    }
}
