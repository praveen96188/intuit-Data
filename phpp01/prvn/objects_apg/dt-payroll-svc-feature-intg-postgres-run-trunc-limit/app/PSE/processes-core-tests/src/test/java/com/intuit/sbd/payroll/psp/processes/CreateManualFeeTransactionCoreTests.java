package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.ERFeeAddDTO;
import com.intuit.sbd.payroll.psp.api.dtos.SettlementTypeDTO;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.*;
import static org.junit.Assert.assertNull;

/**
 * User: ihannur
 * Date: 6/28/12
 * Time: 9:40 AM
 */
public class CreateManualFeeTransactionCoreTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2011, 1, 1));
        DataLoadServices.setPSPDate(2012, 1, 12);
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testAmendedSSN() {
        String psid = "123456789";
        DataLoadServices.setupCompany(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(), company.getSourceCompanyId(), null,
                SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(PSPDate.getPSPTime()),
                new SpcfMoney("25.00"),
                OfferingServiceChargeType.AmendedSSN, null);

        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> result = PayrollServices.financialTransactionManager.createManualFeeTransaction(feeAddDTO);
        assertSuccess(result);
        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        financialTransactions.add(result.getResult().getFirst());
        PayrollServices.commitUnitOfWork();

        assertManualFee(company, new SpcfMoney("25"), OfferingServiceChargeType.AmendedSSN, SettlementTypeDTO.ACH, null);

        offloadAndReturnFeeDebit(financialTransactions);

    }

    @Test
    public void testOtherFee() {
        String psid = "123456789";
        DataLoadServices.setupCompany(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(), company.getSourceCompanyId(), null,
                SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(PSPDate.getPSPTime()),
                new SpcfMoney("5.10"),
                OfferingServiceChargeType.OtherFee, "Other Fee-Testing");

        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> result = PayrollServices.financialTransactionManager.createManualFeeTransaction(feeAddDTO);
        assertSuccess(result);

        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        financialTransactions.add(result.getResult().getFirst());
        PayrollServices.commitUnitOfWork();

        assertManualFee(company, new SpcfMoney("5.10"), OfferingServiceChargeType.OtherFee, SettlementTypeDTO.ACH, "Other Fee-Testing");

        offloadAndReturnFeeDebit(financialTransactions);
    }

    @Test
    public void testExtraCopies_CheckType() {
        String psid = "123456789";
        DataLoadServices.setupCompany(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(), company.getSourceCompanyId(), null,
                SettlementTypeDTO.CheckType,
                CalendarUtils.convertToDate(PSPDate.getPSPTime()),
                new SpcfMoney("5.10"),
                OfferingServiceChargeType.ExtraCopies, null);

        PayrollServices.beginUnitOfWork();
        ProcessResult<DomainEntitySet<FinancialTransaction>> result = PayrollServices.financialTransactionManager.createManualFeeTransaction(feeAddDTO);
        assertSuccess(result);

        DomainEntitySet<FinancialTransaction> financialTransactions = new DomainEntitySet<FinancialTransaction>();
        financialTransactions.add(result.getResult().getFirst());
        PayrollServices.commitUnitOfWork();

        assertManualFee(company, new SpcfMoney("5.10"), OfferingServiceChargeType.ExtraCopies, SettlementTypeDTO.CheckType, null);

    }

    @Test
    public void testCreateMultipleFees_ThenCancelOne() {
        String psid = "123456789";
        DataLoadServices.setupCompany(psid);

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PayrollServices.commitUnitOfWork();

        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(), company.getSourceCompanyId(), null,
                                                SettlementTypeDTO.ACH,
                                                CalendarUtils.convertToDate(PSPDate.getPSPTime()),
                                                new SpcfMoney("5.10"),
                                                OfferingServiceChargeType.OtherFee, "Other Fee-Testing");

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.createManualFeeTransaction(feeAddDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(company.getSourceCompanyId(), company.getSourceSystemCd());
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRunsByType(company, null, null, PayrollType.FeeOnly));
        feeAddDTO = new ERFeeAddDTO(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollRun.getSourcePayRunId(),
                                    SettlementTypeDTO.ACH,
                                    CalendarUtils.convertToDate(PSPDate.getPSPTime()),
                                    new SpcfMoney("10.20"),
                                    OfferingServiceChargeType.OtherFee, "Other Fee-Testing2");

        ProcessResult<DomainEntitySet<FinancialTransaction>> result = PayrollServices.financialTransactionManager.createManualFeeTransaction(feeAddDTO);
        assertSuccess(result);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        LiabilityCheck liabilityCheck = assertOne(payrollRun.getLiabilityCheckCollection().find(LiabilityCheck.Type().equalTo(LiabilityCheckType.EmployerFee)));
        assertEquals("liability check amount", new SpcfMoney("-15.46"), liabilityCheck.getAmount());
        assertEquals("detail lines", 3, liabilityCheck.getLiabilityCheckLineCollection().size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.cancelTransaction(company.getSourceSystemCd(), company.getSourceCompanyId(), result.getResult().getFirst().getId().toString()));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        payrollRun = Application.refresh(payrollRun);
        liabilityCheck = assertOne(payrollRun.getLiabilityCheckCollection().find(LiabilityCheck.Type().equalTo(LiabilityCheckType.EmployerFee)));
        assertEquals("liability check amount", new SpcfMoney("-5.26"), liabilityCheck.getAmount());
        assertEquals("detail lines", 3, liabilityCheck.getLiabilityCheckLineCollection().size());
        PayrollServices.rollbackUnitOfWork();
    }

    private void offloadAndReturnFeeDebit(DomainEntitySet<FinancialTransaction> financialTransactions) {
        DataLoadServices.runOffload();

        Application.beginUnitOfWork();
        PSPDate.addBusinessDaysToPSPTime(3);
        Application.commitUnitOfWork();

        DataLoadServices.returnTxns(financialTransactions);

        FinancialTransaction financialTransaction = assertOne(financialTransactions);
        PayrollServices.beginUnitOfWork();
        Application.refresh(financialTransaction);
        assertEquals("FT Status after return", TransactionState.findTransactionState(TransactionStateCode.Returned), financialTransaction.getCurrentTransactionState());
        DomainEntitySet<FinancialTransaction> totalFts = Application.find(FinancialTransaction.class, FinancialTransaction.PayrollRun().equalTo(financialTransaction.getPayrollRun()).And(FinancialTransaction.TransactionType().TransactionTypeCd().notEqualTo(TransactionTypeCode.ServiceSalesAndUseTax)));
        assertEquals("No additional fee created by returns handler", financialTransactions.size(), totalFts.size());
        assertTrue("Company on Hold", financialTransaction.getCompany().isCompanyOnHold());
        PayrollServices.rollbackUnitOfWork();
    }
    
    public static void assertManualFee(Company pCompany, SpcfMoney pFeeAmount, OfferingServiceChargeType pType, SettlementTypeDTO pSettlementType, String pMemo) {

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRunsByType(pCompany, null, null, PayrollType.FeeOnly));
        PayrollStatus payrollStatus = PayrollStatus.Complete;
        for (FinancialTransaction ft:payrollRun.getFinancialTransactionCollection()) {
            if (!ft.getCurrentFinancialTransactionState().getTransactionState().getTransactionStateCd().equals(TransactionStateCode.Completed)){
                payrollStatus = PayrollStatus.Pending;
            }
        }
        assertEquals("Fee Only PayrollRun status",payrollStatus, payrollRun.getPayrollRunStatus());

        //Assert financial transaction
        FinancialTransaction financialTransaction = assertOne(payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.TransactionType().TransactionTypeCd().notEqualTo(TransactionTypeCode.ServiceSalesAndUseTax)));
        assertEquals("Fee Amount", pFeeAmount, financialTransaction.getFinancialTransactionAmount());
        assertEquals("Fee transaction type", TransactionType.findTransactionType(TransactionTypeCode.EmployerFeeDebit), financialTransaction.getTransactionType());
        assertNotNull("Fee Sku", financialTransaction.getSku());
        assertEquals("Fee Sku quantity", 1, financialTransaction.getSkuQuantity());
        switch(pSettlementType) {
            case ACH:
                assertEquals("Fee MMT amount", pFeeAmount, financialTransaction.getFinancialTransactionAmount());
                assertEquals("Fee Settlement type", SettlementType.ACH, financialTransaction.getSettlementTypeCd());
                assertEquals("Fee MMT payment method", PaymentMethod.ACHDirectDeposit, financialTransaction.getMoneyMovementTransaction().getMoneyMovementPaymentMethod());
                break;
            case CheckType:
                assertEquals("Fee Settlement type", SettlementType.CheckType, financialTransaction.getSettlementTypeCd());
                assertNull("MMT", financialTransaction.getMoneyMovementTransaction());
            default:
                break;
        }

        //Assert Billing detail
        BillingDetail billingDetail = financialTransaction.getBillingDetail();
        assertNotNull("Fee Billing detail", billingDetail);
        assertEquals("Billing detail amount", pFeeAmount, billingDetail.getUnitPrice());
        assertEquals("Fee service charge type", pType, billingDetail.getOfferingServiceChargeType());
        assertEquals("Billing detail payroll Run", payrollRun, billingDetail.getPayrollRun());

        if(OfferingServiceChargeType.OtherFee == pType) {
            assertEquals("Billing detail OtherFee Memo", pMemo, billingDetail.getMemo());
        }

        PayrollServices.rollbackUnitOfWork();
    }

}
