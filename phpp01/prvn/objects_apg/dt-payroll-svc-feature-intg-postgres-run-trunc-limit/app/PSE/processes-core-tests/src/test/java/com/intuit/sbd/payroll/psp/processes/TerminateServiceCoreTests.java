package com.intuit.sbd.payroll.psp.processes;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: June 16, 2011
 * Time: 11:54:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class TerminateServiceCoreTests {
    private static final String FIT = "1";
    private static final String FICA = "61";


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2007, 8, 22, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    @Test
    public void testTerminateRecallsTaxPayroll() {
        //submit a payroll
        //terminate company
        //validate the payroll is cancelled and no outstanding FTs

        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"));

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult terminatePR = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Terminated);
        PSP_PRAssert.assertSuccess("Terminate", terminatePR);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PayrollRun payrollRun = PayrollRun.findPayrollRun(company, payrollRunDTO.getPayrollTXBatchId());
        assertEquals("PayrollRun Status", PayrollStatus.Canceled, payrollRun.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> fts = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.CurrentTransactionState().notEqualTo(TransactionState.findTransactionState(TransactionStateCode.Cancelled)));
        assertEquals("Non cancelled FTs", 0, fts.size());
        PayrollServices.rollbackUnitOfWork();
        
    }

    @Test
    public void testTerminateRecallsTaxPayrollWithAdjustments() {
        //submit a payroll that includes adjustments (use whatever QBDT would send)
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.enrollEFTPS(company);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-178.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-150.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        Collection<CompanyAdjustmentSubmissionDTO> comAdjustments = new ArrayList<CompanyAdjustmentSubmissionDTO>(); 
        comAdjustments.add(companyAdjustmentSubmissionDTO);
        
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-18"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1100", "550", "450", "250", "6.5", "5.6"});
        payrollDTO.setCompanyAdjustmentSubmissionDTOs(comAdjustments);
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        PayrollServices.beginUnitOfWork();
        ProcessResult terminatePR = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Terminated);
        PSP_PRAssert.assertSuccess("Terminate", terminatePR);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRuns(company);
        assertEquals("Number of total payrolls", 1, payrollRuns.size());
        assertEquals("Payroll Status", PayrollStatus.Complete, payrollRuns.get(0).getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload();

        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("IRS-941-PAYMENT"));

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<MoneyMovementTransaction> mmts = MoneyMovementTransaction.findTaxPayments().setCompany(company).setTaxPaymentStatuses(TaxPaymentStatus.AcknowledgedByAgency).find();
        assertEquals("EFTPS payments", 1, mmts.size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.assertLedgerBalances(company);       
       
    }

    @Test
    public void testTerminateFailsWithExecutedPayment() {
        //submit a payroll with 941 and 940
        //do not offload debit, but submit the 941 payment
        //attempt to terminate
        String psid = "123456789";
        PayrollRunDTO payrollRunDTO = DataLoadServices.setupCompanyAndRunPayrollForCA(psid, new DateDTO("2011-04-01"), new DateDTO("2011-04-15"));

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 7, 29, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();
        DataLoadServices.enrollEFTPS(company);
        BatchJobManager.runJob(BatchJobType.EftpsEnrollments);

        PayrollServices.beginUnitOfWork();
        ProcessResult terminatePR = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Terminated);
        assertEquals("Number of errors", 1, terminatePR.getErrorMessages().size());
        assertEquals("Error message code", "1015", terminatePR.getErrorMessages().get(0).getMessageCode());
        PayrollServices.commitUnitOfWork();
    }

    @Test
    public void testTerminateCancelsPartiallyRecalledPayroll() {
        //include partially voided adjustment as well
        String psid = "123456789";
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 15, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid, false, ServiceCode.Tax);
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2010, 10, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addCOBRACompanyLaw(company);
        DataLoadServices.updateCompanyService(company, ServiceCode.Tax, ServiceSubStatusCode.ActiveCurrent);
        List<Employee> emps = DataLoadServices.addEEs(company, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.QUARTERLY, SpcfCalendar.createInstance(2010, 1, 1));
        DataLoadServices.enrollEFTPS(company);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2010, 10, 25, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, new DateDTO("2010-10-18"), emps, new String[]{"61", "62", "63", "64", "1", "65", "66"}, new String[]{"500", "1100", "550", "450", "250", "6.5", "5.6"});

        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, psid, payrollDTO);
        PayrollRun payroll = processResult.getResult();
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);

        String adjustmentSourceId = "Adjust_1";
        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO(adjustmentSourceId, new DateDTO(PSPDate.getPSPTime()));
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, FIT, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("178.20"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("150.27"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> adjustmentProcessResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(adjustmentProcessResult);
        PayrollServices.commitUnitOfWork();

        //Recall one paycheck from the payroll run
        PayrollServices.beginUnitOfWork();
        Application.refresh(payroll);
        String recallPaycheck = payroll.getPaycheckCollection().get(0).getSourcePaycheckId();
        TransactionCancelEEDTO transactionCancelDTO = new TransactionCancelEEDTO();
        ArrayList<String> paycheckList = new ArrayList<String>();
        paycheckList.add(recallPaycheck);
        transactionCancelDTO.setSourcePayrollRunId(payrollRunDTO.getPayrollTXBatchId());
        transactionCancelDTO.setSourcePaycheckIdList(paycheckList);

        ProcessResult recallProcessResult = PayrollServices.payrollManager.cancelEmployeeTransaction(SourceSystemCode.QBDT, psid, transactionCancelDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(recallProcessResult);

        PayrollServices.beginUnitOfWork();
        assertEquals("Payroll Run status after company termination", PayrollStatus.Pending, payroll.getPayrollRunStatus());
        PayrollServices.rollbackUnitOfWork();

        //Recall partial adjustment
        companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_2", new DateDTO(PSPDate.getPSPTime()));
        liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, FIT, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-150.00"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FICA, FICA, null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("-100.00"), new SpcfMoney("0.0"), new SpcfMoney("0.0"), false);
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);

        liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecall(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);

        PayrollServices.beginUnitOfWork();
        companyAdjustmentSubmissionDTO.setOriginalSubmissionId(CompanyAdjustmentSubmission.findCompanyAdjustmentSubmission(company, adjustmentSourceId).getId());
        adjustmentProcessResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, psid, null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(adjustmentProcessResult);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        ProcessResult terminatePR = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Terminated);
        PSP_PRAssert.assertSuccess("Terminate", terminatePR);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(payroll);
        assertEquals("Payroll Run status after company termination", PayrollStatus.Canceled, payroll.getPayrollRunStatus());
        DomainEntitySet<FinancialTransaction> fts = payroll.getFinancialTransactionCollection().find(FinancialTransaction.CurrentTransactionState().notEqualTo(TransactionState.findTransactionState(TransactionStateCode.Cancelled)));
        assertEquals("Non cancelled FTs", 0, fts.size());
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.runOffload();

        DataLoadServices.assertLedgerBalances(company,
                new DataLoadServices.LB(LedgerAccountCode.AgencyTaxRefund, 0),
                new DataLoadServices.LB(LedgerAccountCode.TaxCurrentLiability, 0),
                new DataLoadServices.LB(LedgerAccountCode.ERLiabilityOffset, 0)
        );
        
    }

    @Test
    public void testTerminateCancels100K() throws Exception {
        String psid = "123456789";
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        new PayrollVoidTaxTests().create100KPayrolls();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(psid, SourceSystemCode.QBDT);
        ProcessResult terminatePR = PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Terminated);
        PSP_PRAssert.assertSuccess("Terminate", terminatePR);
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<PayrollRun> payrollRuns = PayrollRun.findPayrollRunsByState(company, PayrollStatus.Canceled);
        assertEquals("Number of cancelled payrolls", 3, payrollRuns.size());
        for (PayrollRun payrollRun : payrollRuns) {
            DomainEntitySet<FinancialTransaction> fts = payrollRun.getFinancialTransactionCollection().find(FinancialTransaction.CurrentTransactionState().notEqualTo(TransactionState.findTransactionState(TransactionStateCode.Cancelled)));
            assertEquals("Non cancelled FTs", 0, fts.size());
        }
        PayrollServices.rollbackUnitOfWork();
    }

    @Test
    public void testTerminateWithPendingAgencyTaxDebits() throws Exception {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1, SpcfTimeZone.getLocalTimeZone()));
