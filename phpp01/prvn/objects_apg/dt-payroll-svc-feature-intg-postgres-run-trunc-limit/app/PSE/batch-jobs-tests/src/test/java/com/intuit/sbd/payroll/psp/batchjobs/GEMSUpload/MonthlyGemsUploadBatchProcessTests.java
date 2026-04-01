package com.intuit.sbd.payroll.psp.batchjobs.GEMSUpload;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.dtos.DateDTO;
import com.intuit.sbd.payroll.psp.api.dtos.EntitlementUnitDTO;
import com.intuit.sbd.payroll.psp.api.dtos.VoidPayrollDTO;
import com.intuit.sbd.payroll.psp.batchjobs.BatchJobManager;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: ihannur
 * Date: 5/16/12
 * Time: 6:01 PM
 */

public class MonthlyGemsUploadBatchProcessTests {

    @AfterClass
    public static void afterClass() {
        DataLoadServices.resetAllPaymentTemplateSupportDates();
    }

    @Before
    public void runBeforeEachTest() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        PayrollServicesTest.beforeEachTest();
        PayrollServicesTest.truncateTables();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    }

    /* Test with 1 months data with assisted account balances only.
       companies have both tax and DD services, assisted account balance is included in assisted accounts.
    */
    @Test
    public void testAssistedGemsAccountBalancesWithActiveTaxService() throws Exception {
        DataLoadServices.setPSPDate(2011, 1, 1);
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-941-PAYMENT", PSPDate.getPSPTime());
        DataLoadServices.updatePaymentTemplateSupportedDate("IRS-940-PAYMENT", PSPDate.getPSPTime());
        String psid1 = "123456789";
        String psid2 = "123456780";
        String psid3 = "123456781";

        Company company1 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid1, true, ServiceCode.Tax);
        Company company2 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid2, true, ServiceCode.Tax);
        Company company3 = DataLoadServices.newCompany(SourceSystemCode.QBDT, psid3, true, ServiceCode.Tax);

        List<Company> companies = Arrays.asList(company1, company2, company3);

        String[] states = {"CA"};
        for (Company company : companies) {
            DataLoadServices.enrollEFTPS(company);
            setupCompany(company, states);
            DataLoadServices.runPayrollRun(company, states, PSPDate.getPSPTime(), new DateDTO("2011-01-07"), false);
        }

        DataLoadServices.setPSPDate(2011, 1, 5);

        DataLoadServices.runOffload();
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_941());
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.getIRS_940());
        DataLoadServices.offloadAgencyTaxCredits(PaymentTemplate.findPaymentTemplate("CA-PITSDI-PAYMENT"));

        DataLoadServices.setPSPDate(2011, 1, 6);

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            PayrollRun payrollRun = PayrollRun.findPayrollRunsByState(company, PayrollStatus.OffloadedAll).getFirst();
            VoidPayrollDTO voidPayrollDTO = new VoidPayrollDTO();
            voidPayrollDTO.setSourcePayrollRunId(payrollRun.getSourcePayRunId());
            PayrollServices.payrollManager.voidPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), voidPayrollDTO);
            PayrollServices.commitUnitOfWork();
        }

        DataLoadServices.setPSPDate(2011, 1, 1);
        for (int i = 2; i < 10; i++) {
            BatchJobManager.runJob(BatchJobType.LedgerBalance);
            DataLoadServices.setPSPDate(2011, 1, i);
        }

        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", "201101");

        PayrollServices.beginUnitOfWork();
        GemsUploadBatch gemsUploadBatch = assertOne(Application.find(GemsUploadBatch.class, GemsUploadBatch.UploadStatus().equalTo(GemsUploadBatchStatus.Finalized)));
        MonthlyGemsFileValidator.validateData(gemsUploadBatch, "201101", "201012");
        PayrollServices.rollbackUnitOfWork();

    }

    /* Test with 1 months data with assisted account balances only.
       cancel tax service i.e. deactivate entitlement unit, Adding DIY Entitlement unit,  Balance amounts are reported in DD Gems accounts.
    */
    @Test
    public void testAssistedGemsAccountBalancesAfterCancellingTax() throws Exception {

        testAssistedGemsAccountBalancesWithActiveTaxService();

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        GemsUploadBatch gemsUploadBatch = assertOne(Application.find(GemsUploadBatch.class, GemsUploadBatch.UploadStatus().equalTo(GemsUploadBatchStatus.Finalized)));
        PayrollServices.rollbackUnitOfWork();


        //Cancel Tax Service
        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            Application.refresh(company);
            for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
                if(entitlementUnit.getEntitlement().getEntitlementCode().isAssisted()) {
                    EntitlementUnitDTO dto = PayrollServices.dtoFactory.create(entitlementUnit);
                    dto.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                    assertSuccess(PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(entitlementUnit.getCompany().getSourceSystemCd(), entitlementUnit.getCompany().getSourceCompanyId(), dto));
                }
            }
            PayrollServices.commitUnitOfWork();
            DataLoadServices.cancelService(company, ServiceCode.Tax);
            DataLoadServices.addDIYEntitlementUnit(company, "diy"+company.getSourceCompanyId(), "diyCode"+company.getSourceCompanyId(), EditionType.Enhanced, NumberOfEmployeesType.UNLIMITED);
        }

        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "regen", String.valueOf(gemsUploadBatch.getBatchId()));

        PayrollServices.beginUnitOfWork();
        gemsUploadBatch = assertOne(Application.find(GemsUploadBatch.class, GemsUploadBatch.UploadStatus().equalTo(GemsUploadBatchStatus.Finalized)));
        MonthlyGemsFileValidator.validateData(gemsUploadBatch, "201101", "201012");
        PayrollServices.rollbackUnitOfWork();

    }

    /* Test with 2 months data with assisted and DD account balances.
       Since Tax service is active, DD balance amount is included in assisted gems account balance.
    */
    @Test
    public void testAssistedAndDDGemsAccountBalancesWithDDBillPaymentAndTax() throws Exception {
        testAssistedGemsAccountBalancesWithActiveTaxService();
        //Move to next month, to validate for balances carry forward
        DataLoadServices.setPSPDate(2011, 2, 1);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        PayrollServices.rollbackUnitOfWork();

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            Application.refresh(company);
            if (!company.isCompanyOnService(ServiceCode.DirectDeposit)) {
                PayrollServices.rollbackUnitOfWork();
                DataLoadServices.addDDService(company);
                DataLoadServices.activateDDService(company);
                continue;
            }
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.activateDDService(company);

            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                    DataLoadServices.createDDPayrollRun(company, new DateDTO("2011-02-02"), Employee.findEmployees(company)));
            PSP_PRAssert.assertSuccess("Submit DD payroll", processResult);
            PayrollServices.commitUnitOfWork();
        }

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 2, 2);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> fts = Application.find(FinancialTransaction.class, FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerDdDebit)));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.returnTxns(fts, "R01", "NSF");

        DataLoadServices.setPSPDate(2011, 2, 3);

        BatchJobManager.runJob(BatchJobType.LedgerBalance);

        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", "201102");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<GemsUploadBatch> gemsUploadBatches = Application.find(GemsUploadBatch.class, GemsUploadBatch.UploadStatus().equalTo(GemsUploadBatchStatus.Finalized)).sort(GemsUploadBatch.BatchId());
        assertEquals("Number Finalized GemsUploadBatch", 2, gemsUploadBatches.size());
        MonthlyGemsFileValidator.validateData(gemsUploadBatches.get(0), "201101", "201012");
        MonthlyGemsFileValidator.validateData(gemsUploadBatches.get(1), "201102", "201101");
