package com.intuit.sbd.payroll.psp.adapters.dis.v1_8;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.sap.adapter.PayrollRunAdapter;
import com.intuit.sbd.payroll.psp.api.LiabilityAdjustmentOptionsDTO;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.dtos.*;
import com.intuit.sbd.payroll.psp.batchjobs.offload.OffloadACHTransactions;
import com.intuit.sbd.payroll.psp.domain.*;
import com.intuit.sbd.payroll.psp.junit.PSP_PRAssert;
import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company1Dataloader;
import com.intuit.sbd.payroll.psp.processes.dataloaders.Company3Dataloader;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.util.SpcfUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.SpcfMoney;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertOne;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccess;
import static com.intuit.sbd.payroll.psp.api.PayrollServicesTest.assertSuccessResult;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/test/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/DISCompanyDataloader.java $
 * $Revision: #2 $
 * $DateTime: 2012/10/15 17:18:20 $
 * $Author: JChickanosky $
 */
public class DISCompanyDataloader {
    private static String entitlementLicenseNumber = "lic_123456789";
    private static String entitlementEoc = "eoc_123456789";

    public static Company setupMigratedCompanyWithDDPayrollAndAssistedPayroll() {
        return setupMigratedCompanyWithDDPayrollAndAssistedPayroll("20120306000000");
    }

    public static Company setupMigratedCompanyWithDDPayrollAndAssistedPayroll(String pPSPDateForService) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 4, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        // create company
        PayrollServices.beginUnitOfWork();
        Company3Dataloader c1dl = new Company3Dataloader();
        c1dl.persistCompany3();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(pPSPDateForService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addServices(company, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        // offload verification transactions
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // set the psp time to allow offload of payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20120309000000");
        Application.commitUnitOfWork();

        // create payroll for same date
        PayrollServices.beginUnitOfWork();
        PayrollRunDTO payrollRun1DTO = c1dl.getCompanyPR_DoesNotExceedLimits(new DateDTO("2012-02-10"));
        c1dl.persistPayrollRun(payrollRun1DTO);
        PayrollServices.commitUnitOfWork();

        // offload payroll
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // cancel the dd service
        PayrollServices.beginUnitOfWork();
        company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        CompanyService companyService = company.getCompanyService(ServiceCode.DirectDeposit);
        companyService.setStatusCd(ServiceSubStatusCode.Cancelled);
        Application.save(companyService);
        PayrollServices.commitUnitOfWork();

        return c1dl.getCompany();
    }

    public static String setupCompanyWithDDPayrollAndAssistedPayroll(String pPSPDateForService) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20120313000000");
        Application.commitUnitOfWork();

        // create company
        PayrollServices.beginUnitOfWork();
        Company1Dataloader c1dl = new Company1Dataloader();
        c1dl.persistCompany1();
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        Company company = Company.findCompany(c1dl.getCompany().getSourceCompanyId(), SourceSystemCode.QBDT);
        PayrollServices.rollbackUnitOfWork();

        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(pPSPDateForService);
        PayrollServices.commitUnitOfWork();

        DataLoadServices.addServices(company, ServiceCode.Tax);
        DataLoadServices.activateTaxService(company);

        // offload verification transactions
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        // set the psp time to allow offload of payroll
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20120316000000");
        Application.commitUnitOfWork();

        // offload payroll
        new OffloadACHTransactions().offloadAndPostOffload(OffloadGroup.Codes.STANDARD, null);

        return c1dl.getCompany().getSourceCompanyId();
    }

    public static Company setupCompany() {
        return setupCompany(null, null);
    }