//        DataLoadServices.updateCAEDDPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1, SpcfTimeZone.getLocalTimeZone()));

        DataLoadServices.setPSPDate(2012, 1, 1);
        Company company = DataLoadPalette.setupTaxCompany();

        DataLoadServices.setPSPDate(2013, 1, 1);
        PayrollRun voidPayroll = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 10));

        DataLoadServices.setPSPDate(2013, 1, 8);
        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2013, 1, 15);
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());


        DataLoadServices.setPSPDate(2013, 1, 21);
        voidAPaycheck(voidPayroll);

        DataLoadServices.setPSPDate(2013, 1, 22);
        PayrollRun newPayroll = DataLoadPalette.runSimpleTaxPayroll(company, new DateDTO(2013, 1, 27));

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.companyManager.updateServiceStatus(company.getSourceSystemCd(), company.getSourceCompanyId(), ServiceCode.Tax, ServiceSubStatusCode.Terminated));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Application.refresh(newPayroll);
        assertEquals(0, newPayroll.getFinancialTransactionCollection().find(FinancialTransaction.CurrentTransactionState().TransactionStateCd().notEqualTo(TransactionStateCode.Cancelled)).size());
        Application.rollbackUnitOfWork();

    }

    public void voidAPaycheck(PayrollRun payrollRun) {
        VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
        voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
        List<String> voidPaychecks = new ArrayList<String>();
        voidPaychecks.add(payrollRun.getPaycheckCollection().get(0).getSourcePaycheckId());
        voidPayrollDTO.setPaycheckIdList(voidPaychecks);

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.payrollManager.voidPayroll(SourceSystemCode.QBDT, payrollRun.getCompany().getSourceCompanyId(), voidPayrollDTO));
        PayrollServices.commitUnitOfWork();
    }
}