//        assertEquals("Assisted GemsLedgerPostingRules with balance amount not zero", 6, Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ToDateBalance().notEqualTo(SpcfMoney.ZERO)
//                .And(GemsMonthlyBalance.GemsLedgerPostingRule().ReportingType().equalTo(ReportingType.Tax))).size());
        PayrollServices.rollbackUnitOfWork();

    }

    /* Test with 2 months data with assisted and DD account balances.
       Cancel tax service, DD balance amounts included in DD gems accounts
    */
    @Test
    public void testAssistedAndDDGemsAccountBalancesWithDDAndBillPaymentAfterCancellingTax() throws Exception {
        testAssistedGemsAccountBalancesAfterCancellingTax();
        //Move to next month, to validate for balances carry forward
        DataLoadServices.setPSPDate(2011, 2, 1);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        PayrollServices.rollbackUnitOfWork();

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            Application.refresh(company);
            if (!company.isCompanyOnService(ServiceCode.DirectDeposit)) {
                PayrollServices.rollbackUnitOfWork();
                DataLoadServices.addDDService(company);
                DataLoadServices.activateDDService(company);
                continue;
            }
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.activateDDService(company);

            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                    DataLoadServices.createDDPayrollRun(company, new DateDTO("2011-02-02"), Employee.findEmployees(company)));
            PSP_PRAssert.assertSuccess("Submit DD payroll", processResult);
            PayrollServices.commitUnitOfWork();
        }

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 2, 2);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> fts = Application.find(FinancialTransaction.class, FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerDdDebit)));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.returnTxns(fts, "R01", "NSF");

        DataLoadServices.setPSPDate(2011, 2, 3);

        BatchJobManager.runJob(BatchJobType.LedgerBalance);

        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", "201102");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<GemsUploadBatch> gemsUploadBatches = Application.find(GemsUploadBatch.class, GemsUploadBatch.UploadStatus().equalTo(GemsUploadBatchStatus.Finalized)).sort(GemsUploadBatch.BatchId());
        assertEquals("Number Finalized GemsUploadBatch", 2, gemsUploadBatches.size());
        MonthlyGemsFileValidator.validateData(gemsUploadBatches.get(0), "201101", "201012");
        MonthlyGemsFileValidator.validateData(gemsUploadBatches.get(1), "201102", "201101");