    public static Company setupCompany(String pEIN, String pPSID) {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(SpcfCalendar.createInstance(2011, 1, 1, SpcfTimeZone.getLocalTimeZone()));
        PayrollServices.commitUnitOfWork();

        Company company = null;
        if (pEIN == null && pPSID == null) {
            company = DataLoadServices.newCompany(SourceSystemCode.QBDT, false, ServiceCode.DirectDeposit, ServiceCode.Tax);
        } else {
            if (pEIN != null && pPSID != null) {
                company = DataLoadServices.newCompany(SourceSystemCode.QBDT, pPSID, pEIN, false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            } else {
                company = DataLoadServices.newCompany(SourceSystemCode.QBDT, pPSID, false, ServiceCode.DirectDeposit, ServiceCode.Tax);
            }
        }
        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);
//        DataLoadServices.claimNoFeesOffer(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.addEEs(company, 2, false, true);

        DomainEntitySet<Employee> employeeListDES = Employee.findEmployees(company);
        for (Employee employee : employeeListDES) {
            DataLoadServices.addEEBankAccount(company, employee, BankAccountType.Checking);
        }

        DataLoadServices.updateEffectiveDepositFreqEffDate(company.getSourceCompanyId(), "IRS-941-PAYMENT", DepositFrequencyCode.SEMIWEEKLY);
        DataLoadServices.enrollEFTPS(company);
        return company;
    }


    public static PayrollRun runPayroll(Company pCompany, DateDTO pDate, String pAmount) {
        PayrollRunDTO payrollRunDTO = new PayrollRunDTO();
        PayrollServices.beginUnitOfWork();
        DataLoadServices.addAssistedBankAccounts(pCompany, payrollRunDTO);
        payrollRunDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, pCompany, pDate, new ArrayList<Employee>(pCompany.getCloudEmployees()), new String[]{"61", "62", "63", "64", "66"}, new String[]{pAmount, pAmount, pAmount, pAmount, pAmount});
        ProcessResult<PayrollRun> processResult = PayrollServices.payrollManager.submitPayroll(SourceSystemCode.QBDT, pCompany.getSourceCompanyId(), payrollRunDTO);
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
        return processResult.getResult();
    }

    public static void setupPaymentTemplateForEachTest() {
        DataLoadServices.resetAllPaymentTemplateSupportDates(true);
        DataLoadServices.updateIRSPaymentTemplateSupportDate(SpcfCalendar.createInstance(2005, 1, 1));
    }

    public static PayrollRun runCloudOnlyPayroll(String pPsid) {
        PayrollRun payrollRun = null;
        return payrollRun;

    }

    public static List<PayrollRun> loadCompanyWithPayrolls(String pPsid) {
        List<PayrollRun> payrollRuns = new ArrayList<PayrollRun>();

        Company company = DataLoadServices.newCompany(SourceSystemCode.QBDT, pPsid, false, ServiceCode.Tax);
        List<Employee> employeeList = DataLoadServices.addEEs(company, 2, false, false);

        DataLoadServices.addCompanyPIN(company, null);
        DataLoadServices.addCompanyBankAccount(company);

        DataLoadServices.addFederalTaxCompanyLaws(company);
        DataLoadServices.updateEffectiveDepositFreqEffDateToQuarterly(company.getSourceCompanyId(), "IRS-941-PAYMENT");

        // Jan 5, 2011 is a Wednesday
        payrollRuns.add(loadPayroll(pPsid, "20110105"));
        payrollRuns.add(loadPayroll(pPsid, "20110112"));
        return payrollRuns;
    }

    public static PayrollRun loadPayroll(String pPsid, String pPayrollDate) {

        SpcfCalendar payrollDateSpcfCal = CalendarUtils.createInstanceFromDate(pPayrollDate);
        // Copied from testEmployerTaxNSFPayRunStatus()
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(pPayrollDate + "000000");
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(pPsid, SourceSystemCode.QBDT);

        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};

        PayrollServices.beginUnitOfWork();