//        assertEquals("Assisted GemsLedgerPostingRules with balance amount not zero", 0, Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ToDateBalance().notEqualTo(SpcfMoney.ZERO)
//                .And(GemsMonthlyBalance.GemsLedgerPostingRule().ReportingType().equalTo(ReportingType.Tax))).size());
//        assertEquals("DD GemsLedgerPostingRules with balance amount not zero", 6, Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ToDateBalance().notEqualTo(SpcfMoney.ZERO)
//                .And(GemsMonthlyBalance.GemsLedgerPostingRule().ReportingType().equalTo(ReportingType.DirectDeposit))).size());
        PayrollServices.rollbackUnitOfWork();

    }

    /* Test with 2 months data with assisted and DD account balances.
       Run batch for second month directly to validate to_balance amounts are calculated correctly with previous data in GemsMonthlyBalance
    */
    @Test
    public void testAssistedAndDDAccountBalancesWithDDAndBillPaymentAfterTaxCancel_withoutPreviousData() throws Exception {
        testAssistedGemsAccountBalancesAfterCancellingTax();

        //Move to next month, to validate for carry forward
        DataLoadServices.setPSPDate(2011, 2, 1);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        PayrollServices.rollbackUnitOfWork();

        for (Company company : companies) {
            PayrollServices.beginUnitOfWork();
            Application.refresh(company);
            if (!company.isCompanyOnService(ServiceCode.DirectDeposit)) {
                PayrollServices.rollbackUnitOfWork();
                DataLoadServices.addDDService(company);
                DataLoadServices.activateDDService(company);
                continue;
            }
            PayrollServices.rollbackUnitOfWork();
            DataLoadServices.activateDDService(company);

            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                    DataLoadServices.createDDPayrollRun(company, new DateDTO("2011-02-02"), Employee.findEmployees(company)));
            PSP_PRAssert.assertSuccess("Submit DD payroll", processResult);
            PayrollServices.commitUnitOfWork();
        }

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 2, 2);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> fts = Application.find(FinancialTransaction.class, FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerDdDebit)));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.returnTxns(fts, "R01", "NSF");

        DataLoadServices.setPSPDate(2011, 2, 3);

        BatchJobManager.runJob(BatchJobType.LedgerBalance);

        PayrollServices.beginUnitOfWork();
        //Deleting data in PSP_GEMS_MONTHLY_BALANCE to validate for calculation for previous month balances from ledger balance
        DomainEntitySet<GemsMonthlyBalance> gemsMonthlyBalances = Application.find(GemsMonthlyBalance.class);
        for (GemsMonthlyBalance gemsMonthlyBalance : gemsMonthlyBalances) {
            Application.delete(gemsMonthlyBalance);
        }
        for (GemsUploadBatch gemsUploadBatch : Application.find(GemsUploadBatch.class)) {
            Application.delete(gemsUploadBatch);
        }
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", "201102");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<GemsUploadBatch> gemsUploadBatches = Application.find(GemsUploadBatch.class, GemsUploadBatch.UploadStatus().equalTo(GemsUploadBatchStatus.Finalized)).sort(GemsUploadBatch.BatchId());
        assertEquals("Number Finalized GemsUploadBatch", 1, gemsUploadBatches.size());
        MonthlyGemsFileValidator.validateData(gemsUploadBatches.get(0), "201102", "201101");
        PayrollServices.rollbackUnitOfWork();

    }

    /* Test with 2 months data with assisted and DD account balances.
       All services are cancelled around same time, balance amount included in assisted service accounts.
    */
    @Test
    public void testAssistedAndDDGemsAccountBalancesWithDDBillPaymentAndTax_cancelAllServices() throws Exception {
        testAssistedAndDDGemsAccountBalancesWithDDBillPaymentAndTax();

        PayrollServices.beginUnitOfWork();
        //Deleting data in PSP_GEMS_MONTHLY_BALANCE to validate for calculation after cancelling all services
        DomainEntitySet<GemsMonthlyBalance> gemsMonthlyBalances = Application.find(GemsMonthlyBalance.class);
        for (GemsMonthlyBalance gemsMonthlyBalance : gemsMonthlyBalances) {
            Application.delete(gemsMonthlyBalance);
        }
        for (GemsUploadBatch gemsUploadBatch : Application.find(GemsUploadBatch.class)) {
            Application.delete(gemsUploadBatch);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Expression<Company> expression = new Query<Company> ().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).EagerLoad(Company.CompanyServiceSet());
        DomainEntitySet<Company> companies = Application.find(Company.class, expression);
        PayrollServices.rollbackUnitOfWork();
        for (Company company : companies) {
            DataLoadServices.cancelService(company, ServiceCode.Tax);
            DataLoadServices.cancelService(company, ServiceCode.DirectDeposit);
            if (company.isCompanyOnService(ServiceCode.BillPayment)) {
                DataLoadServices.cancelService(company, ServiceCode.BillPayment);
            }
        }


        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", "201101");
        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", "201102");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<GemsUploadBatch> gemsUploadBatches = Application.find(GemsUploadBatch.class, GemsUploadBatch.UploadStatus().equalTo(GemsUploadBatchStatus.Finalized)).sort(GemsUploadBatch.BatchId());
        assertEquals("Number Finalized GemsUploadBatch", 2, gemsUploadBatches.size());
        MonthlyGemsFileValidator.validateData(gemsUploadBatches.get(0), "201101", "201012");
        MonthlyGemsFileValidator.validateData(gemsUploadBatches.get(1), "201102", "201101");
//        assertEquals("Assisted GemsLedgerPostingRules with balance amount not zero", 6, Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ToDateBalance().notEqualTo(SpcfMoney.ZERO)
//                .And(GemsMonthlyBalance.GemsLedgerPostingRule().ReportingType().equalTo(ReportingType.Tax))).size());
        PayrollServices.rollbackUnitOfWork();


    }

    /* Test with 2 months data with assisted and DD account balances.
       Cancel all services, check balances are still included in Tax, because assisted entitlement unit is still active.
    */
    @Test
    public void testAssistedAndDDGemsAccountBalancesWithDDBillPaymentAndTax_cancelAll() throws Exception {
        testAssistedAndDDGemsAccountBalancesWithDDBillPaymentAndTax();

        PayrollServices.beginUnitOfWork();
        //Deleting data in PSP_GEMS_MONTHLY_BALANCE to validate for calculation after cancelling all services
        DomainEntitySet<GemsMonthlyBalance> gemsMonthlyBalances = Application.find(GemsMonthlyBalance.class);
        for (GemsMonthlyBalance gemsMonthlyBalance : gemsMonthlyBalances) {
            Application.delete(gemsMonthlyBalance);
        }
        for (GemsUploadBatch gemsUploadBatch : Application.find(GemsUploadBatch.class)) {
            Application.delete(gemsUploadBatch);
        }
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Expression<Company> expression = new Query<Company> ().Where(Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT)).EagerLoad(Company.CompanyServiceSet());
        DomainEntitySet<Company> companies = Application.find(Company.class, expression);
        PayrollServices.rollbackUnitOfWork();
        for (Company company : companies) {
            DataLoadServices.cancelService(company, ServiceCode.Tax);
            DataLoadServices.cancelService(company, ServiceCode.DirectDeposit);
            if (company.isCompanyOnService(ServiceCode.BillPayment)) {
                DataLoadServices.cancelService(company, ServiceCode.BillPayment);
            }
        }

        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", "201101");
        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", "201102");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<GemsUploadBatch> gemsUploadBatches = Application.find(GemsUploadBatch.class, GemsUploadBatch.UploadStatus().equalTo(GemsUploadBatchStatus.Finalized)).sort(GemsUploadBatch.BatchId());
        assertEquals("Number Finalized GemsUploadBatch", 2, gemsUploadBatches.size());
        MonthlyGemsFileValidator.validateData(gemsUploadBatches.get(0), "201101", "201012");
        MonthlyGemsFileValidator.validateData(gemsUploadBatches.get(1), "201102", "201101");
//        assertEquals("Assisted GemsLedgerPostingRules with balance amount not zero", 6, Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ToDateBalance().notEqualTo(SpcfMoney.ZERO)
//                .And(GemsMonthlyBalance.GemsLedgerPostingRule().ReportingType().equalTo(ReportingType.Tax))).size());
//        assertEquals("DD GemsLedgerPostingRules with balance amount not zero", 0, Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ToDateBalance().notEqualTo(SpcfMoney.ZERO)
//                .And(GemsMonthlyBalance.GemsLedgerPostingRule().ReportingType().equalTo(ReportingType.DirectDeposit))).size());
        PayrollServices.rollbackUnitOfWork();

    }

    /* Test with 3 months data with assisted and DD account balances.
     * First 2 month companies are assisted with account balances including DD transactions.
     * third month cancel tax service for 2 companies submit DD payrolls, validate only new transactions are added to DD account balances for only 2 companies, other company still added to assisted accounts.
     */
    @Test
    public void testAssistedAndDDGemsAccountBalancesWithDDBillPaymentAndTax_CancelTax() throws Exception {
        testAssistedAndDDGemsAccountBalancesWithDDBillPaymentAndTax();

        //Move to next month, to validate for DD transaction amount added to assisted account balances will not combine to DD account balances.
        DataLoadServices.setPSPDate(2011, 3, 1);
        PayrollServices.beginUnitOfWork();
        DomainEntitySet<Company> companies = Application.find(Company.class, Company.SourceSystemCd().equalTo(SourceSystemCode.QBDT));
        PayrollServices.rollbackUnitOfWork();

        int counter = 0;
        for (Company company : companies) {
            counter ++;
            if(counter < 3) {
                PayrollServices.beginUnitOfWork();
                Application.refresh(company);
                for (EntitlementUnit entitlementUnit : company.getEntitlementUnitCollection()) {
                    if(entitlementUnit.getEntitlement().getEntitlementCode().isAssisted()) {
                        EntitlementUnitDTO dto = PayrollServices.dtoFactory.create(entitlementUnit);
                        dto.setEntitlementUnitStatus(EntitlementUnitStatusCode.Deactivated);
                        assertSuccess(PayrollServices.entitlementManager.addOrUpdateEntitlementUnit(entitlementUnit.getCompany().getSourceSystemCd(), entitlementUnit.getCompany().getSourceCompanyId(), dto));
                    }
                }
                PayrollServices.commitUnitOfWork();
                DataLoadServices.cancelService(company, ServiceCode.Tax);
                DataLoadServices.addDDService(company);
                DataLoadServices.addDIYEntitlementUnit(company, "diy"+company.getSourceCompanyId(), "diyCode"+company.getSourceCompanyId(), EditionType.Enhanced, NumberOfEmployeesType.UNLIMITED);
            } else {
                DataLoadServices.removeCompanyOnHoldReasons(company);
            }

            PayrollServices.beginUnitOfWork();
            ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(),
                    DataLoadServices.createDDPayrollRun(company, new DateDTO("2011-03-02"), Employee.findEmployees(company)));
            PSP_PRAssert.assertSuccess("Submit DD payroll", processResult);
            PayrollServices.commitUnitOfWork();
        }

        DataLoadServices.runOffload();

        DataLoadServices.setPSPDate(2011, 3, 2);

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<FinancialTransaction> fts = Application.find(FinancialTransaction.class, FinancialTransaction.CurrentTransactionState().equalTo(TransactionState.findTransactionState(TransactionStateCode.Executed))
                                                            .And(FinancialTransaction.TransactionType().equalTo(TransactionType.findTransactionType(TransactionTypeCode.EmployerDdDebit))));
        PayrollServices.rollbackUnitOfWork();

        DataLoadServices.returnTxns(fts, "R01", "NSF");

        DataLoadServices.setPSPDate(2011, 3, 3);

        BatchJobManager.runJob(BatchJobType.LedgerBalance);

        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", "201103");

        PayrollServices.beginUnitOfWork();
        DomainEntitySet<GemsUploadBatch> gemsUploadBatches = Application.find(GemsUploadBatch.class, GemsUploadBatch.UploadStatus().equalTo(GemsUploadBatchStatus.Finalized)).sort(GemsUploadBatch.BatchId());
        assertEquals("Number Finalized GemsUploadBatch", 3, gemsUploadBatches.size());
        MonthlyGemsFileValidator.validateData(gemsUploadBatches.get(2), "201103", "201102");