//        SpcfCalendar checkDate = PSPDate.getPSPTime();
        DomainEntitySet<Employee> employeeListDES = Employee.findEmployees(company);
        List<Employee> employeeList = new ArrayList<Employee>();
        employeeList.add(employeeListDES.get(0));
        employeeList.add(employeeListDES.get(1));

        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(payrollDateSpcfCal), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        DateDTO payrollDateDTO = new DateDTO(payrollDateSpcfCal);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, payrollDateDTO, employeeList, lawIds, amounts);
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);

        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);
        return (PayrollRun) processResult.getResult();
    }

    public static PayrollRun loadOffloadOnlyPayroll(String pPsid, String pPayrollDate) {

        SpcfCalendar payrollDateSpcfCal = CalendarUtils.createInstanceFromDate(pPayrollDate);
        // Copied from testEmployerTaxNSFPayRunStatus()
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(pPayrollDate + "000000");
        PayrollServices.commitUnitOfWork();

        Company company = Company.findCompany(pPsid, SourceSystemCode.QBDT);

        String[] lawIds = {"61", "62", "63", "64", "143", "1"};
        String[] amounts = {"5", "12", "5.5", "45", "2", "25"};

        PayrollServices.beginUnitOfWork();
//        SpcfCalendar checkDate = PSPDate.getPSPTime();
        DomainEntitySet<Employee> employeeListDES = Employee.findEmployees(company);
        List<Employee> employeeList = new ArrayList<Employee>();
        employeeList.add(employeeListDES.get(0));
        employeeList.add(employeeListDES.get(1));

        PayrollRunDTO payrollRunDTO = DataLoadServices.createDDPayrollRun(company, new DateDTO(payrollDateSpcfCal), employeeList);
        DataLoadServices.addAssistedBankAccounts(company, payrollRunDTO);
        DateDTO payrollDateDTO = new DateDTO(payrollDateSpcfCal);
        PayrollRunDTO payrollDTO = DataLoadServices.createPayrollRunWithLawsAndAmounts(payrollRunDTO, company, payrollDateDTO, employeeList, lawIds, amounts);
        for (PaycheckDTO paycheck : payrollDTO.getPaychecks()) {
            paycheck.setDdTransactions(null);
        }
        ProcessResult processResult = PayrollServices.payrollManager.submitPayroll(company.getSourceSystemCd(), company.getSourceCompanyId(), payrollDTO);

        PayrollServices.commitUnitOfWork();
        PSP_PRAssert.assertSuccess("submit payroll", processResult);
        return (PayrollRun) processResult.getResult();
    }

    public String loadCompanyWithoutPayrolls() {
        String psid = "123456789";
        DataLoadServices.setupCompany(psid);
        return psid;
    }

    public static PayrollRun loadBillPayment(String pPsid, String pPayrollDate) {

        return null;  //To change body of created methods use File | Settings | File Templates.
    }

    // Example usage:
    //Date refundDate = new Date(2011, 01, 10);
    //refundPayrollTransaction(payrollRun1, company, refundDate, TransactionTypeCode.EmployerFeeDebit, "Monthly Fee");
    public static void refundPayrollTransaction(PayrollRun pPayrollRun, Company pCompany, Date pRefundDate, TransactionTypeCode pTransactionTypeCode, String pItemName) throws Throwable {
        DomainEntitySet<FinancialTransaction> fnTxs = pPayrollRun.getFinancialTransactions(pTransactionTypeCode);
        FinancialTransaction foundFnTx = fnTxs.get(0);
        for (FinancialTransaction fnTx : fnTxs) {
            if (fnTx.getBillingDetail().getItemName().equals(pItemName)) {
                foundFnTx = fnTx;
                break;
            }
        }
        assertNotNull("Could not find fn tx on payroll with TransactionTypeCode " + pTransactionTypeCode + " and Billing Detail item name " + pItemName + ".", foundFnTx);

        PayrollRunAdapter payrollRunAdapter = new PayrollRunAdapter();

        payrollRunAdapter.refundEmployerTransaction(
                SourceSystemCode.QBDT.toString(),
                pCompany.getSourceCompanyId(),
                foundFnTx.getId().toString(),
                SpcfUtils.convertToBigDecimal(foundFnTx.getFinancialTransactionAmount()).doubleValue(),
                pRefundDate,
                SettlementTypeDTO.ACH.toString());
    }

    public static PayrollRun createFeeOnlyPayroll(Company pCompany) throws Throwable {
        ERFeeAddDTO feeAddDTO = new ERFeeAddDTO(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), null,
                SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(PSPDate.getPSPTime()),
                new SpcfMoney("5.10"),
                OfferingServiceChargeType.OtherFee, "Other Fee-Testing");

        PayrollServices.beginUnitOfWork();
        assertSuccess(PayrollServices.financialTransactionManager.createManualFeeTransaction(feeAddDTO));
        PayrollServices.commitUnitOfWork();

        PayrollServices.beginUnitOfWork();
        pCompany = Company.findCompany(pCompany.getSourceCompanyId(), pCompany.getSourceSystemCd());
        PayrollRun payrollRun = assertOne(PayrollRun.findPayrollRunsByType(pCompany, null, null, PayrollType.FeeOnly));
        feeAddDTO = new ERFeeAddDTO(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), payrollRun.getSourcePayRunId(),
                SettlementTypeDTO.ACH,
                CalendarUtils.convertToDate(PSPDate.getPSPTime()),
                new SpcfMoney("10.20"),
                OfferingServiceChargeType.OtherFee, "Other Fee-Testing2");

        FinancialTransaction financialTransaction = assertSuccessResult(PayrollServices.financialTransactionManager.createManualFeeTransaction(feeAddDTO)).getFirst();
        PayrollServices.commitUnitOfWork();
        return payrollRun;
    }

    public static CompanyAdjustmentSubmission createAdjustmentPayroll(Company pCompany) throws Throwable {
        // Copied this FIT attribute from AddLiabilityAdjustmentsCoreTests
        String FIT = "1";

        CompanyAdjustmentSubmissionDTO companyAdjustmentSubmissionDTO = DataLoadServices.createCompanyAdjustmentSubmissionDTO("Adjust_1", new DateDTO(PSPDate.getPSPTime()));
        LiabilityAdjustmentDTO liabilityAdjustmentDTO = DataLoadServices.createLiabilityAdjustmentDTO(FIT, "1", null, new DateDTO(PSPDate.getPSPTime()), new SpcfMoney("0.0"), new SpcfMoney("2727.25"), new SpcfMoney("0.0"), false);
        Collection<LiabilityAdjustmentDTO> liabilityAdjustmentDTOs = new ArrayList<LiabilityAdjustmentDTO>();
        liabilityAdjustmentDTOs.add(liabilityAdjustmentDTO);
        companyAdjustmentSubmissionDTO.setLiabilityAdjustmentDTOs(liabilityAdjustmentDTOs);
        LiabilityAdjustmentOptionsDTO liabilityAdjustmentOptionsDTO = new LiabilityAdjustmentOptionsDTO();
        liabilityAdjustmentOptionsDTO.setRecordLiabilities(true);
        liabilityAdjustmentOptionsDTO.setDebitCustomer(true);
        liabilityAdjustmentOptionsDTO.setRecordFinancialTransactions(true);
        PayrollServices.beginUnitOfWork();
        ProcessResult<CompanyAdjustmentSubmission> processResult = PayrollServices.payrollManager
                .addLiabilityAdjustments(SourceSystemCode.QBDT, pCompany.getSourceCompanyId(), null, companyAdjustmentSubmissionDTO, new DateDTO(PSPDate.getPSPTime()), liabilityAdjustmentOptionsDTO);
        assertSuccess(processResult);
        PayrollServices.commitUnitOfWork();
        return processResult.getResult();
    }


    public static void addCourtesyFeeRefund(Company pCompany, String pAmount, String pNote, SettlementTypeDTO pSettlementTypeDTO) {
        PayrollServices.beginUnitOfWork();
        ProcessResult<FinancialTransaction> processResult = PayrollServices.financialTransactionManager.addCourtesyFeeRefund(
                pCompany.getSourceSystemCd(),
                pCompany.getSourceCompanyId(),
                new SpcfMoney(pAmount),
                pNote,
                pSettlementTypeDTO
        );
        PayrollServices.commitUnitOfWork();
        assertSuccess(processResult);
    }
}