//        assertEquals("Assisted GemsLedgerPostingRules with balance amount not zero", 10, Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ToDateBalance().notEqualTo(SpcfMoney.ZERO)
//                .And(GemsMonthlyBalance.GemsLedgerPostingRule().ReportingType().equalTo(ReportingType.Tax))).size());
//        assertEquals("DD GemsLedgerPostingRules with balance amount not zero", 2, Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ToDateBalance().notEqualTo(SpcfMoney.ZERO)
//                .And(GemsMonthlyBalance.GemsLedgerPostingRule().ReportingType().equalTo(ReportingType.DirectDeposit))).size());
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        //Deleting data in PSP_GEMS_MONTHLY_BALANCE to validate for calculation for previous month balances from ledger balance
        DomainEntitySet<GemsMonthlyBalance> gemsMonthlyBalances = Application.find(GemsMonthlyBalance.class);
        for (GemsMonthlyBalance gemsMonthlyBalance : gemsMonthlyBalances) {
            Application.delete(gemsMonthlyBalance);
        }
        for (GemsUploadBatch gemsUploadBatch : Application.find(GemsUploadBatch.class)) {
            Application.delete(gemsUploadBatch);
        }
        PayrollServices.commitUnitOfWork();

        BatchJobManager.runJob(BatchJobType.GemsGeneralLedger, "gen", "201103");
        PayrollServices.beginUnitOfWork();
        GemsUploadBatch gemsUploadBatch = assertOne(Application.find(GemsUploadBatch.class, GemsUploadBatch.UploadStatus().equalTo(GemsUploadBatchStatus.Finalized)).sort(GemsUploadBatch.BatchId()));
        MonthlyGemsFileValidator.validateData(gemsUploadBatch, "201103", "201102");
//        assertEquals("Assisted GemsLedgerPostingRules with balance amount not zero", 4, Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ToDateBalance().notEqualTo(SpcfMoney.ZERO)
//                .And(GemsMonthlyBalance.GemsLedgerPostingRule().ReportingType().equalTo(ReportingType.Tax))).size());
//        assertEquals("DD GemsLedgerPostingRules with balance amount not zero", 4, Application.find(GemsMonthlyBalance.class, GemsMonthlyBalance.ToDateBalance().notEqualTo(SpcfMoney.ZERO)
//                .And(GemsMonthlyBalance.GemsLedgerPostingRule().ReportingType().equalTo(ReportingType.DirectDeposit))).size());
        PayrollServices.rollbackUnitOfWork();
    }


    public void setupCompany(Company pCompany, String[] states) {
        DataLoadServices.addFederalTaxCompanyLaws(pCompany);
        DataLoadServices.addEEs(pCompany, 2);

        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(pCompany.getSourceCompanyId(), "IRS-941-PAYMENT");

        DataLoadServices.setupCompanyAgency(states, PaymentTemplateCategory.Withholding, PaymentMethod.ACHCredit);

    }

}
